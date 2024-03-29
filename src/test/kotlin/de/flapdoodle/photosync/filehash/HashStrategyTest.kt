package de.flapdoodle.photosync.filehash

import de.flapdoodle.io.tree.Tree
import de.flapdoodle.photosync.Blob
import de.flapdoodle.photosync.LastModified
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.nio.file.Path

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

  @Test
  fun `group files by hash`() {
    val fileBarA = Tree.File(Path.of("foo", "bar"), 0, now())
    val fileBarB = Tree.File(Path.of("blob", "bar"), 1, now())
    val fileBaz = Tree.File(Path.of("foo", "baz"), 1, now())

    val files = listOf(
            fileBarA,
            fileBarB,
            fileBaz
    )

    val result = HashStrategy.groupBy(listOf(HashFilename(), HashSize()), files)

    assertThat(result)
            .containsKey(FilenameHash("baz"))
            .containsKey(FilenameHash("bar").append(SizeHash(0)))
            .containsKey(FilenameHash("bar").append(SizeHash(1)))
            .size().isEqualTo(3)
            .returnToMap()
            .containsEntry(FilenameHash("baz"), listOf(fileBaz))
            .containsEntry(FilenameHash("bar").append(SizeHash(0)), listOf(fileBarA))
            .containsEntry(FilenameHash("bar").append(SizeHash(1)), listOf(fileBarB))
  }

  data class FilenameHash(val key: String) : Hash<FilenameHash>
  data class SizeHash(val key: Long) : Hash<SizeHash>

  class HashFilename : Hasher<FilenameHash> {
    override fun hash(path: Path, size: Long, lastModifiedTime: LastModified): FilenameHash {
      return FilenameHash(path.fileName.toString())
    }
  }

  class HashSize : Hasher<SizeHash> {
    override fun hash(path: Path, size: Long, lastModifiedTime: LastModified): SizeHash {
      return SizeHash(size)
    }
  }

  private fun now(): LastModified {
    return LastModified.now()
  }
}