package de.flapdoodle.photosync.filehash

import de.flapdoodle.photosync.filehash.MimeTypes
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.nio.file.Path

class MimeTypesTest {

  @Test
  fun sampleMimeTypes() {
    val testee = MimeTypes.defaultMapping()

    assertThat(testee.mimeTypeOf(pathOf("textfile.txt")))
      .isEqualTo("text/plain")
    assertThat(testee.mimeTypeOf(pathOf("IMG.CR2")))
      .isEqualTo("image/x-canon-cr2")
    assertThat(testee.mimeTypeOf(pathOf("IMG.CR2.xmp")))
      .isEqualTo("application/xml")
  }

  private fun pathOf(resourceName: String): Path {
    return Path.of(MimeTypesTest::class.java.getResource(resourceName)!!.toURI())
  }
}