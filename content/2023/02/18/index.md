---
title: "MiddleEllipsisText"
date: 2023-02-18T21:40:51+09:00
draft: false
tags:
  - 日常
  - Android
---

Compose の Text component でもかつての `android:ellipsize="middle"` がやりたくて、[https://github.com/mataku/MiddleEllipsisText]([https://github.com/mataku/MiddleEllipsisText) を作っている。正直公式が対応しそうなもんなので息は短いだろう (該当の Google での issue に動きはないが...) と思っているものの、作らないと僕の仕事が進まないのでやるしかなかった。

ただ割と作ってみると考えることは多くて、省略文字分のスペースを予め確保した上で、文字列の両端からシンプルにポインタを 1 つずつずらしていって 1 文字ずつ入れていき、画面幅から出たら終わりにしていた。ただ絵文字で表示崩れる報告があり、掘り下げて色々見ていくと 🇧🇪 や 👩‍👩‍👧‍👧 のように複数の Unicode コードポイントを持つ文字だと崩れるのでほう...となった。🇧🇪 は🇧 と 🇪 で構成されているので、雑にベルギーの国旗を reverse とかすると🇪🇧になる。なので、1 文字を構成するためのコードポイントをまとめて処理する必要があり、コードポイント周りの計算はバグを生む自信があったため ICU に頼った。

UI component だから久しぶりに androidTest 書くかと思い、初めて composable component のテストを書いたのも良かった。Acrivity や Fragment を意識しなくても screen のテストができるので、Android API で困ることが減る。作り上 Fragment で ComposeView を用いてラップしていたとしても、何らかの state holder を用いて stateless composable になっていれば、テストする際は親の Fragment 経由で起動しなくても十分なはず。Gradle Managed Devices でテストする端末をコードで管理するのも良かった。ライブラリのテストくらいなら少なくとも有用だし、アプリケーションレベルでは並列実行して安定するかや Robolectric を用いて実行した上で判断できると思う。

公式のテストに関するページ見たらすぐ書き始められるのも良かった。あとはお使いの CI のハードウェア次第。
https://developer.android.com/jetpack/compose/testing
