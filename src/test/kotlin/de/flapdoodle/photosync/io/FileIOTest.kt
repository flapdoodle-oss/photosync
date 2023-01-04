package de.flapdoodle.photosync.io

import de.flapdoodle.photosync.ByteArrays
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.ThreadLocalRandom

class FileIOTest {
  val random = ThreadLocalRandom.current();

  @Test
  fun readAllBytes(@TempDir tempDir: Path) {
    val content = ByteArrays.random(random.nextInt(512, 2048))
    Files.write(tempDir.resolve("all"), content)

    val collect=ByteBuffer.allocate(content.size)

    FileIO.readAllBytes(tempDir.resolve("all")) {
      collect.put(it)
    }

    collect.flip()

    val result = ByteArray(collect.limit())
    collect.get(result,0,collect.limit())

    assertThat(tempDir.resolve("all"))
      .binaryContent()
      .containsExactly(result.toTypedArray())
  }

  @Test
  fun readSegment(@TempDir tempDir: Path) {
    val content = ByteArrays.random(random.nextInt(512, 2048))
    Files.write(tempDir.resolve("part"), content)

    val start = FileIO.read(tempDir.resolve("part"),0,512)
    val end = FileIO.read(tempDir.resolve("part"),content.size-512L,512)

    assertThat(tempDir.resolve("part"))
      .binaryContent()
      .startsWith(start.toTypedArray())
      .endsWith(end.toTypedArray())
  }

  @Test
  fun read(@TempDir tempDir: Path) {
    val content = ByteArrays.incrementing(random.nextInt(512, 2048))
    Files.write(tempDir.resolve("seekable"), content)
    val result = FileIO.read(tempDir.resolve("seekable")) {
      // read segments more than once
      val first = read(0, 512)
      val second = read(content.size - 512L, 512)

      val firstAgain = read(0, 512)
      assertThat(first)
        .containsExactly(firstAgain.toTypedArray())

      val secondAgain = read(content.size - 512L, 512)
      assertThat(second)
        .containsExactly(secondAgain.toTypedArray())

      first to second
    }

    assertThat(tempDir.resolve("seekable"))
      .binaryContent()
      .startsWith(result.first.toTypedArray())
      .endsWith(result.second.toTypedArray())
  }
}