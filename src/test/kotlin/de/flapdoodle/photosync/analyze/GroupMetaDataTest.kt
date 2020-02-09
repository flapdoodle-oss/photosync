package de.flapdoodle.photosync.analyze

import de.flapdoodle.photosync.Blob
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.nio.file.Path
import java.nio.file.attribute.FileTime
import java.time.Instant

internal class GroupMetaDataTest {

  @Test
  fun groupMetaDataToBaseFile() {
    val baseBlob = Blob(Path.of("foo", "bar"), 10, now())

    val blobs = listOf(
        baseBlob,
        Blob(Path.of("foo", "bar.txt"), 10, now()),
        Blob(Path.of("foo", "bar.md5"), 10, now())
    )

    val testee = GroupMetaData(blobs)

    assertThat(testee.baseBlobs())
        .containsExactly(baseBlob)
  }

  @Test
  fun blobWithoutMetaMustApearInBaseBlobs() {
    val lonelyBlob = Blob(Path.of("foo", "lonely"), 10, now())
    val baseBlob = Blob(Path.of("foo", "bar"), 10, now())

    val blobs = listOf(
        lonelyBlob,
        baseBlob,
        Blob(Path.of("foo", "bar.txt"), 10, now()),
        Blob(Path.of("foo", "bar.md5"), 10, now())
    )

    val testee = GroupMetaData(blobs)

    assertThat(testee.baseBlobs())
        .containsExactly(lonelyBlob, baseBlob)

  }

  @Test
  fun pathEnds() {
    val a = Path.of("foo", "bar.txt")
    val b = Path.of("foo", "bar")

    assertThat(a.fileName.toString().startsWith(b.fileName.toString())).isTrue()
  }

  private fun now(): FileTime {
    return FileTime.from(Instant.now())
  }
}