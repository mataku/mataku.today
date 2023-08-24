---
title: Firebase App Distribution (Beta) へのアップロードを Android apk で試す
date: 2020-07-21T17:11:58+09:00
draft: false
tags:
  - Android
  - Firebase App Distribution
---

リリース前に動作確認したいのでアプリを配布したい、という場合に DeployGate をずっと用いていたが、Firebase が提供している Firebase App Distribution もさわり心地特に問題なかったので、どうアップロードを自動化できるかというのを調べた。

https://firebase.google.com/docs/app-distribution

CI で Workflow 組もうと思ったら 2020/07 時点では権限周りが少しめんどかったのでメモ。
CI サービスで動かそうとしているので、属人化を防ぐためにサービスアカウントに権限付与し、そのアカウントで認証して Firebase CLI を使うのが良い。Fastlane に乗る。

## サービスアカウントの作成

該当の Firebase project 配下にサービスアカウントを作成し、秘密鍵 (JSON) をダウンロードしてどこかに配置しておく。

![](https://cdn-images-1.medium.com/max/1360/1*v5S5GHvh6dDepc5UbLl3qw.png)
{{< caption "Google Cloud Platform の console でのサービスアカウント作成画面" >}}

## Firebase App Distribution 用の権限付与
Firebase の権限に関してはこちら。App Distribution に関してはベータ版なので、今後変更がある可能性がある。

https://firebase.google.com/docs/projects/iam/permissions

適当にオーナー権限つけるわけにもいかんので、コードを読む。

https://github.com/fastlane/fastlane-plugin-firebase_app_distribution/blob/master/lib/fastlane/plugin/firebase_app_distribution/actions/firebase_app_distribution_action.rb

firebase_app_distribution plugin のコードを見ると、firebase CLI のラッパーであり、firebase appdistribution:distribute コマンドを実行している。

https://github.com/firebase/firebase-tools/blob/42881bd56e9b5b57d15db270a656a6afee595f5d/src/commands/appdistribution-distribute.ts

`appdistribution:distribute` 内を覗くと Firebase App Distribution 以外のリソースへの権限は不要そう。アプリの情報の取得と、バイナリのアップロードのために以下の権限を付与するので十分。

- firebaseappdistro.releases.list
- firebaseappdistro.releases.update

ちなみに、2020/07/20 現在ではバイナリアップロードのみに適したロールはデフォルトでは用意されてなかった。
ロールから Firebase App Distribution で検索すると、デフォルトで Firebase App Distribution 管理者 というのが用意されている。名前だけ見てこれでいいじゃんと思ったけど、読み取り専用。Firebase 品質管理者というのもいたけど、App Distribution 以外の権限もあったため、専用のカスタムロールを作成した。

![](https://cdn-images-1.medium.com/max/1360/1*l8WYFv90UhxkqguWv-M6YQ.png)
{{< caption "Firebase App Distribution へのバイナリアップロード専用のカスタムロール" >}}

## Fastlane に乗せる

Firebase 自体のページにご丁寧に載っている。

https://firebase.google.com/docs/app-distribution/android/distribute-fastlane?apptype=apk

Firebase CLI が必要なので、以下を参考にインストールするステップを Fastlane のタスク実行前に入れる。CI で用いるマシンや docker image には node が入っていない場合もあると思うが、standalone binary も用意されていて便利。

https://firebase.google.com/docs/cli#install_the_firebase_cli

Firebase の認証はサービスアカウントを用いて行うので、以下のように予め環境変数を設定しておく。

```shell
export GOOGLE_APPLICATION_CREDENTIALS="/path/to/service_account.json"
```

Fastfile もこのようになるだろう。


```
# Fastfile
lane :upload_apk_to_firebase_app_distribution do
  gradle(task: 'assembleDebug')
  firebase_app_distribution(
    app: "firebase_app_id",
    apk_path: "/path/to/apk",
    release_notes: 'release note',
    groups: 'tester-group, tester2-group'
  )
end
```

## おわりに

App Tester アプリのおかげで比較的配布も楽な印象。
Play Console の Internal App Sharing は App Bundle をアップロードできるが、ストアに公開されているアプリしかアップロードできない。App Distribution はストアに公開してないアプリもアップロードできるが、App Bundle をアップロードできない。両方できるサービスが待ち望まれている。

- - - 

2021/10/26 追記

Firebase App Distribution でも Android App Bundle が利用できるようになっていた 感謝
https://firebase.googleblog.com/2021/05/app-distribution-adds-support-to-android-app-bundles.html

