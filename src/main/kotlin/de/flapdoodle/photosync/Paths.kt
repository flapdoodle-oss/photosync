package de.flapdoodle.photosync

import java.nio.file.Path

fun Path.startsWithOrIsEqualTo(basePath: Path): Boolean {
  return this.parent == basePath.parent
      && this.fileName.toString().startsWith(basePath.fileName.toString())
}

fun Path.isMetaOf(basePath: Path): Boolean {
  return this.startsWithOrIsEqualTo(basePath)
      && this.fileName != basePath.fileName
}

fun Path.rewrite(srcBase: Path, dstBase: Path): Path {
  return dstBase.resolve(srcBase.relativize(this))
}

fun Path.replaceBase(base: Path, newBase: Path): Path {
  require(this.startsWithOrIsEqualTo(base)) { "$this must start with $base"}

  val fileName = this.fileName.toString()
  val baseFileName = base.fileName.toString()
  val append = fileName.substring(baseFileName.length)

  return newBase.parent.resolve(newBase.fileName.toString()+append)
}

fun Path.expectParent(): Path {
  return this.parent ?: throw IllegalArgumentException("expected parent for $this is null")
}