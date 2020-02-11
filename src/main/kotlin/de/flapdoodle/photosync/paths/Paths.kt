package de.flapdoodle.photosync.paths

import java.nio.file.Path
import java.util.regex.Pattern

fun Path.rewrite(srcBase: Path, dstBase: Path): Path {
  return dstBase.resolve(srcBase.relativize(this))
}

fun Path.expectParent(): Path {
  return this.parent ?: throw IllegalArgumentException("expected parent for $this is null")
}

fun Path.matches(pattern: Pattern): Boolean {
  return pattern.asPredicate().test(toString())
}