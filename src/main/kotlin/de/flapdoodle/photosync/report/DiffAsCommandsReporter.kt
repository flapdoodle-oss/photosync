package de.flapdoodle.photosync.report

import de.flapdoodle.photosync.diff.BlobWithMeta
import de.flapdoodle.photosync.diff.DiffEntry
import de.flapdoodle.photosync.rewrite
import java.nio.file.Path

class DiffAsCommandsReporter(
    private val srcPath: Path,
    private val dstPath: Path
) {

  fun generate(
      diff: List<DiffEntry>
  ) {
    diff.forEach {
      when (it) {
        is DiffEntry.Match -> sync(DiffMatchAnalyser(srcPath,dstPath).inspect(it))
        is DiffEntry.NewEntry -> newEntry(it)
        is DiffEntry.DeletedEntry -> deleteEntry(it)
        else -> {
        }
      }
    }
  }

  private fun sync(result: DiffMatchAnalyser.InspectedMatch) {
    result.matchingBlobs.forEach { (source, dest) ->
      sync(source,dest)
    }
  }

  private fun sync(entry: DiffEntry.Match) {
    val sourceDestMapping = entry.src.blobs.associateWith { blobWithMeta ->
      val exppectedSrcPath = blobWithMeta.base.path.rewrite(srcPath, dstPath)
      entry.dst.blobs.find { it.base.path == exppectedSrcPath }
    }

    sourceDestMapping
        .filterValues { it != null }
        .forEach { (s, d) ->
      if (d!=null) sync(s, d)
    }
  }

  private fun sync(source: BlobWithMeta, dest: BlobWithMeta) {
    println("sync ${source.base.path} ${dest.base.path}")
    source.meta.forEach {sourceMeta ->
      val expectedDestination = sourceMeta.path.rewrite(srcPath, dstPath)
      val matchingDest = dest.meta.find { expectedDestination ==it.path }
      if (matchingDest!=null) {
        if (sourceMeta.lastModifiedTime.toInstant().isAfter(matchingDest.lastModifiedTime.toInstant())) {
          op("cp",sourceMeta.path, expectedDestination)
        } else {
          println("do nothing ${sourceMeta.path} $expectedDestination")
        }
      } else {
        op("cp",sourceMeta.path, expectedDestination)
      }
    }
  }

  private fun newEntry(entry: DiffEntry.NewEntry) {
    entry.src.blobs.forEach { blobWithMeta ->
      op("cp", blobWithMeta.base.path, blobWithMeta.base.path.rewrite(srcPath,dstPath))
      blobWithMeta.meta.forEach {
        op("cp", it.path, it.path.rewrite(srcPath,dstPath))
      }
    }
  }

  private fun deleteEntry(entry: DiffEntry.DeletedEntry) {
    entry.dst.blobs.forEach { blobWithMeta ->
      op("rm", blobWithMeta.base.path)
      blobWithMeta.meta.forEach {
        op("rm", it.path)
      }
    }
  }

  fun op(command: String, src: Path) {
    println("$command $src")
  }

  fun op(command: String, src: Path, dst: Path) {
    println("$command $src $dst")
  }
}