plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.commonmark)
    implementation(libs.commonmark.ext.gfm.tables)
    implementation(libs.commonmark.ext.gfm.strikethrough)
    implementation(libs.commonmark.ext.autolink)
    implementation(libs.snakeyaml.engine)
}

application {
    mainClass.set("blog.MainKt")
}

tasks.named<JavaExec>("run") {
    workingDir = rootProject.projectDir
}

tasks.register<JavaExec>("new") {
    group = "application"
    description = "Create a new article markdown file"
    mainClass.set("blog.ArticleCreator")
    classpath = sourceSets["main"].runtimeClasspath
    workingDir = rootProject.projectDir
}

tasks.register<JavaExec>("feed") {
    group = "application"
    description = "Generate RSS feed (feed.xml)"
    mainClass.set("blog.FeedGenerator")
    classpath = sourceSets["main"].runtimeClasspath
    workingDir = rootProject.projectDir
}
