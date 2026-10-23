package blog

import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.FileTime
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.getLastModifiedTime
import kotlin.io.path.readText
import kotlin.io.path.setLastModifiedTime
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StaticFileSyncerTest {
  @TempDir
  lateinit var root: Path

  private val source: Path get() = root.resolve("static")
  private val target: Path get() = root.resolve("output")

  private fun write(
    path: Path,
    content: String,
  ): Path {
    path.parent.createDirectories()
    path.writeText(content)
    return path
  }

  @Test
  fun `copies missing files preserving relative paths`() {
    write(source.resolve("assets/favicon.ico"), "icon")
    write(source.resolve("nested/deep/file.txt"), "deep")

    val copied = StaticFileSyncer.sync(source, target)

    assertEquals(2, copied)
    assertEquals("icon", target.resolve("assets/favicon.ico").readText())
    assertEquals("deep", target.resolve("nested/deep/file.txt").readText())
  }

  @Test
  fun `skips files that are already up to date`() {
    write(source.resolve("a.txt"), "same")
    StaticFileSyncer.sync(source, target)
    val firstMtime = target.resolve("a.txt").getLastModifiedTime()

    val copied = StaticFileSyncer.sync(source, target)

    assertEquals(0, copied)
    assertEquals(firstMtime, target.resolve("a.txt").getLastModifiedTime())
  }

  @Test
  fun `overwrites target when source is newer`() {
    val file = write(source.resolve("a.txt"), "old")
    StaticFileSyncer.sync(source, target)
    Files.writeString(file, "new")
    file.setLastModifiedTime(FileTime.fromMillis(target.resolve("a.txt").getLastModifiedTime().toMillis() + 10_000))

    val copied = StaticFileSyncer.sync(source, target)

    assertEquals(1, copied)
    assertEquals("new", target.resolve("a.txt").readText())
  }

  @Test
  fun `overwrites target when size differs even if not newer`() {
    val file = write(source.resolve("a.txt"), "short")
    StaticFileSyncer.sync(source, target)
    Files.writeString(file, "much longer content")
    file.setLastModifiedTime(FileTime.fromMillis(0))

    val copied = StaticFileSyncer.sync(source, target)

    assertEquals(1, copied)
    assertEquals("much longer content", target.resolve("a.txt").readText())
  }

  @Test
  fun `respects include filter`() {
    write(source.resolve("post.md"), "# md")
    write(source.resolve("post/image.png"), "png")

    val copied = StaticFileSyncer.sync(source, target) { it.extension != "md" }

    assertEquals(1, copied)
    assertFalse(target.resolve("post.md").exists())
    assertTrue(target.resolve("post/image.png").exists())
  }

  @Test
  fun `returns zero when source directory does not exist`() {
    assertEquals(0, StaticFileSyncer.sync(root.resolve("missing"), target))
    assertFalse(target.exists())
  }
}
