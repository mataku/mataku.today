---
title: "2021/07/28"
date: 2021-07-28T16:28:05+09:00
draft: true
tags:
  - Android
---

## アプリのビルド周り

### インストール

`./gradlew installDebug` とかで良かったんだけど、複数端末をつないでいる状態が多々あり、そのたびに端末指定するのがめんどいので、端末指定でインストールできるようにした。

https://github.com/mataku/dotfiles/blob/develop/fish/functions/adb-install.fish

### アンインストール

https://github.com/mataku/dotfiles/blob/develop/fish/functions/adb-uninstall.fish

### アプリ起動

https://github.com/mataku/dotfiles/blob/develop/fish/functions/adb-open.fish

## Android Emulator

Android SDK の `avdmanager` と `cmdline-tools` にある emulator をラップしたコマンドを用いている。

https://github.com/mataku/dotfiles/blob/develop/fish/functions/emu.fish

## デバッグ

Logcat を通じて android.util.Log.d や Timber を用いた print デバッグ, OkHttp のログを見ることが多い。[JakeWharton/pidcat](https://github.com/JakeWharton/pidcat) を用いている。

ブレークポイントを用いたいならば、Android Studio でやるのが無難そう。ただ `adb shell am set-debug-app packageName` のコマンドでデバッガ待ちにはできるので、Android Studio 上で `Attach Debugger to Android Process` を実行すれば設定したブレークポイントが反応すると思うが、そこまでするなら IDE 上でやるのが楽という気持ちがある。

## スクリーンショット

`adb shell -p screencap` してファイルを持ってくるラッパーを用いている。

https://github.com/mataku/dotfiles/blob/develop/fish/functions/adb-screenshot.fish

## 画面録画

`adb shell screenrecord` は `--time-limit` (デフォルトで 180秒) で指定した時間を超えない限りは終了シグナル待ち状態になる。録画されたデータは端末に保存されるため、コマンド一発で終わらせるならば終了シグナルをフックして `adb pull` を実行する必要がある。

https://github.com/mataku/dotfiles/blob/develop/fish/functions/adb-screenrecord.fish
