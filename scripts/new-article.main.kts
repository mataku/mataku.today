#!/usr/bin/env kotlin

import java.nio.file.Path
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.system.exitProcess

val projectRoot: Path = Path.of("").toAbsolutePath()
val articlesDir: Path = projectRoot.resolve("articles")
val templatePath: Path = projectRoot.resolve("templates/article.md")
val placeholderRegex = Regex("\\{\\{\\s*(\\w+)\\s*}}")

val now = ZonedDateTime.now()
val datePath = now.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
val articleDir = articlesDir.resolve(datePath)
articleDir.createDirectories()
val outputPath = articleDir.resolve("index.md")

if (outputPath.exists()) {
  System.err.println("Error: File already exists: $outputPath")
  exitProcess(1)
}

val variables =
  mapOf(
    "title" to datePath,
    "date" to now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX")),
  )

val content =
  placeholderRegex.replace(templatePath.readText()) { match ->
    variables[match.groupValues[1]] ?: match.value
  }
outputPath.writeText(content)

println("Created: $outputPath")
