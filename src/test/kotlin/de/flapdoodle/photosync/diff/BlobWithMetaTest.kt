package de.flapdoodle.photosync.diff

import de.flapdoodle.photosync.Blob
import de.flapdoodle.photosync.FileTimes
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.lang.IllegalArgumentException
import java.nio.file.Path
import java.nio.file.attribute.FileTime

internal class BlobWithMetaTest {

  @Test
  fun `meta file in same path with different basename must throw exception`() {
    val base = Path.of("base", "file")
    val meta = Path.of("base", "fill.wrong")

    assertThatThrownBy {
      BlobWithMeta(
          Blob(base, 0, FileTimes.now()),
          listOf(Blob(meta, 0, FileTimes.now()))
      )
    }.isInstanceOf(IllegalArgumentException::class.java)
  }

  @Test
  fun `meta file in different path with same basename must throw exception`() {
    val base = Path.of("base", "file")
    val meta = Path.of("wrong", "file.meta")

    assertThatThrownBy {
      BlobWithMeta(
          Blob(base, 0, FileTimes.now()),
          listOf(Blob(meta, 0, FileTimes.now()))
      )
    }.isInstanceOf(IllegalArgumentException::class.java)
  }

  @Test
  fun `replace path must replace path in each blob`() {
    val blob = BlobWithMeta(Blob(Path.of("a", "base"), 0, FileTimes.now()),
        listOf(Blob(Path.of("a", "base.meta"), 0, FileTimes.now()))
    )

    val result = blob.replaceBase(Path.of("new","other"))

    assertThat(result.base)
        .isEqualTo(blob.base.copy(path = Path.of("new", "other")))

    assertThat(result.meta)
        .size().isEqualTo(1)
        .returnToIterable().element(0)
        .isEqualTo(blob.meta[0].copy(path = Path.of("new","other.meta")))
  }
}