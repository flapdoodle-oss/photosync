package de.flapdoodle.photosync

import java.nio.file.Path

fun Path.isMetaOf(basePath: Path): Boolean {
  return this.parent == basePath.parent
      && this.fileName != basePath.fileName
      && this.fileName.toString().startsWith(basePath.fileName.toString())
}