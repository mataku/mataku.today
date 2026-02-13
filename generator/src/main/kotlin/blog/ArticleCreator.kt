package blog

import java.nio.file.Path
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.writeText

object ArticleCreator {
    private val projectRoot: Path = Path.of("").toAbsolutePath()
    private val articlesDir: Path = projectRoot.resolve("articles")
    private val templatePath: Path = projectRoot.resolve("templates/article.md")

    @JvmStatic
    fun main(args: Array<String>) {
        create()
    }

    private fun create() {
        val now = ZonedDateTime.now()
        val datePath = now.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
        val articleDir = articlesDir.resolve(datePath)
        articleDir.createDirectories()
        val outputPath = articleDir.resolve("index.md")

        if (outputPath.exists()) {
            System.err.println("Error: File already exists: $outputPath")
            System.exit(1)
        }

        val title = now.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
        val date = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX"))
        val variables = mapOf(
            "title" to title,
            "date" to date
        )

        val content = TemplateEngine.render(templatePath, variables)
        outputPath.writeText(content)

        println("Created: $outputPath")
    }
}
