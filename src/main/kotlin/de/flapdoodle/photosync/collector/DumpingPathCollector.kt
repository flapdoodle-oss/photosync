package de.flapdoodle.photosync.collector

import java.nio.file.Path
import java.nio.file.attribute.FileTime

class DumpingPathCollector: PathCollector {

  private val sizeCluster: HashMap<Long, MutableSet<Path>> = HashMap()

  override fun add(path: Path, size: Long, lastModifiedTime: FileTime) {
    println("-> $path size=$size")

    sizeCluster.getOrPut(size) { mutableSetOf() }.add(path)
  }

  fun report() {
    sizeCluster.forEach {
      if (it.value.size>1) {
        println("-> ${it.key}")
        it.value.forEach {
          println(" --> $it")
        }
      }
    }
  }
}