package de.flapdoodle.photosync.analyze

import de.flapdoodle.photosync.Blob
import de.flapdoodle.photosync.paths.Meta
import java.nio.file.Path

class GroupMetaData(blobs: List<Blob>) {
  private val groupedBlobs: Map<Blob, List<Blob>>
  private val isMeta: Set<Blob>

  companion object {
    internal fun groupByBasePath(paths: List<Path>): Map<Path, List<Path>> {
      val baseFiles = paths.filter { thisPath -> paths.none { Meta.isMeta(thisPath, it) } }
      val metaFiles = paths.filter { !baseFiles.contains(it) }

      return baseFiles.map {
        it to emptyList<Path>()
      }.toMap() + metaFiles.groupBy { thisPath ->
        baseFiles.find { Meta.isMeta(thisPath, it) }
            ?: throw IllegalArgumentException("basePath not found: $thisPath - $paths")
      }
    }
  }

  init {
    val mappedToPath = blobs.associateBy { it.path }
    val groupByDir = mappedToPath.keys.groupBy { it.parent }
    var groupedPaths: Map<Path, List<Path>> = emptyMap()

    groupByDir.forEach { it ->
      groupedPaths = groupedPaths + groupByBasePath(it.value)
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
  fun baseBlobs() = groupedBlobs.keys
}