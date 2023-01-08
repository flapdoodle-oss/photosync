package de.flapdoodle.photosync

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class LastModifiedTest {

  @Test
  fun readBackFromStringMustMatchSourceValue() {
    val now = LastModified.now()
    val asString = LastModified.toString(now)
    val readBack = LastModified.fromString(asString)

    Assertions.assertThat(readBack)
      .isEqualTo(now)
  }
}