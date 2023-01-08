package de.flapdoodle.photosync.filehash

import de.flapdoodle.photosync.LastModified
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.nio.file.Path

internal class FastHashSelectorTest {
  private val testee = FastHashSelector.defaultMapping()

  @Test
  fun basicMimeTypeHasherMapping() {
    val now = LastModified.now()
    
    assertThat(testee.hasherFor(pathOf("textfile.txt"), 1L, now))
      .isEqualTo(FullHash)
    assertThat(testee.hasherFor(pathOf("IMG.CR2"), 2L, now.plus(1)))
      .isEqualTo(SizedQuickHash)
    assertThat(testee.hasherFor(pathOf("IMG.CR2.xmp"), 3L, now.plus(2)))
      .isEqualTo(FullHash)
  }

  fun pathOf(resourceName: String): Path {
    return Path.of(FastHashSelectorTest::class.java.getResource(resourceName)!!.toURI())
  }
}