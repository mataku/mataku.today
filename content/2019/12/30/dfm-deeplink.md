---
title: "Dynamic Feature Module 構成でDeepLink を利用した画面遷移でアプリの選択が常にされる"
date: 2019-12-30T17:35:18+09:00
draft: false
tags:
  - Android
  - 技術
---

既存のアプリを Dynamic Feature Module 化していたら、DeepLink のように Intent.ActionView にてアプリ内の画面遷移をしている部分でアプリ選択の chooser が常に表示されてしまう現象に遭遇した。Android Gradle Plugin 3.5.X で確認している。

https://issuetracker.google.com/issues/123857941

ちょい前に Issue も上がってるようだけど、いまいち不具合なのかそういうもんなのかというのが判断できてない (不具合な感じがするので閉じられていない と勝手に思っている) のでこちら側で対策せざるを得なかった。

** 1. Intent に packageName と className を明示的に付与する **

```kotlin
intent.setPackage("com.mataku.amazingapp")
intent.setClassName(
    "com.mataku.amazingapp",
    "com.mataku.amazingapp.AmazingActivity
)
```

明示的に付与すると正常に画面遷移するのを確認した。ただ将来的に画面が追加されることを踏まえると、ホワイトリスト方式で URL を管理する必要があるし、DeepLink 用にあれこれ頑張らないといけないのでつらい。

** 2. Universal APK にする **

Dynamic Feature Module や Android App Bundle の恩恵を受けられないがこの問題は回避できる。この事象を年末に遭遇していたのもあり一旦こちらの方法で対処し、1 で頑張るというようにした。ただ、Android App Bundle にできないというのはデメリットが多すぎるので恒久対応にはできない。

** 3. Android Gradle Plugin のアップデートを待つ **

うーんという感じではあるし、本当に関係しているか分からないので何とも言えない..

DFM にしている構成で dist:onDemand=”false” にしていようと、App Bundle -> APK にして検証しないと分からない。ただ App Bundle が配布しやすい環境が Play Store に上げるくらいしかないので、正直他の会社の皆様がどのように検証しているのかが本当に最近気になっている。

現状見ているアプリたちは開発用の ID の suffix に .dev を付与しており、ストアに公開もしていないため、検証端末に APK を配布するのが最近は手間になってきている。2020 年はそれがやりやすくなるといいな。

