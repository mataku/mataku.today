---
title: "無限 HorizontalPager"
date: 2023-01-06T22:37:18+09:00
draft: false
tags:
  - 日常
  - Android
  - 技術
---

Jetpack Compose で横スワイプで画像切り替えのような、カルーセルで画像表示したい場合には Accompanist の HorizontalPager が便利。

無限カルーセルが必要になり、[accompanist リポジトリのサンプル](https://github.com/google/accompanist/blob/b823877ed2c42836a2a695d5dd3730ee7d23d6bc/sample/src/main/java/com/google/accompanist/sample/pager/HorizontalPagerLoopingSample.kt)にあるように `Int.MAX_VALUE` (かできるだけ大きな値) 個の要素を持つ Pager を用意して、余り計算をして対象の要素を算出するみたいにするのも良かったけど、いつだかの ViewPager2 のように端にスクロール用の要素をおいてサイレントスクロールするようにした。

{{< vertical_space >}}

{{< figure src="./horizontal_pager.gif" title="3 つの要素を持つリストによる動作サンプル (ポジションの遷移付き)" width=50% >}}



```kotlin
// carousel display of components with an image and a title arranged in Columns
@Composable
fun LoopingHorizontalBannerPager(
  modifier: Modifier = Modifier,
  bannerList: List<Banner>,
) {
  val scrollableList = if (bannerList.size > 1) {
    listOf(bannerList.last()) + bannerList + listOf(bannerList.first())
  } else {
    bannerList
  }

  // starts from the middle of the list
  val pagerState = rememberPagerState(initialPage = scrollableList.size / 2)
  HorizontalPager(
    state = pagerState,
    count = scrollableList.size,
    modifier = modifier,
    content = { index ->
      val banner = scrollableList[index]
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Image(
          painter = painterResource(id = banner.resId),
          contentDescription = "banner image",
          modifier = Modifier
            .fillMaxWidth(),
          contentScale = ContentScale.Crop,
        )
        Text(
          text = banner.title,
          fontSize = 16.sp,
          modifier = Modifier
            .padding(16.dp),
        )
      }
    }
  )
  if (pagerState.currentPage == 0 && !pagerState.isScrollInProgress) {
    LaunchedEffect(Unit) {
      pagerState.scrollToPage(scrollableList.lastIndex - 1)
    }
  }

  if (pagerState.currentPage == scrollableList.lastIndex && !pagerState.isScrollInProgress) {
    LaunchedEffect(Unit) {
      pagerState.scrollToPage(1)
    }
  }
}
```

そのまま要素を並べると行き止まるので、左端にリストの最後の要素、右端にリストの最初の要素を付与したリストを新しく作成 (例: 1,2,3 -> 3,1,2,3,1) し、その端に到達したら本来のリストの位置に行くように裏で位置移動する。

位置移動する際に [PagerState#animateScrollToPage](https://google.github.io/accompanist/api/pager/com.google.accompanist.pager/-pager-state/animate-scroll-to-page.html) を使うと実際にスワイプしたようにエフェクトがのるので、[Pager#scrollToPage](https://google.github.io/accompanist/api/pager/com.google.accompanist.pager/-pager-state/scroll-to-page.html) を使う。

また端に到達した際に裏で移動する場合、PagerState#isScrollInProgress (内部では [LazyListState#isScrollInProgress](https://developer.android.com/reference/kotlin/androidx/compose/foundation/lazy/LazyListState#isScrollInProgress())) を参考にスクロール中でない場合に実行しないと、ユーザのスクロール中に走ってしまいキャンセルされてしまう。


- - -

いつだか ViewPager2 でやったようなのを思い出しながら組んだが、まるで当時を何も思い出せなかった。バグってなかったらこれで良いかなと思いつつ、これ内部で表示するリストを加工しているので、PagerScope から渡される page を元にした Pager 用の composable component を外から突っ込めず汎用的な component として切り出せないのが良くない。

サンプルにあるような Pager の count を `Int.MAX_VALUE` にしようとも LazyListState を使っているので、表示上のパフォーマンスは軽く見たところ問題にならないはず。どっちがメンテしやすいかを考えている。


コードは https://github.com/mataku/composable-snippets/blob/34322eb/app/src/main/java/com/mataku/jetpackcomposesandbox/ui/compose/component/LoopingHorizontalPager.kt

## 参考

Horizontal Pager - Accompanist  
https://google.github.io/accompanist/pager/#horizontalpager

google/accompanist - GitHub  
https://github.com/google/accompanist
