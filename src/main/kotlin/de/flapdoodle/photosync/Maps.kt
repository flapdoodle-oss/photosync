package de.flapdoodle.photosync

fun <V,T> Iterable<T>.associateByNotNull(valueSelector: (T) -> V?): Map<T, V> {
  return map { it to valueSelector(it) }
      .filter { it.second!=null }
      .map { Pair(it.first, it.second!!) }
      .toMap()
}

infix fun <K,V> Map<K,V>.add(pair: Pair<K,V>): Map<K, V> {
  require(!this.contains(pair.first)) {"${pair.first} already set to ${this[pair.first]}"}
  return this + pair
}