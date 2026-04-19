package blog

data class Article(
  val metadata: Map<String, String>,
  val content: String,
  val tags: List<String> = emptyList(),
)
