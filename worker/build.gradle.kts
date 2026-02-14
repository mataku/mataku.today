plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

repositories {
    mavenCentral()
}

kotlin {
    js {
        nodejs()
        binaries.executable()
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    sourceMap.set(false)
                    sourceMapEmbedSources.set(org.jetbrains.kotlin.gradle.dsl.JsSourceMapEmbedMode.SOURCE_MAP_SOURCE_CONTENT_NEVER)
                }
            }
        }
    }
}
