package de.flapdoodle.photosync.filehash

import de.flapdoodle.photosync.LastModified
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.nio.file.Path

class MimeTypeHashSelectorTest {

  @Test
  fun expectedHasherInDefaultConfig() {
    val testee = MimeTypeHashSelector.defaultConfig()
    val now = LastModified.now()

    assertThat(testee.hasherFor(pathOf("textfile.txt"), 123L, now))
      .isEqualTo(FullHash)
    assertThat(testee.hasherFor(pathOf("IMG.CR2"), 123L, now))
      .isEqualTo(SizedQuickHash)
    assertThat(testee.hasherFor(pathOf("IMG.CR2.xmp"), 123L, now))
      .isEqualTo(FullHash)
  }

  @Test
  fun expectedSizeRuleHasherInDefaultConfig() {
    val testee = MimeTypeHashSelector.defaultConfig()
    val now = LastModified.now()

    assertThat(testee.hasherFor(pathOf("unknown"), 123L, now))
      .isEqualTo(FullHash)
    assertThat(testee.hasherFor(pathOf("unknown"), 4*1024+1L, now))
      .isEqualTo(SizedQuickHash)
  }

  private fun pathOf(resourceName: String): Path {
    return Path.of(MimeTypesTest::class.java.getResource(resourceName)!!.toURI())
  }
}