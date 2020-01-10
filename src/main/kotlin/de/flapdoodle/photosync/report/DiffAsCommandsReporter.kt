package de.flapdoodle.photosync.report

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
        is DiffEntry.Match -> sync(it)
        is DiffEntry.NewEntry -> newEntry(it)
        is DiffEntry.DeletedEntry -> deleteEntry(it)
        else -> {
        }
      }
    }
  }

  private fun sync(entry: DiffEntry.Match) {

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