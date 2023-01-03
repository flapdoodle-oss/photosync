package de.flapdoodle.photosync.io

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Duration

class HumansTest {

  @Test
  fun durationAsString() {
    val result = Humans.asHumanReadable(
      Duration.ofMillis(123)
        .plus(Duration.ofSeconds(2))
        .plus(Duration.ofMinutes(4))
    )

    assertThat(result).isEqualTo("0h 4m 2s 123ms")
  }
}