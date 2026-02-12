---
title: "2022/08/24"
date: 2022-08-24T16:56:53+09:00
draft: false
tags:
  - 日常
  - 髪
---

https://github.com/mataku/SunsetScrob/pull/233

Gradle の composite build 便利。ひたすらコピペする作業がなくなった。buildSrc に突っ込む方法を取ると楽は楽だけど、コード変更時の build cache の扱いが最高にセンシティブなので、このくらいなら includeBuild する方式で良い。現実的には build cache のおかげでそこまでビルドコストもないんじゃないかと考えている。

この方法ではなく `${project.rootDir}/build.gradle` に subprojects ブロック作ってそこに集約する手もなくはない。ただそれだと暗黙的なのと、対応する module の build.gradle{.kts} を見に行くと設定がすべて分かるほうがメンテナンスしやすいかなと考え、明示的に利用するものを指定できる方式にした。
