package blog

object XEmbedTransformer {
    private val X_LINK_REGEX = Regex(
        """<a href="(https://x\.com/[^/]+/status/\d+)">[^<]*</a>"""
    )
    private val X_RAW_URL_REGEX = Regex(
        """(?<![">])(https://x\.com/[^/]+/status/\d+)"""
    )

    private fun convertToTwitterUrl(xUrl: String): String {
        return xUrl.replace("https://x.com/", "https://twitter.com/")
    }

    fun transform(html: String): TransformResult {
        var hasEmbed = false
        var result = X_LINK_REGEX.replace(html) { match ->
            hasEmbed = true
            val url = convertToTwitterUrl(match.groupValues[1])
            """<blockquote class="twitter-tweet"><a href="$url"></a></blockquote>"""
        }
        result = X_RAW_URL_REGEX.replace(result) { match ->
            hasEmbed = true
            val url = convertToTwitterUrl(match.groupValues[1])
            """<blockquote class="twitter-tweet"><a href="$url"></a></blockquote>"""
        }
        return TransformResult(result, hasEmbed)
    }

    data class TransformResult(val html: String, val hasXEmbed: Boolean)
}
