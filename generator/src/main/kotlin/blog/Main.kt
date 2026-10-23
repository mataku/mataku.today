package blog

fun main() {
  Generator().run()
  IndexPageGenerator.generate()
  FeedGenerator.main(emptyArray())
}
