---
title: 2023/08/25
date: 2023-08-25T14:35:39+09:00
draft: false
tags:
  - 日常
---

mataku.today ドメインを Cloudflare に移したので、ついでにアプリケーション自体も Netlify から Cloudflare Pages に移した。GitHub (と GitLab) 連携もあるし、様々なフレームワークを網羅したガイドもあったので困らず。

https://developers.cloudflare.com/pages/framework-guides/

アプリケーションは以下の流れでデプロイされる。

0. Hugo のビルド用コマンドを Cloudflare dashboard で設定
1. デプロイフックする git branch に push されたら Cloudflare がデプロイ処理を開始
2. Hugo の OGP 用のページのビルド時に指定したエンドポイントにリクエストし、結果をパースして HTML 化
3. 生成された public ディレクトリ配下のページをデプロイ

Hugo で静的に HTML を生成してそれをデプロイしており、そのビルドの際に外部サイトの OGP 用の HTML 生成用に JSON を返す Web API を使っていた。(例: [https://mataku.today/2022/10/31/](https://mataku.today/2022/10/31/))

そのエンドポイントは Netlify Function で用意していたので、Cloudflare Pages Function で用意した。Cloudflare Pages Function は Netlify と同じようにデフォルトで functions 以下のディレクトリにあるファイル名をエンドポイントの名前空間として利用する。例えば `functions/ogp.ts` でちゃんと実装すると `$domainName/ogp` が生成される。

https://developers.cloudflare.com/pages/platform/functions/routing/

めんどいので Hugo のリポジトリと同じところに置いているけれども、public repository で作った場合エンドポイントが少なくともばれるので、mataku.today では頑張って封じている。
