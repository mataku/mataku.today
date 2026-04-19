package blog

object JsonWriter {
  private fun escapeString(value: String): String {
    val sb = StringBuilder()
    for (ch in value) {
      when (ch) {
        '"' -> {
          sb.append("\\\"")
        }

        '\\' -> {
          sb.append("\\\\")
        }

        '\n' -> {
          sb.append("\\n")
        }

        '\r' -> {
          sb.append("\\r")
        }

        '\t' -> {
          sb.append("\\t")
        }

        '\b' -> {
          sb.append("\\b")
        }

        '\u000C' -> {
          sb.append("\\f")
        }

        else -> {
          if (ch.code < 0x20) {
            sb.append("\\u%04x".format(ch.code))
          } else {
            sb.append(ch)
          }
        }
      }
    }
    return sb.toString()
  }

  private fun buildJsonArray(items: List<String>): String {
    if (items.isEmpty()) return "[]"
    return items.joinToString(",", "[", "]") { "\"${escapeString(it)}\"" }
  }

  fun buildArticlesJson(articles: List<Map<String, Any>>): String {
    val entries =
      articles.joinToString(",\n  ") { article ->
        val title = escapeString((article["title"] as? String) ?: "")
        val date = escapeString((article["date"] as? String) ?: "")
        val path = escapeString((article["path"] as? String) ?: "")

        @Suppress("UNCHECKED_CAST")
        val tags = buildJsonArray((article["tags"] as? List<String>) ?: emptyList())
        """{"title":"$title","date":"$date","path":"$path","tags":$tags}"""
      }
    return "[\n  $entries\n]"
  }
}
