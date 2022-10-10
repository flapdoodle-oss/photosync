package de.flapdoodle.photosync.filehash

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.nio.file.Path

internal class FastHashSelectorTest {
  private val testee = FastHashSelector.defaultMapping()

  @Test
  fun basicMimeTypeHasherMapping() {
    assertThat(testee.hasherFor(pathOf("textfile.txt")))
      .isEqualTo(FullHash)
    assertThat(testee.hasherFor(pathOf("IMG.CR2")))
      .isEqualTo(SizedQuickHash)
    assertThat(testee.hasherFor(pathOf("IMG.CR2.xmp")))
      .isEqualTo(FullHash)
  }

  fun pathOf(resourceName: String): Path {
    return Path.of(FastHashSelectorTest::class.java.getResource(resourceName)!!.toURI())
  }
}