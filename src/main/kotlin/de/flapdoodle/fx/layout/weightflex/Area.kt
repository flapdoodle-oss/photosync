package de.flapdoodle.fx.layout.weightflex

data class Area(
        val column: Range,
        val row: Range
) {
    companion object {
        fun of(column: Int, row: Int): Area {
            return Area(Range.of(column), Range.of(row))
        }
        fun of(column: Range, row: Range): Area {
            return Area(column, row)
        }
    }

    fun columns() = column.asList()
    fun rows() = row.asList()
}