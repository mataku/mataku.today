package blog

import org.snakeyaml.engine.v2.api.Load
import org.snakeyaml.engine.v2.api.LoadSettings

object FrontmatterParser {
    private const val DELIMITER = "---"

    fun parse(raw: String): Article {
        val trimmed = raw.trimStart()
        if (!trimmed.startsWith(DELIMITER)) {
            return Article(emptyMap(), raw)
        }

        val endIndex = trimmed.indexOf(DELIMITER, DELIMITER.length)
        if (endIndex == -1) {
            return Article(emptyMap(), raw)
        }

        val yamlBlock = trimmed.substring(DELIMITER.length, endIndex).trim()
        val body = trimmed.substring(endIndex + DELIMITER.length).trimStart()

        val settings = LoadSettings.builder().build()
        val load = Load(settings)
        val yamlData = load.loadFromString(yamlBlock)

        var tags: List<String> = emptyList()
        val metadata = when (yamlData) {
            is Map<*, *> -> {
                val tagsValue = yamlData["tags"]
                tags = when (tagsValue) {
                    is List<*> -> tagsValue.filterIsInstance<String>()
                    else -> emptyList()
                }
                yamlData.entries
                    .filter { (k, _) -> k.toString() != "tags" }
                    .associate { (k, v) -> k.toString() to v.toString() }
            }
            else -> emptyMap()
        }

        return Article(metadata = metadata, content = body, tags = tags)
    }
}
