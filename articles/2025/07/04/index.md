---
title: 2025/07/04
date: 2025-07-04T13:20:08+09:00
draft: false
tags:
  - 日常
---

やっとの思いで Jenkins から self-hosted GitHub Actions runner に移ることになったと思ったら、GitHub Actions の実行が許可されていない GitHub Enterprise Server に移ることになり、Jenkins を社内でホストすることになった。大体のタスクは fastlane で管理しているので fastlane さえ動くようにセットアップしておけば良いが...

fastlane も真面目にライブラリや開発環境のアップデートをしていると壊れるのを実感できるので修正を送ったりしているが、最近は iOS アプリ周りの修正がちょいちょいマージされるだけでどうも活発ではないので、fastlane に依存し続けるべきかを最近は考えている。GitHub Actions に移行したならしばらくはその環境で使い続けるだろうからそのアクションに依存するのはありかなと思っていた。

Ruby 3.4 では動かない様子
https://github.com/fastlane/fastlane/issues/29183
https://github.com/fastlane/fastlane/pull/29184

メンテナンス体制に課題があるようで watch しているが動きがない
https://github.com/MobileNativeFoundation/discussions/discussions/194
