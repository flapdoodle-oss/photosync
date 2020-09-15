package de.flapdoodle.fx.layout.weightflex

import de.flapdoodle.fx.layout.WeightedSize
import org.assertj.core.api.WithAssertions
import org.junit.jupiter.api.Test

internal class FlexMapTest : WithAssertions {

    @Test
    fun sample() {
        val testee = FlexMap<String>()
                .add("(0,0)", Area.of(0, 0))

        val columnSizes = testee.columnSizes({ it -> Weight(1.0) }, limitsOf = {
            Triple(0.2, 2.0, 1.0)
        })

        assertThat(testee.columns()).containsExactly(0)
        assertThat(testee.rows()).containsExactly(0)

        assertThat(columnSizes).size().isEqualTo(1)
        assertThat(columnSizes[0]).isEqualTo(WeightedDimension(1.0, 0.2, 2.0, 1.0))
    }

    /**
     *
     * |2|2|1|
     *
     * | x | y | z |
     * |   a   | b |
     */
    @Test
    fun otherSample() {
        val testee = FlexMap<String>()
                .add("x", Area.of(0, 0))
                .add("y", Area.of(1, 0))
                .add("z", Area.of(2, 0))
                .add("a", Area.of(Range.of(0, 1), Range.of(1)))
                .add("b", Area.of(Range.of(2), Range.of(1)))

        assertThat(testee.columns()).containsExactly(0, 1, 2)
        assertThat(testee.rows()).containsExactly(0, 1)

        val columnWeight = { it: Int ->
            when (it) {
                0, 1 -> Weight(2.0)
                else -> Weight(1.0)
            }
        }
        val limitsOf = { it: String ->
            when (it) {
                "z" -> Triple(1.0 , 3.0, 2.0)
                "b" -> Triple(0.0, 2.0, 1.0)
                "a" -> Triple(1.2 , 3.8, 2.5)
                else -> Triple(0.5 , 2.0,1.25)
            }
        }

        val columnSizes = testee.columnSizes(columnWeight, limitsOf)

        assertThat(columnSizes).size().isEqualTo(3)
        assertThat(columnSizes[0]).isEqualTo(WeightedDimension(2.0, 0.6, 2.0, 1.25))
        assertThat(columnSizes[1]).isEqualTo(WeightedDimension(2.0, 0.6, 2.0, 1.25))
        assertThat(columnSizes[2]).isEqualTo(WeightedDimension(1.0, 1.0, 3.0, 2.0))

        val rowWeight = { it: Int ->
            when (it) {
                0 -> Weight(1.0)
                else -> Weight(2.0)
            }
        }

        val rowLimitsOf = { it: String ->
            when (it) {
                "z" -> Triple(1.0, 3.0,2.0)
                "a" -> Triple(2.0 ,4.0, 2.0)
                else -> Triple(0.5 ,1.0, 0.75)
            }
        }
        val rowSizes = testee.rowSizes(rowWeight, rowLimitsOf)

        assertThat(rowSizes).size().isEqualTo(2)
        assertThat(rowSizes[0]).isEqualTo(WeightedDimension(1.0, 1.0, 3.0, 2.0))
        assertThat(rowSizes[1]).isEqualTo(WeightedDimension(2.0, 2.0, 4.0, 2.0))
    }

    /**
     *
     * | 1 | 2 |
     *
     * | a | b |  <- 4
     * |   | c |  <- 1
     */
    @Test
    fun errorCase() {
        val testee = FlexMap<String>()
                .add("a", Area.of(0, 0))
                .add("b", Area.of(1, 0))
                .add("c", Area.of(1, 1))

        assertThat(testee.columns()).containsExactly(0, 1)
        assertThat(testee.rows()).containsExactly(0, 1)

        val columnWeight = { it: Int ->
            when (it) {
                0 -> Weight(1.0)
                else -> Weight(2.0)
            }
        }

        val limitsOf = { it: String ->
            when (it) {
                "a" -> Triple(20.0 , 100.0, 40.0)
                "b" -> Triple(30.0, 30.0, 30.0)
                "c" -> Triple(30.0 , 30.0, 30.0)
                else -> Triple(0.0 , 0.0,0.0)
            }
        }

        val columnSizes = testee.columnSizes(columnWeight, limitsOf)

        assertThat(columnSizes).size().isEqualTo(2)
        assertThat(columnSizes[0]).isEqualTo(WeightedDimension(1.0, 20.0, 100.0, 40.0))
        assertThat(columnSizes[1]).isEqualTo(WeightedDimension(2.0, 30.0, 30.0, 30.0))

        val rowWeight = { it: Int ->
            when (it) {
                0 -> Weight(4.0)
                else -> Weight(1.0)
            }
        }

        val rowLimitsOf = { it: String ->
            when (it) {
                "a" -> Triple(26.0, 26.0,26.0)
                "b" -> Triple(26.0 ,26.0, 26.0)
                "c" -> Triple(26.0 ,100.0, 26.0)
                else -> Triple(0.0 ,0.0, 0.0)
            }
        }
        val rowSizes = testee.rowSizes(rowWeight, rowLimitsOf)

        assertThat(rowSizes).size().isEqualTo(2)
        assertThat(rowSizes[0]).isEqualTo(WeightedDimension(4.0, 26.0, 26.0, 26.0))
        assertThat(rowSizes[1]).isEqualTo(WeightedDimension(1.0, 26.0, 100.0, 26.0))
    }
}