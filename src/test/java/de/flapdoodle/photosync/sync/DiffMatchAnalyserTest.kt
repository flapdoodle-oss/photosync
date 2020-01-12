package de.flapdoodle.photosync.sync

import de.flapdoodle.photosync.Blob
import de.flapdoodle.photosync.FileTimes
import de.flapdoodle.photosync.diff.BlobWithMeta
import de.flapdoodle.photosync.diff.DiffEntry
import de.flapdoodle.photosync.diff.GroupedBlobs
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.nio.file.Path

internal class DiffMatchAnalyserTest {

  private val testee = DiffMatchAnalyser(Path.of("src"), Path.of("dst"))

  @Test
  fun `one source matches one dest`() {
    val srcGroup = GroupedBlobs(listOf(
        BlobWithMeta(Blob(Path.of("src", "bar"), 0, FileTimes.now()))
    ))

    val dstGroup = GroupedBlobs(listOf(
        BlobWithMeta(Blob(Path.of("dst", "bar"), 0, FileTimes.now()))
    ))

    val result = testee.inspect(DiffEntry.Match(srcGroup, dstGroup))

    assertThat(result.matchingBlobs).containsEntry(srcGroup.blobs[0], dstGroup.blobs[0])
    assertThat(result.removeDestinations).isEmpty()
    assertThat(result.moveDestinations).isEmpty()
    assertThat(result.copySource).isEmpty()
  }

  @Test
  fun `one source matches dest, but path does not match`() {
    val srcGroup = GroupedBlobs(listOf(
        BlobWithMeta(Blob(Path.of("src", "bar"), 0, FileTimes.now()))
    ))

    val dstGroup = GroupedBlobs(listOf(
        BlobWithMeta(Blob(Path.of("dst", "baz"), 0, FileTimes.now()))
    ))

    val result = testee.inspect(DiffEntry.Match(srcGroup, dstGroup))

    assertThat(result.matchingBlobs).isEmpty()
    assertThat(result.removeDestinations).isEmpty()
    assertThat(result.moveDestinations).containsEntry(srcGroup.blobs[0], dstGroup.blobs[0])
    assertThat(result.copySource).isEmpty()
  }

  @Test
  fun `one source, but two destinations`() {
    val srcGroup = GroupedBlobs(listOf(
        BlobWithMeta(Blob(Path.of("src", "bar"), 0, FileTimes.now()))
    ))

    val dstGroup = GroupedBlobs(listOf(
        BlobWithMeta(Blob(Path.of("dst", "bar"), 0, FileTimes.now())),
        BlobWithMeta(Blob(Path.of("dst", "second"), 0, FileTimes.now()))
    ))

    val result = testee.inspect(DiffEntry.Match(srcGroup, dstGroup))

    assertThat(result.matchingBlobs).containsEntry(srcGroup.blobs[0], dstGroup.blobs[0])
    assertThat(result.removeDestinations).contains(dstGroup.blobs[1])
    assertThat(result.moveDestinations).isEmpty()
    assertThat(result.copySource).isEmpty()
  }

  @Test
  fun `two sources, but one destination`() {
    val srcGroup = GroupedBlobs(listOf(
        BlobWithMeta(Blob(Path.of("src", "bar"), 0, FileTimes.now())),
        BlobWithMeta(Blob(Path.of("src", "second"), 0, FileTimes.now()))
    ))

    val dstGroup = GroupedBlobs(listOf(
        BlobWithMeta(Blob(Path.of("dst", "bar"), 0, FileTimes.now()))
    ))

    val result = testee.inspect(DiffEntry.Match(srcGroup, dstGroup))

    assertThat(result.matchingBlobs).containsEntry(srcGroup.blobs[0], dstGroup.blobs[0])
    assertThat(result.removeDestinations).isEmpty()
    assertThat(result.moveDestinations).isEmpty()
    assertThat(result.copySource).contains(srcGroup.blobs[1])
  }
}