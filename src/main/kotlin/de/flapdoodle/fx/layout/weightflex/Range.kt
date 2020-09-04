package de.flapdoodle.fx.layout.weightflex

data class Range(val start: Int, val end: Int) {

    private val rangeAsList = IntRange(start, end).toList()

    init {
        require(start >= 0) { "invalid start: $start" }
        require(start <= end) { "invalid end: $end (start=$start)" }
    }

    companion object {
        fun of(pos: Int): Range {
            return Range(pos, pos)
        }

        fun of(start: Int, end: Int): Range {
            return Range(start, end)
        }
    }

    fun asList() = rangeAsList
}