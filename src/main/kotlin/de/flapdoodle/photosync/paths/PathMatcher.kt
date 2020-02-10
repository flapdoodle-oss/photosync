package de.flapdoodle.photosync.paths

import de.flapdoodle.photosync.KotlinCompilerFix_SAM_Helper
import java.nio.file.Path

interface PathMatcher {
  fun match(first: Path, second: Path): Boolean

  fun andThen(other: PathMatcher): PathMatcher {
    return PathMatcher { a, b -> this.match(a, b) && other.match(a, b) }
  }

  companion object {

    @KotlinCompilerFix_SAM_Helper
    inline operator fun invoke(crossinline delegate: (Path, Path) -> Boolean): PathMatcher {
      return object : PathMatcher {
        override fun match(first: Path, second: Path): Boolean {
          return delegate(first, second)
        }
      }
    }

    val sameParent = PathMatcher { a, b -> a.parent == b.parent }
    val fileNameStart = PathMatcher { a, b -> a.fileName.toString().startsWith(b.fileName.toString()) }
    val notSameFileName = PathMatcher { a, b -> a.fileName != b.fileName }
  }
}