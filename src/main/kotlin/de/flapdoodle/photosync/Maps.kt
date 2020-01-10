package de.flapdoodle.photosync

fun <V,T> Iterable<T>.associateByNotNull(valueSelector: (T) -> V?): Map<T, V> {
  return map { it to valueSelector(it) }
      .filter { it.second!=null }
      .map { Pair(it.first, it.second!!) }
      .toMap()
}