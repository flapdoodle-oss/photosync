package de.flapdoodle.photosync.paths

import java.nio.file.Path

fun interface PathMatcher {
  fun match(first: Path, second: Path): Boolean

  fun andThen(other: PathMatcher): PathMatcher {
    return PathMatcher { a, b -> this.match(a, b) && other.match(a, b) }
  }

  companion object {

    val sameParent = PathMatcher { a, b -> a.parent == b.parent }
    val fileNameStart = PathMatcher { a, b -> a.fileName.toString().startsWith(b.fileName.toString()) }
    val notSameFileName = PathMatcher { a, b -> a.fileName != b.fileName }
  }
}