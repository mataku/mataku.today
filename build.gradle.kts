// Do not declare plugins here, not even with `apply false`.
// Renovate updates the wrapper with `./gradlew :wrapper` under configure-on-demand, which configures only this root project.
// Any plugin declared here makes that run resolve the Kotlin Gradle plugin from a cold cache, about 45 requests to Maven Central, which fails with 429 on the shared Mend runners.
// The subprojects still share one Kotlin plugin classloader as long as their plugin classpaths stay identical, so keep both on the same Kotlin version.
