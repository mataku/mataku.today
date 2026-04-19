package blog

import org.commonmark.ext.autolink.AutolinkExtension
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.getLastModifiedTime
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.readText
import kotlin.io.path.writeText

class Generator {
  private val projectRoot: Path = Path.of("").toAbsolutePath()
  private val articlesDir: Path = projectRoot.resolve("articles")
  private val outputDir: Path = projectRoot.resolve("output")
  private val templatePath: Path = projectRoot.resolve("templates/article.html")
  private val notFoundTemplatePath: Path = projectRoot.resolve("templates/404.html")

  fun run() {
    outputDir.createDirectories()

    val isDev = System.getenv("DEV") == "1"

    val allMarkdownFiles =
      Files
        .walk(articlesDir)
        .filter { it.extension == "md" }
        .toList()
    if (allMarkdownFiles.isEmpty()) {
      println("No markdown files found in $articlesDir")
      return
    }

    val markdownFiles =
      if (isDev) {
        allMarkdownFiles
          .filter { file ->
            val relativePath = articlesDir.relativize(file.parent)
            val slug = file.nameWithoutExtension
            val outputFile = outputDir.resolve(relativePath).resolve("$slug.html")
            !outputFile.exists() || file.getLastModifiedTime() > outputFile.getLastModifiedTime()
          }.also { filtered ->
            println("DEV mode: ${filtered.size}/${allMarkdownFiles.size} articles to build")
          }
      } else {
        allMarkdownFiles
      }

    val extensions =
      listOf(
        TablesExtension.create(),
        StrikethroughExtension.create(),
        AutolinkExtension.create(),
      )
    val parser = Parser.builder().extensions(extensions).build()
    val renderer = HtmlRenderer.builder().extensions(extensions).build()

    for (file in markdownFiles) {
      if (file.extension != "md") continue

      val raw = file.readText()
      val article = FrontmatterParser.parse(raw)

      val document = parser.parse(article.content)
      val rawHtmlBody = renderer.render(document)
      val captionedHtml = ImageCaptionTransformer.transform(rawHtmlBody)
      val xEmbedResult = XEmbedTransformer.transform(captionedHtml)
      val gistEmbeddedHtml = GistEmbedTransformer.transform(xEmbedResult.html)
      val youtubeEmbeddedHtml = YouTubeEmbedTransformer.transform(gistEmbeddedHtml)
      val spotifyEmbeddedHtml = SpotifyEmbedTransformer.transform(youtubeEmbeddedHtml)
      val htmlBody = spotifyEmbeddedHtml

      val tagsHtml =
        if (article.tags.isNotEmpty()) {
          article.tags.joinToString("") { """<span class="tag">$it</span>""" }
        } else {
          ""
        }

      val relativePath = articlesDir.relativize(file.parent)
      val slug = file.nameWithoutExtension
      val urlPath = relativePath.resolve(slug).toString().replace("\\", "/")

      val variables = article.metadata.toMutableMap()
      variables["content"] = htmlBody
      variables["tags"] = tagsHtml
      variables["date"] = formatDateForDisplay(article.metadata["date"] ?: "")
      variables["url"] = "https://mataku.today/$urlPath"
      variables["description"] = generateDescription(htmlBody)
      variables["footer"] = SiteConfig.footerHtml
      variables["theme_toggle"] = SiteConfig.themeToggleHtml
      variables["x_widgets_script"] =
        if (xEmbedResult.hasXEmbed) {
          """<script async src="https://platform.twitter.com/widgets.js" charset="utf-8"></script>"""
        } else {
          ""
        }

      val html = TemplateEngine.render(templatePath, variables)
      val outputArticleDir = outputDir.resolve(relativePath)
      outputArticleDir.createDirectories()
      val outputFile = outputArticleDir.resolve("$slug.html")
      outputFile.writeText(html)
      println("Generated: $outputFile")

      copyArticleImages(file.parent, outputArticleDir)
    }

    val notFoundOutputPath = outputDir.resolve("404.html")
    if (!isDev || !notFoundOutputPath.exists() ||
      notFoundTemplatePath.getLastModifiedTime() > notFoundOutputPath.getLastModifiedTime()
    ) {
      generateStaticPage(notFoundTemplatePath, notFoundOutputPath)
    }
  }

  private val imageExtensions = setOf("png", "jpg", "jpeg", "gif")

  private fun copyArticleImages(
    articleDir: Path,
    outputArticleDir: Path,
  ) {
    Files
      .list(articleDir)
      .filter { it.extension in imageExtensions }
      .forEach { imageFile ->
        Files.copy(imageFile, outputArticleDir.resolve(imageFile.fileName), StandardCopyOption.REPLACE_EXISTING)
        println("Copied: ${outputArticleDir.resolve(imageFile.fileName)}")
      }
  }

  private fun generateStaticPage(
    templatePath: Path,
    outputPath: Path,
  ) {
    val variables =
      mapOf(
        "footer" to SiteConfig.footerHtml,
        "theme_toggle" to SiteConfig.themeToggleHtml,
      )
    val html = TemplateEngine.render(templatePath, variables)
    outputPath.writeText(html)
    println("Generated: $outputPath")
  }

  private fun formatDateForDisplay(dateString: String): String {
    if (dateString.isBlank()) return ""

    val displayFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.ENGLISH)

    return try {
      val offsetDateTime = OffsetDateTime.parse(dateString, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
      offsetDateTime.toLocalDate().format(displayFormatter)
    } catch (e: DateTimeParseException) {
      try {
        val localDate = LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE)
        localDate.format(displayFormatter)
      } catch (e: DateTimeParseException) {
        throw IllegalArgumentException("Invalid date format: $dateString")
      }
    }
  }

  private fun generateDescription(
    htmlBody: String,
    maxLength: Int = 80,
  ): String {
    val withoutHeaders = htmlBody.replace(Regex("<h[1-6][^>]*>.*?</h[1-6]>", RegexOption.DOT_MATCHES_ALL), "")
    val withoutLinks = withoutHeaders.replace(Regex("<a[^>]*>(.*?)</a>")) { it.groupValues[1] }
    val text =
      withoutLinks
        .replace(Regex("<[^>]+>"), "")
        .replace(Regex("https?://[a-zA-Z0-9./?=&#_%-]+"), "")
        .replace(Regex("\\s+"), " ")
        .trim()
    return if (text.length > maxLength) {
      text.take(maxLength) + "..."
    } else {
      text
    }
  }
}
