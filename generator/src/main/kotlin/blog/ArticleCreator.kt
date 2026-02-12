package blog

import java.nio.file.Path
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.io.path.exists
import kotlin.io.path.writeText

object ArticleCreator {
    private val projectRoot: Path = Path.of("").toAbsolutePath()
    private val articlesDir: Path = projectRoot.resolve("articles")
    private val templatePath: Path = projectRoot.resolve("templates/article.md")

    @JvmStatic
    fun main(args: Array<String>) {
        if (args.isEmpty()) {
            System.err.println("Usage: ./gradlew :generator:new --args=\"<article>\"")
            System.exit(1)
        }
        create(args[0])
    }

    private fun create(article: String) {
        val outputPath = articlesDir.resolve("$article.md")

        if (outputPath.exists()) {
            System.err.println("Error: File already exists: $outputPath")
            System.exit(1)
        }

        val today = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX"))
        val variables = mapOf("date" to today)

        val content = TemplateEngine.render(templatePath, variables)
        outputPath.writeText(content)

        println("Created: $outputPath")
    }
}
