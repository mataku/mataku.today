---
title: TextOverflow.StartEllipsis と TextOverflow.MiddleEllipsis が Compose 1.8.0-alpha02 からサポートされていた
date: 2024-11-04T17:22:31+09:00
draft: false
tags:
  - 日常
---

https://issuetracker.google.com/issues/185418980

```kotlin
Text(
  text = soLongText,
  modifier = Modifier,
  overflow = TextOverflow.MiddleEllipsis,
  maxLines = 1 // 1 行の場合
)
```

何もカスタマイズしなくて良いなら https://github.com/mataku/MiddleEllipsisText みたいな対応が不要になっていたので、ありがたく README を更新。従来の XML-based layout に比べれば存在しないコンポーネントも多々あるけど、いろんな Layout を駆使することで組めなくはないのでなんだかんだやっていける仕組みはあるんだよな。
