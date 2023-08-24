---
title: "6 年ものの Android アプリケーションに Google Play Instant 機能を入れた"
date: 2020-02-04T17:27:27+09:00
draft: false
tags:
  - Android
---

仕事で担当しているサービスでモジュール化がしがしやるかって時に、プロダクト的にも、モジュール分割と行った面で開発環境のテコ入れにも面白いなと思ったので Google Play Instant やっていた。気付けばちょっと経ってしまっていたので開発のステップをメモしておく。

## そもそも

Google Play Instant やりましょうよーと紹介したら、”へーこんなことできるんですね。やりましょう” みたいな流れになったのでやった。

プラットフォームの技術を追いかける立場でなければ、そもそも Play ストアで出会わないと気付けない機能だと思われるので、エンジニア主導で実例や期待できる効果を示しながら、デザイナーやディレクターを巻き込んで進めるのが良い。

Google Play Instant に関してや、実装に当たっての制約は Android Developers 公式を見るのが早い。

https://developer.android.com/topic/google-play-instant/getting-started/instant-enabled-app-bundle

https://developer.android.com/topic/google-play-instant/overview#reduce-size

## モジュール設計

起動画面という入口は同じで、あとは製品版と Google Play Instant でかっちり責務をモジュールで分ける というようにしている。それはそう…という感じですね。

6 年もののコードベースということで巨大な app module という事情はうちにもあったため、それを最小限にするために解体するというのも並行して行っており、以下のようになった。Dynamic Feature Module を DFM としています。


![](https://miro.medium.com/max/874/1*7ovPEYqRkg9Vd1FrGEz8fw.png)
{{< caption "モジュールの依存図 (矢印の先を依存関係として指定している)" >}}

AndroidManifest.xml 内で `xmlns:dist=”http://schemas.android.com/apk/distribution"` を指定しているものとする。

**app**: com.android.application module でアプリの設定やエントリポイントに必要な依存関係がいる。dist:instant="true" 指定。

**instantapp**: Google Play Instant のコンテンツを表示するためのコンポーネントや依存関係がいる。DFM で dist:instant="true" 指定。

**legacy**: 製品版のアプリを構成するコンポーネントや依存関係がいる。ここから機能単位で feature module として後々切り出されていく。dist:instant="false" と dist:onDemand="false" 指定。

**core**: common クラス、common UI component, resource といった共通コンポーネントがいる。

- - - 

実は以下に示す記事のように、巨大な app module から feature module を切り出していくぞという方針で元々動いていた。

https://matakucom.medium.com/minne-%E3%81%A8%E3%83%9E%E3%83%AB%E3%83%81%E3%83%A2%E3%82%B8%E3%83%A5%E3%83%BC%E3%83%AB-5a8c73b50096

しかし、少しずつ切り出していくと 4MB というサイズの制約を達成するのが想定より先になってしまうのが大いに予想できたため、急いで app module を最小限にし、その他を legacy module として押し込んだ。

app module を解体して app-legacy 構成にするにあたり、legacy の方が圧倒的にファイル数が多いため、legacy 側を引き剥がしていると時間がかかり、並行して進んでいる pull request とのコンフリクトの嵐になる。そのため一旦 app module であろうクラスたちを引き剥がし、その後一括でリネームするという手段をとった。

## APK の詳細を眺める

そもそも解体したとして、モジュール自体の APK のサイズどのくらいなんだろうというのを知る必要があったため、Android Studio の `Build -> Analyze APK...` にて眺めた。

```shell
# Build apks from aab
$ bundletool build-apks  \
      --bundle=/path/to/aab \
      --output=app.apks \
      --overwrite \
      --connected-device
$ unzip app.apks -d someDir
Archive:  app.apks
 extracting: someDir/splits/base-xxhdpi.apk
 extracting: someDir/splits/base-ja.apk
 extracting: someDir/splits/legacy-xxhdpi.apk
 extracting: someDir/splits/legacy-ja.apk
 ...
```

なにがどうなって構成されているのか？や重い部分といった情報が見られるし、階層をたどれば詳細が表示される。そもそも最小限にした app module にした時点で、その module の apk に対して調べてみるのが良い。

うちのサービスでは 3MB いかないくらいだったので行けそう となり進めた。

![](https://miro.medium.com/max/1190/1*nQmLp4lUJvKA6f0te9kBIg.png)
{{< caption "適当な個人アプリの apk で Analyze APK した様子" >}}

## 動作検証

ぴっと端末にインストールするにあたって、iaコマンドが便利。デフォルトではおそらく入っていないので、SDK tool を入れる

```shell
# Install tools for Google Play Instant
$ sdkmanager 'extras;google;instantapps'
# Build an aab
$ ./gradlew bundleDebug 

# Build apks from aab
$ bundletool build-apks  \
      --bundle=/path/to/aab \
      --output=app.apks \
      --overwrite \
      --connected-device 

$ $HOME/Library/Android/sdk/extras/google/instantapps/ia run app.apks
```

Android Studio 上でも `Run Configurations` から、独自の設定を作成しインストールするモジュールを指定すれば、インストールも可能。

- - - 

モジュールのダウンロードを実際に試すには、Play ストアの内部テスト版や Alpha 版等へ上げて検証する以外なさそう。良いのあったら教えて下さい。その際に気をつける点は以下くらい。

💁 Play ストアに公開しているパッケージしか上げられない  
💁 Internal App Sharing は未対応なので、バージョンかぶりができない

## ハマっ{た,ている}ところ

### instantapp (DFM) module 内の Data Binding クラスが参照できない

IDE 上でクラス参照及びプロパティ参照はできるけど、ビルド時に Data Binding クラスが参照できずに落ちる。これはminifyEnabled true にしておりクラスが難読化されていたためで、以下の設定を追加。

```shell
# DFM のパスを指定
-keep class com.mataku.app.instantapp.DataBinderMapperImpl { *; }
```

### `dist:onDemand="false"` にしても instantapp module が参照できない

起動画面から Google Play Instant 用の画面へ遷移する際に ClassNotFoundException でクラッシュするという端末があった。正直ここはよく分かっておらず、モジュールをダウンロードすると参照できるので一旦それで解決した。

モジュールのダウンロードには Play Core ライブラリを用いるので、以下のページにお世話になった。

https://developer.android.com/guide/playcore#monitor_requests

### SplitInstallSessionStatus

Status がいくつかあるが、必ずしもすべてのものをハンドリングしなくてはいけないことはない。

そもそもダウンロードするとき何が呼ばれるんだと思い、`SplitInstallStateUpdatedListener#onStateUpdated` を眺めていたら、成功時でも CANCELED を挟んだりしていた。終着点として基本的に INSTALLED / FAILED が来たら成功または失敗処理としてあれこれする、で良いと思われる。

## CD workflow への入れ込み

App Bundle さえビルドして Play ストアにアップロードすれば完了 という工程は楽。ただ、製品版未満のバージョンを指定する必要があるのと、毎回リリースするわけではないのでどうしようかなと思っている。

master ブランチへマージされたときに自動でリリースされるので、毎回何も考えず製品版と Google Play Instant 版をリリースしてしまえばいいんだけど、そもそも対応する Publishing API もまだなかったはずなのでそのときに考える、で着地した。

## おわりに

モジュール分割がめちゃインパクトがあるタスクだったが、ふりかえるとそれ以外はそんなに苦労していない。

DFM 関連であれこれ学ぶことやハマる点があったので、それを知る良い機会になった。検証周りはこれから Google さんがどんどん良くしていくだろうと信じている。

出してはみたものの流入 0 ならどうしようかな…と思っていたが、想定よりも流入があってよかった。


