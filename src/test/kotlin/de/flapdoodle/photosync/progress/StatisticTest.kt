package de.flapdoodle.photosync.progress

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class StatisticTest {
  @Test
  fun collectSomeSamples() {
    val genKeyB = GenericKey("foo", Int::class.java, Int::plus, { ">$it<" })
    val genKeyC = GenericKey("foo", Int::class.java, Int::plus, { ">$it<" })

    val result = Statistic.collect {
      Statistic.set(KeyA, 123)
      Statistic.set(genKeyB, 1)
      Statistic.set(genKeyC, 10)
      Statistic.set(KeyA, 17)
      Statistic.set(genKeyB, 100)
      Statistic.set(genKeyC, 3)
    }

    assertThat(result)
      .hasSize(3)
      .containsExactlyInAnyOrder(
        Statistic.Entry(KeyA, 140),
        Statistic.Entry(genKeyB, 101),
        Statistic.Entry(genKeyC, 13),
      )

    assertThat(result.map { it.asHumanReadable() })
      .containsExactlyInAnyOrder(
        "foo: >101<",
        "foo: >13<",
        "KeyA: 140"
      )
  }

  object KeyA : Statistic.Property<Long> {
    override val name: String = "KeyA"
    override val type: Class<Long> = Long::class.java
    override val reduce: (Long, Long) -> Long = Long::plus
    override val formatter: (Long) -> String = { value -> "$value" }
  }

  data class GenericKey(
    override val name: String,
    override val type: Class<Int>,
    override val reduce: (Int, Int) -> Int,
    override val formatter: (Int) -> String
  ) : Statistic.Property<Int>
}