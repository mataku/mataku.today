---
title: "2021/02/26"
date: 2021-02-26T23:33:21+09:00
draft: false
tags:
  - Android
  - 技術
---

apollo-android + OkHttp を用いたリクエスト時に、アクセストークンの再取得を挟んだ上でリクエストをリトライしたい場合、その処理を ApolloInterceptor と OkHttp Interceptor のどちらにやってもらうか悩んでたけど、ApolloInterceptor にした。

apollo-android と OkHttp を比べた上で、ライブラリのレイヤーは若干違えど今後差し替わる可能性を踏まえると OkHttp にのっておく方が安心なはず。だけど GraphQL がいる限り apollo-android は使い続けるので依存してもいい、GraphQL のエラーハンドリングにおけるロジック構築は apollo-android にのるほうが楽、apollo-android の Response の中身を見る制約に one-shot はないので終端に至るまで変な考慮をしなくて良い、くらいの背景で決めた。

まあまあ apollo-android の事例がないので、世の中本当に GraphQL 使っとるんか? みたいな感じで書いてた。
