package blog

import java.nio.file.Path
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import kotlin.io.path.createDirectories
import kotlin.io.path.extension
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.readText
import kotlin.io.path.writeText

object FeedGenerator {
    private val projectRoot: Path = Path.of("").toAbsolutePath()
    private val articlesDir: Path = projectRoot.resolve("articles")
    private val outputDir: Path = projectRoot.resolve("output")

    private const val SITE_TITLE = "mataku.today"
    private const val SITE_URL = "https://mataku.today"
    private const val AUTHOR_NAME = "Takuma Homma"
    private const val MAX_ENTRIES = 20

    @JvmStatic
    fun main(args: Array<String>) {
        generate()
    }

    private fun generate() {
        outputDir.createDirectories()

        val markdownFiles = articlesDir.listDirectoryEntries("*.md")
        if (markdownFiles.isEmpty()) {
            println("No markdown files found in $articlesDir")
            return
        }

        val entries = markdownFiles
            .filter { it.extension == "md" }
            .mapNotNull { file ->
                val raw = file.readText()
                val article = FrontmatterParser.parse(raw)
                val dateStr = article.metadata["date"] ?: return@mapNotNull null
                val title = article.metadata["title"] ?: return@mapNotNull null
                val isDraft = article.metadata["draft"]?.toBoolean() ?: false
                if (isDraft) return@mapNotNull null

                val slug = file.nameWithoutExtension
                val summary = extractSummary(article.content)

                FeedEntry(slug, title, dateStr, summary, article.tags)
            }
            .sortedByDescending { it.date }
            .take(MAX_ENTRIES)

        val lastUpdated = entries.firstOrNull()?.date
            ?: OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

        val xml = buildAtomFeed(entries, lastUpdated)
        val outputFile = outputDir.resolve("feed.xml")
        outputFile.writeText(xml)
        println("Generated: $outputFile")
    }

    private fun extractSummary(content: String, maxLength: Int = 200): String {
        val firstParagraph = content.trim()
            .split(Regex("\n\n+"))
            .firstOrNull { it.isNotBlank() && !it.startsWith("#") && !it.startsWith("```") }
            ?: ""

        val plainText = firstParagraph
            .replace(Regex("!?\\[([^\\]]*)]\\([^)]+\\)"), "$1")
            .replace(Regex("`[^`]+`"), "")
            .replace(Regex("\\*\\*([^*]+)\\*\\*"), "$1")
            .replace(Regex("\\*([^*]+)\\*"), "$1")
            .replace(Regex("^#+\\s*", RegexOption.MULTILINE), "")
            .replace(Regex("\\s+"), " ")
            .trim()

        return if (plainText.length > maxLength) {
            plainText.take(maxLength) + "..."
        } else {
            plainText
        }
    }

    private fun buildAtomFeed(entries: List<FeedEntry>, lastUpdated: String): String {
        val sb = StringBuilder()
        sb.appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")
        sb.appendLine("""<feed xmlns="http://www.w3.org/2005/Atom">""")
        sb.appendLine("  <title>${escapeXml(SITE_TITLE)}</title>")
        sb.appendLine("""  <link href="$SITE_URL"/>""")
        sb.appendLine("""  <link href="$SITE_URL/feed.xml" rel="self"/>""")
        sb.appendLine("  <updated>$lastUpdated</updated>")
        sb.appendLine("  <id>$SITE_URL/</id>")
        sb.appendLine("  <author>")
        sb.appendLine("    <name>${escapeXml(AUTHOR_NAME)}</name>")
        sb.appendLine("  </author>")

        for (entry in entries) {
            val entryUrl = "$SITE_URL/articles/${entry.slug}"
            sb.appendLine()
            sb.appendLine("  <entry>")
            sb.appendLine("    <title>${escapeXml(entry.title)}</title>")
            sb.appendLine("""    <link href="$entryUrl"/>""")
            sb.appendLine("    <id>$entryUrl</id>")
            sb.appendLine("    <updated>${entry.date}</updated>")
            sb.appendLine("    <summary>${escapeXml(entry.summary)}</summary>")
            for (tag in entry.tags) {
                sb.appendLine("""    <category term="${escapeXml(tag)}"/>""")
            }
            sb.appendLine("  </entry>")
        }

        sb.appendLine("</feed>")
        return sb.toString()
    }

    private fun escapeXml(value: String): String {
        return value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }

    private data class FeedEntry(
        val slug: String,
        val title: String,
        val date: String,
        val summary: String,
        val tags: List<String>
    )
}
