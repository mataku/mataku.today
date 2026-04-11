---
title: "2026/04/11"
date: 2026-04-11T22:06:58+09:00
tags: 
---

[invertase/dart_custom_lint](https://github.com/invertase/dart_custom_lint) が archive されてて、analysis_server_plugin を使うようにする流れなのを知った。ちょうど design system に沿った新しい UI component を使うようにチェックする用の custom_lint を入れたばかりだったので感謝。

ここまで coding agent が浸透すると、iOS/Android どちらかにある程度詳しい前提のもと、ネイティブ開発での越境の方がハマりどころや考慮すべきところが少なくこだわりきれるんじゃないかと思うけど実践する環境がない。チーム外からの pull request がめっちゃ飛んでくる際に、レビュー工程をどう最適化するかが最近は悩みどころではあるので、ネイティブ開発にすると単純に pull request が倍になることでそこへの悩みが強くなりそう。

現状では無料で様々な instruction files を参照してレビューする Devin Review は良い指摘をくれる。こういった自動レビューをベースにして、ユニットテストは当たり前に書くとして、アーキテクチャやコーディング規約みたいなのは lint や自動レビューでカバーできるはずで、VRT を充実させていけば足りるのか、都度都度 CI で simulator セットアップしてテストするのは様々なコストがあるので、Firebase App Distribution の App Testing Agent みたいなのを充実させてリリース前に定期実行するとかちゃんと考えたいが、クライアントチーム人がいすぎてサーバーチームに異動になるみたいな話もあるので何もわからない。
