package de.flapdoodle.photosync.filetree

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.nio.file.Path

internal class TreeCollectorAdapterTest {
  private val testee = TreeCollectorAdapter()

  @Test
  fun `nothing collected will give error`() {
    assertThrows(IllegalArgumentException::class.java) {
      testee.asTree()
    }
  }

  @Test
  fun `should fail if scaning is in progress (quick hack)`() {
    val path = Path.of("down")
    testee.down(path)
    assertThrows(IllegalArgumentException::class.java) {
      testee.asTree()
    }
  }

  @Test
  fun `collect empty directory`() {
    val path = Path.of("down")
    testee.down(path)
    testee.up(path)

    val result = testee.asTree()

    assertThat(result.path).isEqualTo(path)
    assertThat(result.children).isEmpty()
  }
}