package blog

import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import java.nio.file.attribute.FileTime
import kotlin.io.path.getLastModifiedTime
import kotlin.io.path.readText
import kotlin.io.path.setLastModifiedTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OutputWriterTest {
  @TempDir
  lateinit var root: Path

  @Test
  fun `writes a new file and creates parent directories`() {
    val target = root.resolve("nested/dir/page.html")

    val written = OutputWriter.writeIfChanged(target, "hello")

    assertTrue(written)
    assertEquals("hello", target.readText())
  }

  @Test
  fun `leaves an identical file untouched`() {
    val target = root.resolve("page.html")
    OutputWriter.writeIfChanged(target, "hello")
    target.setLastModifiedTime(FileTime.fromMillis(0))

    val written = OutputWriter.writeIfChanged(target, "hello")

    assertFalse(written)
    assertEquals(0, target.getLastModifiedTime().toMillis())
  }

  @Test
  fun `overwrites a file whose content differs`() {
    val target = root.resolve("page.html")
    OutputWriter.writeIfChanged(target, "hello")

    val written = OutputWriter.writeIfChanged(target, "world")

    assertTrue(written)
    assertEquals("world", target.readText())
  }
}
