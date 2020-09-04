package de.flapdoodle.fx.layout.weightflex

data class Weight(val value: Double) {
    init {
        require(value >= 0) { "invalid weight: $value" }
    }

    operator fun plus(other: Weight): Weight {
        return Weight(value + other.value)
    }

    fun partOf(other: Weight): Double {
        require(other.value >= value) { "can not be a part of something smaller: $value > ${other.value}" }
        return if (other.value != 0.0)
            value / other.value
        else
            0.0
    }
}