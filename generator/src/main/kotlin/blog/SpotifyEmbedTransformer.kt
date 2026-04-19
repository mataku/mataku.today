package blog

object SpotifyEmbedTransformer {
  private val URL_LINK_REGEX =
    Regex(
      """<a href="(https://open\.spotify\.com/(track|album|playlist|artist|episode)/([a-zA-Z0-9]+)[^"]*)"[^>]*>[^<]*</a>""",
    )
  private val URL_RAW_REGEX =
    Regex(
      """(?<![">])(https://open\.spotify\.com/(track|album|playlist|artist|episode)/([a-zA-Z0-9]+))(\?[^\s<]*)?""",
    )

  fun transform(html: String): String {
    var result = html
    result =
      URL_LINK_REGEX.replace(result) { matchResult ->
        val type = matchResult.groupValues[2]
        val id = matchResult.groupValues[3]
        createIframe(type, id)
      }
    result =
      URL_RAW_REGEX.replace(result) { matchResult ->
        val type = matchResult.groupValues[2]
        val id = matchResult.groupValues[3]
        createIframe(type, id)
      }
    return result
  }

  private fun createIframe(
    type: String,
    id: String,
  ): String =
    """<div class="spotify-embed spotify-$type"><iframe src="https://open.spotify.com/embed/$type/$id" frameborder="0" allow="autoplay; clipboard-write; encrypted-media; fullscreen; picture-in-picture" loading="lazy"></iframe></div>"""
}
