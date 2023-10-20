---
title: "Heroku から Google Cloud Platform へお引越し"
date: 2022-05-02T21:26:43+09:00
draft: false
tags:
  - 日常
  - 技術
---

Heroku で動かしていたアプリケーションを Google Cloud Run と Cloud Scheduler に移した。以下みたいな用途で Heroku を利用していた。

- 主に Slack interactive components で利用するエンドポイント用の sinatra app (Heroku dyno, Redis)
- Twitter の定期投稿とかで使っている rake task (Heroku Scheduler)

{{< vertical_space >}}

### Cloud Run

https://cloud.google.com/run

Heroku Dyno からの移行先にした。Heroku ではソースコードをそのまま push して buildpack にいい感じにしてもらっていたし、Google Cloud Buildpacks にお世話になるかとも思ったけど、良い機会なのでエントリポイントもまるっと管理できるし手元でも管理が楽なのでコンテナ化した。

```dockerfile
FROM rubylang/ruby:3.0.3-focal
WORKDIR /app

RUN groupadd -r --gid 1001 kota && useradd -m -r --gid 1001 kota
RUN chown -R kota:kota /app

USER kota

COPY Gemfile /app/
COPY Gemfile.lock /app/
ENV BUNDLE_FROZEN=true

RUN bundle config set path 'vendor/bundle'
RUN bundle config set without 'test'
RUN bundle install

COPY . /app/

EXPOSE 8080
ENV GOOGLE_APPLICATION_CREDENTIALS="/app/key.json"
CMD ["bundle", "exec", "ruby", "kota.rb"]
```

{{< vertical_space >}}

GitHub 上でマージしたらデプロイしてほしい。[https://docs.github.com/ja/enterprise-cloud@latest/actions/deployment/security-hardening-your-deployments/configuring-openid-connect-in-google-cloud-platform](https://docs.github.com/ja/enterprise-cloud@latest/actions/deployment/security-hardening-your-deployments/configuring-openid-connect-in-google-cloud-platform) にあるように、GitHub Actions では OpenID Connect をサポートしているので、それを用いて紹介されているようにワークフローを組む。


```yaml
name: Deploy
on:
  workflow_dispatch:
  push:
    branches:
      - develop

jobs:
  deploy:
    runs-on: ubuntu-latest
    permissions:
      id-token: write
    steps:
      - uses: actions/checkout@v3
        with:
          token: ${{ secrets.GCR_IMAGE_UPLOAD_GITHUB_TOKEN }}
      - id: auth
        uses: google-github-actions/auth@v0
        with:
          create_credentials_file: true
          workload_identity_provider: workload_identity_provider_name
          service_account: github-actions-deployer@$project_id.iam.gserviceaccount.com
      - name: Set up Cloud SDK
        uses: google-github-actions/setup-gcloud@v0

      - name: Login to GCP
        run: |
          gcloud auth login --brief --cred-file=${{ steps.auth.outputs.credentials_file_path }}
          gcloud auth configure-docker --quiet

      - name: Build image and push
        uses: docker/build-push-action@v2
        with:
          context: .
          push: true
          tags: gcr.io/project_id/image_name:latest
          platforms: linux/amd64
      - name: Deploy
        run: |
          gcloud run deploy app_name --image gcr.io/project_id/image_name --memory 128Mi --max-instances=1 --min-instances=1 --region=asia-northeast1
          ./bin/delete_unused_images
```

Container Registry では古いイメージは自動で消えずにタグ無しで生き続けるが、個人用途では不要。気付けばストレージ料金が積み上がるなと面白くなったため、アプリケーションをデプロイした後に以下のコマンドを実行しタグ無しのイメージを消している。

```bash
#!/usr/bin/env bash
for t in $(gcloud container images list-tags gcr.io/project_id/image_name --filter='-tags:*' --format="get(digest)"); do
  gcloud container images delete gcr.io/project_id/image_name@$t
done
```

{{< vertical_space >}}

Slack slash command はタイムアウトが 3 秒で設定されており、現状ではコールドスタートにするとぎりレスポンスが間に合わないため常にインスタンスを 1 つ立てている。Slack 側が exponential back-off の retry を 3 回してくれるので現実的には処理は継続されるだろうが、コールドスタート時に Slack 上に timeout のメッセージが表示されるのがだるいのと、リトライを強いるのが気が乗らなかった。個人用途なのでなんとかコールドスタートでも間に合うようにしたい。

任意の言語を利用できるし、タイムアウト秒数、同時実行数、自動スケーリング設定、CPU 割り当てをリクエストの処理中のみにできるなど便利すぎた。雑にその時々で興味のある実装を試してデプロイするみたいな遊び場になっているので、コンテナイメージを作り直してデプロイすれば動くのはありがたい。スケールするような規模でも試してみたくなった。

### Firestore

https://cloud.google.com/firestore

Twitter bot のツイート管理と副業の記録を期限付きで保存するために利用していた Heroku Redis は Cloud Firestore に変更。Redis でないといけない理由と Memorystore for Redis のお値段を天秤にかけた結果、無料枠で収まる Firestore にした。オブジェクトを雑に突っ込んでたせいで明示的にシリアライズ / デシリアライズを考慮したり、実質 order 考慮するロジックもあったので、移行したことで無理しなくなった。

{{< vertical_space >}}

### Secret Manager

https://cloud.google.com/secret-manager

秘匿情報のやりとりに利用。秘匿しなくて良いものは環境変数に入れた。

```shell
# 値の登録
echo -n 'token' | gcloud secrets create key_name --data-file=-
```

```ruby
# 値の参照
require 'google/cloud/secret_manager'

secret_manager_service = Google::Cloud::SecretManager.secret_manager_service
# key_name で登録された値を取得
path = secret_manager_service.secret_version_path project: PROJECT_ID, secret: key_name, secret_version: 'latest'
res = secret_manager_service.access_secret_version name: path
value = res.payload.data
```

{{< vertical_space >}}

### Cloud Scheduler

https://cloud.google.com/scheduler

Heroku Scheduler からの移行先にした。HTTP / PubSub / AppEngine HTTP を受け付けているので、rake task でやっていた処理を Web API 化して、そのエンドポイント (Cloud Run) にリクエストするように job を作成。

![](../schedule.png)
{{< caption "Twitter の定期投稿の job の例" >}}


Cloud Scheduler からリクエストする際に Auth ヘッダーに Google OAuth / OIDC トークンを付与できる。リクエストしたいエンドポイントは Cloud Scheduler 以外からは呼ばれたくないので、OIDC トークンを付与してもらい、リクエスト時に検証するようにした。

```ruby
# リクエストを受ける側での検証
post '/random_tweet' do
  require 'googleauth'
  # ...
  result = Google::Auth::IDTokens.verify_oidc(auth_token, aud: request.url)
  if result['email'] == ENV['SERVICE_ACCOUNT_EMAIL']
    # 正常時の処理
  end
  # ...
end
```

{{< vertical_space >}}

- - -

もともと Heroku では $7 だったのが、料金シミュレーターによると全て込みで $7.39。怖いので予算アラートは設定した。
