package de.flapdoodle.photosync

inline fun <T, D: Any> Iterable<T>.findNotNull(predicate: (T) -> D?): D? {
  return mapNotNull(predicate).firstOrNull()
}