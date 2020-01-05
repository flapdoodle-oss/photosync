package de.flapdoodle.photosync.analyze

import de.flapdoodle.photosync.Blob
import java.nio.file.Path

class GroupMetaData(blobs: List<Blob>) {
  init {
    val paths = blobs.map { it.path }
    val groupByDir = paths.groupBy { it.parent }
    var groupedPaths: Map<Path, List<Path>> = emptyMap()

    groupByDir.forEach { it ->
      groupedPaths = groupedPaths + groupByBaseName(it.value)
    }

    groupedPaths.forEach { path, list ->
      println("$path:")
      list.forEach {
        println("-> $it")
      }
    }
  }

  companion object {
    fun groupByBaseName(paths: List<Path>): Map<Path, List<Path>> {
      //val fileNames= paths.associateBy { it.fileName.toString() }
      val fileNames = paths.map { it.fileName.toString() to it }
      val groupMap: HashMap<Path, MutableList<Path>> = HashMap()

      paths.forEach {path ->
        val fileName = path.fileName.toString()
        val baseFile = fileNames.filter { fileName.startsWith(it.first) && fileName!=it.first }
        if (baseFile.isNotEmpty()) {
          groupMap.getOrPut(baseFile.single().second) { mutableListOf() }.add(path)
        }
      }

      return groupMap
    }
  }
}