package blog

import java.nio.file.Path
import kotlin.io.path.readText

object TemplateEngine {
    private val PLACEHOLDER_REGEX = Regex("\\{\\{\\s*(\\w+)\\s*}}")

    fun render(templatePath: Path, variables: Map<String, String>): String {
        val template = templatePath.readText()
        return PLACEHOLDER_REGEX.replace(template) { match ->
            val key = match.groupValues[1]
            variables[key] ?: match.value
        }
    }
}
