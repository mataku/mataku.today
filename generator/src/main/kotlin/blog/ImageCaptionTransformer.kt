package blog

object ImageCaptionTransformer {
    private val IMG_WITH_TITLE_REGEX = Regex(
        """<img\s+src="([^"]*)"\s+alt="([^"]*)"\s+title="([^"]*)"\s*/?>"""
    )

    fun transform(html: String): String {
        return IMG_WITH_TITLE_REGEX.replace(html) { match ->
            val src = match.groupValues[1]
            val alt = match.groupValues[2]
            val caption = match.groupValues[3]
            """<figure><img src="$src" alt="$alt"><figcaption>$caption</figcaption></figure>"""
        }
    }
}
