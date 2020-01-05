package de.flapdoodle.photosync.analyze

import de.flapdoodle.photosync.Blob
import java.nio.file.Path

class GroupMetaData(blobs: List<Blob>) {
  private val groupedBlobs: Map<Blob, List<Blob>>
  private val isMeta: Set<Blob>

  companion object {
    internal fun groupByBaseName(paths: List<Path>): Map<Path, List<Path>> {
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

  init {
    val mappedToPath = blobs.associateBy { it.path }
    val groupByDir = mappedToPath.keys.groupBy { it.parent }
    var groupedPaths: Map<Path, List<Path>> = emptyMap()

    groupByDir.forEach { it ->
      groupedPaths = groupedPaths + groupByBaseName(it.value)
    }

    groupedBlobs = groupedPaths.map { entry ->
      val blob = mappedToPath[entry.key] ?: error("could not find ${entry.key}")
      val metaBlobs = entry.value.map { mappedToPath[it] ?: error("could not find $it") }
      blob to metaBlobs
    }.toMap()

    isMeta = groupedBlobs.values.flatten().toSet()
  }

  fun isMeta(blob: Blob) = isMeta.contains(blob)
  fun metaBlobs(blob: Blob) = groupedBlobs[blob] ?: emptyList()
}