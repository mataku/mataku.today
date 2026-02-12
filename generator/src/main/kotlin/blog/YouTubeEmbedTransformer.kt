package blog

object YouTubeEmbedTransformer {
    private val YOUTUBE_LINK_REGEX = Regex(
        """<a href="(https?://(?:www\.)?youtube\.com/watch\?v=([a-zA-Z0-9_-]+)[^"]*)"[^>]*>[^<]*</a>"""
    )
    private val YOUTUBE_RAW_URL_REGEX = Regex(
        """(?<![">])(https?://(?:www\.)?youtube\.com/watch\?v=([a-zA-Z0-9_-]+))"""
    )
    private val YOUTU_BE_LINK_REGEX = Regex(
        """<a href="(https?://youtu\.be/([a-zA-Z0-9_-]+)[^"]*)"[^>]*>[^<]*</a>"""
    )
    private val YOUTU_BE_RAW_URL_REGEX = Regex(
        """(?<![">])(https?://youtu\.be/([a-zA-Z0-9_-]+))"""
    )

    fun transform(html: String): String {
        var result = html
        result = YOUTUBE_LINK_REGEX.replace(result) { matchResult ->
            val videoId = matchResult.groupValues[2]
            createIframe(videoId)
        }
        result = YOUTUBE_RAW_URL_REGEX.replace(result) { matchResult ->
            val videoId = matchResult.groupValues[2]
            createIframe(videoId)
        }
        result = YOUTU_BE_LINK_REGEX.replace(result) { matchResult ->
            val videoId = matchResult.groupValues[2]
            createIframe(videoId)
        }
        result = YOUTU_BE_RAW_URL_REGEX.replace(result) { matchResult ->
            val videoId = matchResult.groupValues[2]
            createIframe(videoId)
        }
        return result
    }

    private fun createIframe(videoId: String): String {
        return """<div class="youtube-embed"><iframe src="https://www.youtube.com/embed/$videoId" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" allowfullscreen></iframe></div>"""
    }
}
