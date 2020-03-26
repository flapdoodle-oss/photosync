package de.flapdoodle.photosync.filehash

import de.flapdoodle.photosync.Blob
import de.flapdoodle.photosync.LastModified
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.nio.file.Path
import java.nio.file.attribute.FileTime
import java.time.Instant

class HashStrategyTest {

  @Test
  fun `single group if hashStrategy provides no hasher`() {
    val blobs = listOf(
        Blob(Path.of("foo","bar"),0,now()),
        Blob(Path.of("foo","baz"),1,now())
    )

    val result = HashStrategy.groupBlobs(HashStrategy { emptyList() }, blobs)

    assertThat(result).size().isEqualTo(1)
        .returnToMap()
        .containsValues(blobs)
  }

  @Test
  fun `group blobs by hash`() {
    val blobBarA = Blob(Path.of("foo", "bar"), 0, now())
    val blobBarB = Blob(Path.of("blob", "bar"), 1, now())
    val blobBaz = Blob(Path.of("foo", "baz"), 1, now())

    val blobs = listOf(
        blobBarA,
        blobBarB,
        blobBaz
    )

    val result = HashStrategy.groupBlobs(HashStrategy { listOf(HashFilename(), HashSize()) }, blobs)

    assertThat(result)
        .containsKey(FilenameHash("baz"))
        .containsKey(FilenameHash("bar").append(SizeHash(0)))
        .containsKey(FilenameHash("bar").append(SizeHash(1)))
        .size().isEqualTo(3)
        .returnToMap()
        .containsEntry(FilenameHash("baz"), listOf(blobBaz))
        .containsEntry(FilenameHash("bar").append(SizeHash(0)), listOf(blobBarA))
        .containsEntry(FilenameHash("bar").append(SizeHash(1)), listOf(blobBarB))
  }

  data class FilenameHash(val key: String) : Hash<FilenameHash>
  data class SizeHash(val key: Long) : Hash<SizeHash>

  class HashFilename : Hasher<FilenameHash> {
    override fun hash(path: Path, size: Long): FilenameHash {
      return FilenameHash(path.fileName.toString())
    }
  }

  class HashSize : Hasher<SizeHash> {
    override fun hash(path: Path, size: Long): SizeHash {
      return SizeHash(size)
    }
  }

  private fun now(): LastModified {
    return LastModified.now()
  }
}