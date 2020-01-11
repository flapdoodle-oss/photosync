package de.flapdoodle.photosync.diff

import de.flapdoodle.photosync.Blob
import de.flapdoodle.photosync.FileTimes
import de.flapdoodle.photosync.MockedHasher
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.nio.file.Path

internal class ScanDiffAnalyzerTest {

  @Test
  fun `src contains all, dst nothing`() {
    val srcGroup = GroupedBlobs(listOf(
        BlobWithMeta(Blob(Path.of("src", "bar"), 0, FileTimes.now()))
    ))

    val src = Scan(Path.of("src"),
        listOf(srcGroup)
    )

    val dst = Scan(Path.of("dst"), emptyList())

    val hasher = MockedHasher()
        .addRule(Path.of("src","bar"),0,"hash")

    val result = ScanDiffAnalyzer.scan(src,dst,hasher)

    assertThat(result).size().isEqualTo(1)
        .returnToIterable()
        .element(0)
        .isInstanceOf(DiffEntry.NewEntry::class.java)
        .extracting { (it as DiffEntry.NewEntry).src }
        .isEqualTo(srcGroup)
  }


}