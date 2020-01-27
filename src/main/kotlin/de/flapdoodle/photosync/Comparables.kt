package de.flapdoodle.photosync

enum class Comparision {
  Smaller, Equal, Bigger
}

fun <T: Comparable<T>> T.compare(other: T?): Comparision? {
  val res = other?.let { compareTo(it) }

  return if (res!=null) {
    when {
      res < 0 -> Comparision.Smaller
      res > 0 -> Comparision.Bigger
      else -> Comparision.Equal
    }
  } else {
    null
  }

}