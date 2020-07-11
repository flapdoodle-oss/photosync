package de.flapdoodle.fx.layout

data class AutoArray<T : Any> private constructor(
    val map: Map<Int, T> = emptyMap()
) {
  private val maxIndex = map.keys.max()

  fun set(index: Int, value: T?): AutoArray<T> {
    return if (value != null)
      copy(map = map + (index to value))
    else
      copy(map = map - index)
  }

  operator fun get(index: Int): T? {
    return map[index]
  }

  fun <D : Any> mapNotNull(function: (T?) -> D?): List<D> {
    return if (maxIndex != null)
      (0..maxIndex).mapNotNull { function(map[it]) }
    else
      emptyList()
  }

  companion object {
    fun <T : Any> empty() = AutoArray<T>()
  }
}