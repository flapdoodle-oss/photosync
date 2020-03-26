package de.flapdoodle.photosync.sync

import de.flapdoodle.photosync.Blob
import de.flapdoodle.photosync.Comparision
import de.flapdoodle.photosync.compare
import de.flapdoodle.photosync.diff.BlobWithMeta
import de.flapdoodle.photosync.diff.DiffEntry
import de.flapdoodle.photosync.filehash.Hasher
import de.flapdoodle.photosync.paths.rewrite
import java.nio.file.Path

data class Diff2SyncCommands(
    private val srcPath: Path,
    private val dstPath: Path,
    private val sameContent: (Blob, Blob?) -> Boolean
) {

  companion object {
    fun sameContent(hasher: Hasher<*>): (Blob, Blob?) -> Boolean = { sourceMeta, matchingDest ->
      matchingDest != null && hasher.hash(sourceMeta.path, sourceMeta.size) == hasher.hash(matchingDest.path, matchingDest.size)
    }
  }

  fun generate(
      diff: List<DiffEntry>
  ): List<SyncCommandGroup> {
    return diff.flatMap {
      when (it) {
        is DiffEntry.Match -> sync(DiffMatchAnalyser(srcPath, dstPath).inspect(it))
        is DiffEntry.NewEntry -> listOf(newEntry(it))
        is DiffEntry.DeletedEntry -> listOf(deleteEntry(it))
        is DiffEntry.Noop -> emptyList()
      }
    }
  }

  private fun sync(result: DiffMatchAnalyser.InspectedMatch): List<SyncCommandGroup> {
    val syncCommands = result.matchingBlobs.map { (source, dest) ->
      sync(source, dest)
    }

    val createCommands = result.copySource.map {
      create(it)
    }

    val movedDestCommands = result.moveDestinations.map { (source, dest) ->
      val movedDest = dest.replaceBase(source.base.path.rewrite(srcPath, dstPath))

      SyncCommandGroup() +
          SyncCommand.Move(dest.base.path, movedDest.base.path) +
          SyncCommandGroup(dest.meta.zip(movedDest.meta).map {
            SyncCommand.Move(it.first.path, it.second.path)
          }) +
          sync(source, movedDest)
    }

    val removeCommands = result.removeDestinations.map {
      remove(it, SyncCommand.Cause.CopyRemovedFromSource)
    }

    return syncCommands + createCommands + movedDestCommands + removeCommands
  }

  private fun sync(source: BlobWithMeta, dest: BlobWithMeta): SyncCommandGroup {
    var commands = SyncCommandGroup()

    source.meta.forEach { sourceMeta ->
      val expectedDestination = sourceMeta.path.rewrite(srcPath, dstPath)
      val matchingDest = dest.meta.find { expectedDestination == it.path }

      val command = when (sourceMeta.lastModifiedTime.compare(matchingDest?.lastModifiedTime)) {
        Comparision.Bigger -> SyncCommand.Copy(sourceMeta.path, expectedDestination)
        Comparision.Smaller -> if (sameContent(sourceMeta, matchingDest)) {
          SyncCommand.Copy(sourceMeta.path, expectedDestination, sameContent = true)
        } else {
          SyncCommand.CopyBack(sourceMeta.path, expectedDestination)
        }
        Comparision.Equal -> null
        else -> SyncCommand.Copy(sourceMeta.path, expectedDestination) // nothing to compare
      }

      if (command != null) commands = commands + command
    }

    dest.meta.forEach { destMeta ->
      val expectedSource = destMeta.path.rewrite(dstPath, srcPath)
      val matchingSource = source.meta.find { expectedSource == it.path }
      if (matchingSource == null) {
        commands = commands + SyncCommand.Remove(destMeta.path, cause = SyncCommand.Cause.CopyRemovedFromSource)
      }
    }

    return commands
  }

  private fun newEntry(entry: DiffEntry.NewEntry): SyncCommandGroup {
    return entry.src.blobs.fold(SyncCommandGroup()) { commandGroup, blobWithMeta ->
      commandGroup + create(blobWithMeta)
    }
  }

  private fun deleteEntry(entry: DiffEntry.DeletedEntry): SyncCommandGroup {
    return entry.dst.blobs.fold(SyncCommandGroup()) { commandGroup, blobWithMeta ->
      commandGroup + remove(blobWithMeta, SyncCommand.Cause.DeletedEntry)
    }
  }

  private fun create(blobWithMeta: BlobWithMeta): SyncCommandGroup {
    return SyncCommandGroup(
        listOf(SyncCommand.Copy(blobWithMeta.base.path, blobWithMeta.base.path.rewrite(srcPath, dstPath))) +
            blobWithMeta.meta.map { SyncCommand.Copy(it.path, it.path.rewrite(srcPath, dstPath)) }
    )
  }

  private fun remove(blobWithMeta: BlobWithMeta, cause: SyncCommand.Cause): SyncCommandGroup {
    return SyncCommandGroup(
        listOf(SyncCommand.Remove(blobWithMeta.base.path, cause = cause)) + blobWithMeta.meta.map { SyncCommand.Remove(it.path, cause = cause) }
    )
  }
}