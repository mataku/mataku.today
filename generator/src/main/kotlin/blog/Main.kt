package blog

fun main() {
    val isDev = System.getenv("DEV") == "1"
    Generator().run()
    IndexPageGenerator.generate()
    if (!isDev) {
        FeedGenerator.main(emptyArray())
    }
}
