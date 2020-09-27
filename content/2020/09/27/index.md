---
title: "Android Studio 4.2 Canary 8 が起動するようになった"
date: 2020-09-27T23:48:58+09:00
draft: false
tags: 
  - Android
---

4.2 Canary 8 にしてから、以下のエラーメッセージを放置していたが重い腰を上げて直した。

> Missing essential plugin:
>
>  org.jetbrains.android
>
> Please reinstall Android Studio from scratch.

{{< tweet 1310190187406786561 >}}

Android Studio のリリースノートで言及されている vmoption は上のエラーメッセージの対象ではなかったので、ひたすらそれっぽい設定ファイルを探しては中身見て判断というのを 5 分くらい試していた。Kotlin プラグイン無効になったら何もできないんだなと改めて基幹的存在になっていることを認識。いつのタイミングでこの設定ファイルに出力されてたのかはわからん。

Compose 利用しているプロジェクトを開こうとするときに、大体 4.2 の新しいバージョンを使っていることが多いので助かった。
