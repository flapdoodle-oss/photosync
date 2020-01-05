package de.flapdoodle.photosync.analyze

import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.nio.file.Path

internal class GroupMetaDataTest {

  @Test
  fun pathEnds() {
    val a=Path.of("foo","bar.txt")
    val b = Path.of("foo","bar")

    assertThat(a.fileName.toString().startsWith(b.fileName.toString())).isTrue()
  }
}