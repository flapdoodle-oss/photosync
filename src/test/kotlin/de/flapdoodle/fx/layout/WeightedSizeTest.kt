package de.flapdoodle.fx.layout

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class WeightedSizeTest {

  @Test
  fun `same weights must share space equal`() {
    val src = listOf(
            WeightedSize(weight = 1.0, min = 0.0, max = Double.MAX_VALUE),
            WeightedSize(weight = 1.0, min = 0.0, max = Double.MAX_VALUE)
    )

    val result = WeightedSize.distribute(100.0, src)

    assertThat(result).containsExactly(50.0, 50.0)
  }

  @Test
  fun `same weights must share space equal if enough space left`() {
    val src = listOf(
            WeightedSize(weight = 1.0, min = 75.0, max = Double.MAX_VALUE),
            WeightedSize(weight = 1.0, min = 0.0, max = Double.MAX_VALUE)
    )

    val result = WeightedSize.distribute(100.0, src)

    assertThat(result).containsExactly(75.0, 25.0)
  }

  @Test
  fun `same weights must share space equal if not too much space left`() {
    val src = listOf(
            WeightedSize(weight = 1.0, min = 0.0, max = 25.0),
            WeightedSize(weight = 1.0, min = 0.0, max = Double.MAX_VALUE)
    )

    val result = WeightedSize.distribute(100.0, src)

    assertThat(result).containsExactly(25.0, 75.0)
  }

  @Test
  fun `sample`() {
    val src = listOf(
            WeightedSize(weight = 1.0, min = 20.0, max = 30.0),
            WeightedSize(weight = 4.0, min = 10.0, max = Double.MAX_VALUE),
            WeightedSize(weight = 1.0, min = 10.0, max = 60.0)
    )

//    assertThat(WeightedSize.distribute(400.0, src)).containsExactly(30.0, 310.0, 60.0)
    assertThat(WeightedSize.distribute(330.0, src)).containsExactly(30.0, 240.0, 60.0)

    (300..400).forEach {
      val space = it * 1.0
      val sizes = WeightedSize.distribute(space, src)
      assertThat(sizes.sumByDouble { it })
          .describedAs("space = $space")
          .isEqualTo(space)
    }
//    assertThat(WeightedSize.distribute(300.0, src)).containsExactly(30.0, 310.0, 60.0)
  }
}