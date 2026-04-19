package blog

object GistEmbedTransformer {
  private val GIST_LINK_REGEX =
    Regex(
      """<a href="(https://gist\.github\.com/[^/]+/[a-f0-9]+)"[^>]*>[^<]*</a>""",
    )
  private val GIST_RAW_URL_REGEX =
    Regex(
      """(?<![">])(https://gist\.github\.com/[^/]+/[a-f0-9]+)(?![a-f0-9/])""",
    )

  fun transform(html: String): String {
    var result = html
    result =
      GIST_LINK_REGEX.replace(result) { matchResult ->
        val url = matchResult.groupValues[1]
        """<script src="$url.js"></script>"""
      }
    result =
      GIST_RAW_URL_REGEX.replace(result) { matchResult ->
        val url = matchResult.groupValues[1]
        """<script src="$url.js"></script>"""
      }
    return result
  }
}
