package de.flapdoodle.fx.layout.weightflex

import org.assertj.core.api.WithAssertions
import org.assertj.core.data.Offset
import org.assertj.core.data.Percentage
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

    @Test
    fun otherErrorSample() {
        val map = mapOf<Int,WeightedDimension>() +
                (0 to WeightedDimension(10.0, 10.0, Double.MAX_VALUE, 151.0)) +
                (1 to WeightedDimension(10.0, 10.0, Double.MAX_VALUE, 435.0))+
                (2 to WeightedDimension(1.0, 60.0, 60.0, 60.0))+
                (3 to WeightedDimension(1.0, 0.0, Double.MAX_VALUE, 0.0))
        val result = WeightedSizeCalculator.distribute(644.0, map)

        // THIS IS WRONG
        assertThat(result).size().isEqualTo(4)
        assertThat(result[0]).isCloseTo(278.09, Offset.offset(0.01))
        assertThat(result[1]).isCloseTo(278.09, Offset.offset(0.01))
        assertThat(result[2]).isEqualTo(60.0)
        assertThat(result[3]).isCloseTo(27.80, Offset.offset(0.01))
    }
}