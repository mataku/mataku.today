---
title: Google Play Console の審査中のステータスを機械的に取りたかった
date: 2024-02-19T15:51:35+09:00
draft: false
tags:
  - 日常
---

Google Play Console でアップデートを公開する際に、アップデートの審査中及び完了のステータスをどうにかして機械的に取りたいが無理だった。

Google Play Developer API では https://developers.google.com/android-publisher/api-ref/rest/v3/edits.tracks というように、内部テスト版から製品版までステータスを取得できるエンドポイントがあるので、それ使って以下のように考えていた。

1. edits.insert
2. edits.tracks.list で Track 一覧を持ってきて、track: production でフィルターして製品版を取ってくる
3. 得られたレスポンス内の release の status を見る

ただ https://developers.google.com/android-publisher/api-ref/rest/v3/edits.tracks#status の記載にもあるように、審査中や公開準備完了のステータスはなく、あくまでバイナリの配信状況のみをお知らせしているのを知る。うちでは審査が通った後公開のタイミングをコントロールしたく手動公開設定にしており、審査が通ったかどうかを手動でポーリングしているのをどうにかしたかった。[https://issuetracker.google.com/issues/179708468](https://issuetracker.google.com/issues/179708468) も見つけたので +1 しておいたが、担当者がレイオフされてないことを祈るしかできない。
