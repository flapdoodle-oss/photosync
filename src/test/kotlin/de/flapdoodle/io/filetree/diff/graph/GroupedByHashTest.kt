package de.flapdoodle.io.filetree.diff.graph

import de.flapdoodle.photosync.filehash.SizeHash
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.nio.file.Path

class GroupedByHashTest {

  @Test
  fun addPaths() {
    val a = Path.of("a")
    val cd = Path.of("c/d")
    val e = Path.of("e")

    val testee = GroupedByHash()
      .add(SizeHash(1), cd)
      .add(SizeHash(2), e)
      .add(SizeHash(1), a)

    assertThat(testee.map)
      .containsEntry(SizeHash(1), setOf(a, cd))
      .containsEntry(SizeHash(2), setOf(e))
      .hasSize(2)
  }
}