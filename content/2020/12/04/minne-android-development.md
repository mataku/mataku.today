---
title: "2022/08/19"
date: 2020-12-04T00:18:00+09:00
draft: false
tags:
  - Android
---

普段は minne Android アプリに関わるところでがしがし開発しているので、僕からは開発基盤における改善をお送りします。


## 目次
1. MVP から MVVM パターンへの移行
1. Rx{Java/Android} を LiveData と Kotlin Coroutines へ
1. GitHub Actions

## MVP から MVVM パターンへの移行
もともと minne では創成期の無秩序の状態 (UI クラスにビジネス/プレゼンテーションロジックが記述されている状態) からしばらくして、 MVP (Model-View-Presenter) パターンが導入され、責務をきちんと定義してユニットテストを記述していくことで堅牢性を高めるという方針ができました。ただ、近年の Android Architecture Components の登場により、そのコンポーネントを取り入れて作っていくのが標準となりつつあります。

後述しますが、minne では Presenter から UI へデータを反映するために Rx のストリームを利用しており、画面のライフサイクルに対応して処理をキャンセルするようにストリームへ作用させる処理機構を独自でメンテナンスしている、という事情もありました。そのため、ライフサイクルへのサポートがライブラリ側でされているコンポーネントを利用することでメンテナンスコストも減るだろうと見込み、ViewModel と LiveData の導入を行っています。

```kotlin
class SampleViewModel(
    private val hogeRepository: HogeRepository
) : ViewModel() {
    
    // UI は原則 1 LiveData を購読し、必要なデータクラスを定義する
    data class SampleModel(
        val users: List<User> = emptyList(),
        val throwable: Thrpwable? = null,
        val loadingState: LoadingState // ローディング状態 (初回やページネーションなどを識別できるようにする)
    )
    
    // 購読対象の LiveData
    val sampleModelLiveData = MutableLiveData<SampleModel>()
    
    // UI から呼ばれるデータ取得などのメソッド
    fun fetchUser() {
        viewModelScope.launch {
            val result = hogeRepository.fetch()
            sampleModelLiveData.postValue(
                // ...
            )
        }
    }
```

minne では Web API として RESTful な構成を主に取っているので、1 画面を構成するのにあたり、複数の HTTP リクエストを行う場合があります。そのリクエスト毎に LiveData を用意すると UI 側での購読処理が複数に渡るため、複雑になることが予想できました。そのため、基本的に 1 UI が購読するのは 1 LiveData にしています。今後 GraphQL も拡大していく予定で、その際にも特に問題なく 1 LiveData で購読処理を行えると考えます。

複数リクエスト時にデータを束ねる実装は DroidKaigi/conference-app-2020 を参考にさせていただいてます。


## Rx{Java/Android} を LiveData と Kotlin Coroutines へ
minne では RxJava と RxAndroid にお世話になっていて、役割は以下です。

スレッド切り替え (UI/IO)
主に HTTP リクエストのレスポンスハンドリングにおけるストリーム処理 (Observable/Single)
ViewModel を利用するときに、UI へのデータのバインドにおいて Rx のストリームをそのまま利用するか、LiveData を取り入れるか悩みました。先述した画面のライフサイクルの管理を始めとするメンテナンスコストや、Rx の機能をそこまで使い切れていないことを理由に、MVVM 構成にするにあたり、Kotlin Coroutines と LiveData を導入することでそれぞれの責務を担ってもらうようにしました。

Rx 自体は特に悪くなく、様々な言語実装を通じて今でも開発が活発にされており、個人的にも好きです。ですが、適切な責務が分かれているとバージョンアップにおける影響範囲も狭くなるため追従しやすいというのと、Rx 職人もしくはサーバーサイドや他事業部でも幅広く Rx が使われているという状況があれば後押しになったかなとふりかえります。

## GitHub Actions

普段コードや文書の管理で用いている GitHub Enterprise (Server) でも GitHub Actions が利用できるようになったため、セルフホストマシン上で様々なワークフローを動かしています。minne の Android アプリを構成するプロジェクトでも同様に、日々様々なワークフローにお世話になっています。

セルフホストマシンによるランナー利用だと以下の方法が (少なくとも) あるでしょう。

マシン上でビルドできるように依存関係をインストールした上でワークフローを動かす
マシン上でワークフロー用のコンテナを用意し、コンテナ上でワークフローを動かす
minne では専用の Docker コンテナを用意して、その中でワークフローを動かすようにしています。

マシン上で apt-getなどで依存パッケージをセットアップするようにしてもいいんですが、そのマシン上で複数のワークフローが動くような環境である場合、その他のプロセスによって何かパッケージのアップデートやら何かが起こった場合に全体に影響が及びます。想定している環境で動くのを担保したかったので、Docker コンテナ上でワークフローを動かすようにしています。その元となるコンテナはざっと以下のようなものです。

```Dockerfile
# キャッシュ生成用
ARG RUBY_VERSION
FROM ubuntu:bionic AS cache-builder

ENV ANDROID_HOME=/usr/local/android-sdk-linux
ENV JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
ENV APP_ROOT /app

# ...
# Install and setup Android SDK
# ...

# プロジェクトごとコピー
COPY ./ $APP_ROOT/
# タスクを空回しして依存パッケージのキャッシュを生成
RUN ./gradlew buildDebug testDebug


FROM rubylang/ruby:$RUBY_VERSION-bionic

# ...
# Install and setup Android SDK
# ...

RUN mkdir -p /root/.gradle/caches /root/app/
WORKDIR /root/
COPY --from=cache-builder /root/.gradle/caches ./.gradle/caches/
```

マルチステージビルドを利用して、キャッシュ生成用コンテナを作り、CI で利用するコンテナではそのキャッシュが使える状態にしています。

./gradlew build{variantName} と ./gradlew test{variantName}タスクを空回しし、CI 用で使うコンテナにプロジェクトを構成する依存パッケージの gradle キャッシュだけコピーして持ってきています。単純に依存パッケージのダウンロード分稼げるので、3 分程ワークフローにかかる時間が短縮されます。

というのも、単純に actions/cache は GitHub Enterprise ではまだ使えない認識なので何かしらのキャッシュ機構を考える必要があります。どこか S3 等に置くならブランチごとに運用したいからハッシュで管理したり、キャッシュダウンロード時のネットワーク利用 (圧縮すればそんなに気にならないとは思います) など考えることが多かったため、Docker image に入れ込み定期更新する、という選択をしています。

github.com でホストされているならば、素直に actions/setup-java と actions/cache を使うのが無難でしょう。

```yaml
name: Unit test

on: [push]

jobs:
  unit_test:
    runs-on: [ self-hosted ]
    container:
      image: privateimage:minne/android/android-sdk:latest
    steps:
    - uses: actions/checkout@v2
    - name: unit test
      run: |
        export GRADLE_OPTS="-Xmx2048m -Xms256m -XX:MaxPermSize=256m -XX:PermSize=256m"
        export build_path=$(find $ANDROID_HOME/build-tools -maxdepth 1 | sort | awk 'END{print $NF}')
        export PATH=$PATH:$build_path
        bundle exec fastlane android test
```

## おわりに
昨年までの様々な自動化の導入やマルチモジュール導入などに続いて、minne の Android アプリの開発基盤が 2020 年にどう変化していたのかを紹介しました。来年もまとまった情報をお届けできるように頑張っていきたいと思います。
