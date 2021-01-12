---
title: "2021/01/12"
date: 2021-01-12T23:50:05+09:00
draft: false
tags: 
  - 日常
---

AWS Device Farm project に fastlane 経由でファイルアップロードをするための [fastlane プラグイン](https://github.com/mataku/fastlane-plugin-aws_device_farm_upload) を作った。

Android アプリで管理してる Fastfile に直書きされたタスクを、iOS アプリでもやるかと思って切り出したものの、AWS Device Farm 上の端末にて iOS アプリのプッシュ通知確認をするには、通常のリモートアクセスでは不可能で、プライベートデバイスが必要そう (試してないので未確認) ということで一旦積み。

切り出した事自体は Fastfile 肥大化防止に繋がるし、fastlane におけるプラグインの作り方は [Create your own fastlane plugin](https://docs.fastlane.tools/plugins/create-plugin/) を見ればわかるので実装面で困ることはなかったということでとんとん。
