package de.flapdoodle.photosync

import java.nio.file.Path

fun Path.isMetaOf(basePath: Path): Boolean {
  return this.parent == basePath.parent
      && this.fileName != basePath.fileName
      && this.fileName.toString().startsWith(basePath.fileName.toString())
}

fun Path.rewrite(srcBase: Path, dstBase: Path): Path {
  return dstBase.resolve(srcBase.relativize(this))
}