---
title: 2023/08/25
date: 2023-08-25T14:35:39+09:00
draft: false
tags:
  - 日常
---

mataku.today ドメインを Cloudflare に移したので、ついでにページ自体も Netlify から Cloudflare Pages に移した。GitHub (と GitLab) 連携もあるし、様々なフレームワークを網羅したガイドもあったので困らず。

https://developers.cloudflare.com/pages/framework-guides/

Hugo で静的に HTML を生成してそれをデプロイしており、そのビルドの際に外部サイトの OGP 用の HTML 生成用に JSON を返す Web API を用意して使っていた。(例: [https://mataku.today/2022/10/31/](https://mataku.today/2022/10/31/))

そのエンドポイントは Netlify Function で用意していたので、Cloudflare Pages Functions に移した。Cloudflare Pages Functions は Netlify と同じようデフォルトで functions 以下のディレクトリにあるファイル名をエンドポイントの名前空間として利用する。例えば `functions/ogp.ts` でちゃんと実装すると `$domainName/ogp` が生成されるし、大体どこもこんな機能あるんだなと知った。

https://developers.cloudflare.com/pages/platform/functions/routing/

めんどいので Hugo のリポジトリと同じところに置いているけれども、public repository で作った場合エンドポイントが少なくともばれるので、mataku.today では封じている。
