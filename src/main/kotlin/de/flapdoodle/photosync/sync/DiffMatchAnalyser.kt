package de.flapdoodle.photosync.sync

import de.flapdoodle.photosync.associateByNotNull
import de.flapdoodle.photosync.diff.BlobWithMeta
import de.flapdoodle.photosync.diff.DiffEntry
import de.flapdoodle.photosync.paths.rewrite
import java.nio.file.Path

class DiffMatchAnalyser(
    private val srcPath: Path,
    private val dstPath: Path
) {

  fun inspect(entry: DiffEntry.Match): InspectedMatch {
    val matchingBlobs = entry.src.blobs.associateByNotNull { s ->
      val expectedPath = s.base.path.rewrite(srcPath, dstPath)
      entry.dst.blobs.find { it.base.path == expectedPath }
    }

    val unmappedSourceBlobs = entry.src.blobs.filter { s ->
      !matchingBlobs.containsKey(s)
    }.sortedBy { it.base.path }

    val unmappedDstBlobs = entry.dst.blobs.filter { d ->
      !matchingBlobs.containsValue(d)
    }.sortedBy { it.base.path }

    val moveDestinations = unmappedSourceBlobs.zip(unmappedDstBlobs).toMap()

    val removeDestinations = unmappedDstBlobs.drop(moveDestinations.size)
    val stillUnmappedSourceBlobs = unmappedSourceBlobs.dropLast(moveDestinations.size)

    return InspectedMatch(
        matchingBlobs = matchingBlobs,
        moveDestinations = moveDestinations,
        copySource = stillUnmappedSourceBlobs,
        removeDestinations = removeDestinations
    )
  }

  data class InspectedMatch(
      val matchingBlobs: Map<BlobWithMeta, BlobWithMeta>,
      val moveDestinations: Map<BlobWithMeta, BlobWithMeta>,
      val copySource: List<BlobWithMeta>,
      val removeDestinations: List<BlobWithMeta>
  )
}