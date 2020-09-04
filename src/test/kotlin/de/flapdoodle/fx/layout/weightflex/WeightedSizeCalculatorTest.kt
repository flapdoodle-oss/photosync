package de.flapdoodle.fx.layout.weightflex

import org.assertj.core.api.WithAssertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class WeightedSizeCalculatorTest : WithAssertions {

    @Test
    fun errorSample() {
        val map = mapOf<Int,WeightedDimension>() +
                (0 to WeightedDimension(4.0, 26.0, 26.0, 26.0)) +
                (1 to WeightedDimension(1.0, 26.0, 100.0, 26.0))
        val result = WeightedSizeCalculator.distribute(125.0, map)

        assertThat(result).size().isEqualTo(2)
        assertThat(result[0]).isEqualTo(26.0)
        assertThat(result[1]).isEqualTo(99.0)
    }
}