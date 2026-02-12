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
                }
            }
        }
    }
}
