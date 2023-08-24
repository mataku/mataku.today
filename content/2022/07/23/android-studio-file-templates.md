---
title: Android Studio で使える file templates を作り直した
date: 2022-07-23T11:16:05+09:00
draft: false
tags:
  - 日常
  - Android
---

{{< ogp "https://matakucom.medium.com/%E4%BB%8A%E6%9B%B4%E3%81%A0%E3%81%91%E3%81%A9-android-studio-%E3%81%AE-%E3%82%AB%E3%82%B9%E3%82%BF%E3%83%A0%E3%83%86%E3%83%B3%E3%83%97%E3%83%AC%E3%83%BC%E3%83%88%E6%A9%9F%E8%83%BD%E3%81%8C%E4%BE%BF%E5%88%A9-74909951c4b2" >}}

ここから Android Studio も 7 系になったことで見事に使えなくなったカスタムテンプレートを作り直した。こうしたい。

- ViewModel のクラス名を入れたら `app/src/main/java/com/example/myapplication/{ViewModel class name}.kt` を作成
- Generate JUnit Test file を選択すると `app/src/test/java/com/example/myapplication/{ViewModel class name}Test.kt` を作成
- Generate kotest spec file を選択すると `app/src/test/java/com/example/myapplication/{ViewModel class name}Spec.kt` を作成

元々は template.xml と FreeMaker Template Language によるテンプレートを用意し、Android Studio の template 用のディレクトリにまるっと配置し、Android Studio を都度再起動する運用だったが、結局 template API に動きはなく Intellij plugin をインストールする方式になっていた。


- - - 

{{< vertical_space >}}

## Intellij plugin

以前言及していた thagikura さんのリポジトリを見ていてどういう構成になっているか読むかと思っていたものの、プラグイン作成用にテンプレートが用意されているので、README を見ていたら大まかな理解はできた。

https://github.com/JetBrains/intellij-platform-plugin-template

### plugin.xml

[src/main/resources/META-INF/plugin.xml](https://github.com/mataku/android-studio-file-templates/blob/0d3f0502418ef9bc3f6fa445c0ba7be2dba47c29/src/main/resources/META-INF/plugin.xml) がエントリポイントを読み取るところになっている。

今回はファイル作成ウィザード (Android Studio のメニューより New -> 下にある Activity/Fragment/Other など) を新規作成したいので、`com.android.tools.idea.wizard.template` のクラスを読み込むように設定。

### WizardTemplateProvider

[WizardTemplateProviderImpl.kt](https://github.com/mataku/android-studio-file-templates/blob/5ba10eb6c5a457ae6b03c9a315c6fdf69649643c/src/main/kotlin/other/WizardTemplateProviderImpl.kt) のように、`com.android.tools.idea.wizard.template.WizardTemplateProvider` を継承し読み込ませたい template を指定する。

### Template

どこにどう作るかの入れ物を作る。必要な Widget も用意されているので必要なものを詰め込んでいく。任意のテキストを入力できる TextFieldWidget, checkbox を用いて true/false の 2 値を取得できる CheckBoxWidget など色々用意されている。

![](https://raw.githubusercontent.com/mataku/android-studio-file-templates/main/screenshot/viewmodel_with_hilt.png)
{{< caption_mac "Android Studio での作成ウィザード" >}}

https://github.com/mataku/android-studio-file-templates/blob/f80d9aef90b7ad785809be3d5c194fe85dfbeceb/src/main/kotlin/other/viewmodel/ViewModelTemplate.kt

### Recipe

template を用いて作成されたウィザードにおいて、実際に入力された値を用いて処理を担う。上記の作成ウィザード例でいうと、Finish を押した際に実行される任意の処理。TemplateBuilder で template を組み立てる際に recipe として指定。

指定したモジュール名やクラス名から実際にファイル生成を行ったり、ファイル生成後にすぐ編集できるようにファイルを開いたりできる。任意の処理なので recipe を指定する際のブロックにある RecipeExecutor の拡張関数として定義しなくても良いんだけど、RecipeExecutor 内にある ファイル保存用の save や IDE 上でファイルを開く open といった便利関数を楽に利用できるので拡張関数として定義しておくのが良い。

<details>
<summary> ViewModelRecipe 抜粋</summary>

```kotlin
// template 
recipe = { data: TemplateData ->
    viewModelRecipe(
        moduleTemplateData = data as ModuleTemplateData,
        moduleName = moduleNameParam.value,
        packageName = packageNameParam.value,
        viewModelName = viewModelNameParam.value,
        junitTestFileRequired = junitParam.value,
        kotestSpecFileRequired = kotestParam.value
    )
}

// viewModelRecipe
fun RecipeExecutor.viewModelRecipe(
    moduleTemplateData: ModuleTemplateData,
    moduleName: String,
    packageName: String,
    viewModelName: String,
    junitTestFileRequired: Boolean,
    kotestSpecFileRequired: Boolean
) {
    val (projectTemplateData, _, _) = moduleTemplateData
    val mainSourcePath =
        "${projectTemplateData.rootDir.path}/${moduleName}/src/main/java/${packageName.slashedPackageName()}"
    val testSourcePath =
        "${projectTemplateData.rootDir.path}/${moduleName}/src/test/java/${packageName.slashedPackageName()}"

    val viewModelPath = "$mainSourcePath/${viewModelName}.kt"

    save(
        viewModel(
            packageName = packageName,
            viewModelName = viewModelName
        ),
        File(viewModelPath)
    )
    open(File(viewModelPath))
    if (junitTestFileRequired) {
        val viewModelTestPath = "$testSourcePath/${viewModelName}Test.kt"
        save(
            viewModelTest(
                packageName = packageName,
                viewModelName = viewModelName
            ),
            File(viewModelTestPath)
        )
        open(File(viewModelPath))
    }
    if (kotestSpecFileRequired) {
        val viewModelSpecPath = "$testSourcePath/${viewModelName}Spec.kt"
        save(
            viewModelSpec(
                packageName = packageName,
                viewModelName = viewModelName
            ),
            File(viewModelSpecPath)
        )
        open(File(viewModelSpecPath))
    }
}
```

実際に生成する文字列は以下のように用意。Dagger hilt を多くの場合で利用するので @inject constructor の表記であったり、kotest のテストクラスは DescribeSpec をよく使うのでデフォルトでつくようにしている。

https://github.com/mataku/android-studio-file-templates/blob/f80d9aef90b7ad785809be3d5c194fe85dfbeceb/src/main/kotlin/other/viewmodel/src/app_package/ViewModel.kt

https://github.com/mataku/android-studio-file-templates/blob/f80d9aef90b7ad785809be3d5c194fe85dfbeceb/src/main/kotlin/other/viewmodel/test/app_package/ViewModelSpec.kt

</details>

### Installation

個人用途だと性質上 Intellij Marketplace に公開するものでもないし、ローカルで jar ビルドして直接それをインストールする。

https://github.com/mataku/android-studio-file-templates#installation

### Debug

Gradle IntelliJ Plugin で用意されている runIde タスクを使う。`./gradlew runIde` を実行するとライブラリをビルドしインストールした上で、sandbox 環境の IDE が起動する。

実際にビルドした jar を直接普段利用するところにインストールしてもよいが、都度再起動が必要なので面倒。例えば Android Studio でライブラリの開発をしていた場合に、そこも一緒に再起動で巻き込まれてしまうので、runIde によって別インスタンスが起動される方が開発がスムーズ。

{{< vertical_space >}}

## 感想

ftl を用意してテンプレート用ディレクトリに配置する方法だと、僕が知る範囲ではデバッグが大変でつらい部分が目立っていた。Intellij plugin による方式だと sandbox 環境でログも出るデバッグ環境があるのと、普段 Android 開発をしている人が手を出しやすい言語である Kotlin (kts もあるが) で記述できるのは大きなメリット。最近 Web API 業が落ち着いて Android アプリ開発をするようになったのでこれを拡張していきたい気持ちはありつつ、公式での言及を見つけられないので切り捨ててくれるなよという気持ち。

とはいえどさどさ移行しようとしたらもう compose によって使わないものも多く、開発過程を見直す良い機会。


## 参考

Gradle IntelliJ Plugin - IntelliJ Platform Plugin SDK  
https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html

thagikura/AndroidTemplatePluginSample - GitHub  
https://github.com/thagikura/AndroidTemplatePluginSample
