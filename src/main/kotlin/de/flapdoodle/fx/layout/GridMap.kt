package de.flapdoodle.fx.layout

data class GridMap<T : Any>(
    private val map: Map<Pos, T> = emptyMap()
) {

  private val columnSet = map.keys.map { it.column }.toSet().sorted()
  private val rowSet = map.keys.map { it.row }.toSet().sorted()

  fun columns() = columnSet
  fun rows() = rowSet
  fun values() = map.values

  fun add(pos: Pos, value: T): GridMap<T> {
    return copy(map = map + (pos to value))
  }

  fun remove(value: T): GridMap<T> {
    val keysToRemove = map.filter { it.value == value }.keys
    return copy(map = map - keysToRemove)
  }

  fun <D : Any> mapColumns(allColumnRows: (Int, Collection<T>) -> D): List<D> {
    return columnSet.map { column ->
      val matchingColumns = map.filter { it.key.column == column }.values
      allColumnRows(column, matchingColumns)
    }
  }

  fun <D : Any> mapRows(allRowColumns: (Int, Collection<T>) -> D): List<D> {
    return rowSet.map { row ->
      val matchingRows = map.filter { it.key.row == row }.values
      allRowColumns(row, matchingRows)
    }
  }

  operator fun get(pos: Pos): T? {
    return map[pos]
  }

  companion object {
    fun <T : Any> create(src: Collection<T>, posOf: (T) -> Pos): GridMap<T> {
      return GridMap(src.map { posOf(it) to it }.toMap())
    }
  }

  data class Pos(
      val column: Int,
      val row: Int
  ) {
    init {
      require(column >= 0) { "invalid column: $column" }
      require(row >= 0) { "invalid row: $row" }
    }
  }
}