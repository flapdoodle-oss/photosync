package de.flapdoodle.photosync.diff

import de.flapdoodle.photosync.Blob
import de.flapdoodle.photosync.FileTimes
import de.flapdoodle.photosync.LastModified
import de.flapdoodle.photosync.MockedHasher
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.nio.file.Path

internal class ScanDiffAnalyzerTest {

  @Test
  fun `src contains all, dst nothing - create entry`() {
    val srcGroup = GroupedBlobs(listOf(
        BlobWithMeta(Blob(Path.of("src", "bar"), 0, LastModified.now()))
    ))

    val src = Scan(listOf(srcGroup))
    val dst = Scan(emptyList())

    val hasher = MockedHasher()
        .addRule(Path.of("src", "bar"), 0, "hash")

    val result = ScanDiffAnalyzer.scan(src, dst, hasher)

    assertThat(result).size().isEqualTo(1)
        .returnToIterable()
        .element(0)
        .isInstanceOf(DiffEntry.NewEntry::class.java)
        .extracting { (it as DiffEntry.NewEntry).src }
        .isEqualTo(srcGroup)
  }

  @Test
  fun `dst contains all, src nothing - delete entry`() {
    val dstGroup = GroupedBlobs(listOf(
        BlobWithMeta(Blob(Path.of("dst", "bar"), 0, LastModified.now()))
    ))

    val src = Scan(emptyList())
    val dst = Scan(listOf(dstGroup))

    val hasher = MockedHasher()
        .addRule(Path.of("dst", "bar"), 0, "hash")

    val result = ScanDiffAnalyzer.scan(src, dst, hasher)

    assertThat(result).size().isEqualTo(1)
        .returnToIterable()
        .element(0)
        .isInstanceOf(DiffEntry.DeletedEntry::class.java)
        .extracting { (it as DiffEntry.DeletedEntry).dst }
        .isEqualTo(dstGroup)
  }

  @Test
  fun `src contains same as dst`() {
    val srcGroup = GroupedBlobs(listOf(
        BlobWithMeta(Blob(Path.of("src", "bar"), 0, LastModified.now()))
    ))

    val dstGroup = GroupedBlobs(listOf(
        BlobWithMeta(Blob(Path.of("dst", "baz"), 0, LastModified.now()))
    ))

    val src = Scan(listOf(srcGroup))
    val dst = Scan(listOf(dstGroup))

    val hasher = MockedHasher()
        .addRule(Path.of("src", "bar"), 0, "hash")
        .addRule(Path.of("dst", "baz"), 0, "hash")

    val result = ScanDiffAnalyzer.scan(src, dst, hasher)

    assertThat(result).size().isEqualTo(2)
        .returnToIterable()
        .anySatisfy {
          assertThat(it)
              .isInstanceOf(DiffEntry.Match::class.java)
              .extracting { (it as DiffEntry.Match) }
              .isEqualTo(DiffEntry.Match(srcGroup, dstGroup))
        }
        .anySatisfy {
          assertThat(it)
              .isInstanceOf(DiffEntry.Noop::class.java)
        }
  }

}