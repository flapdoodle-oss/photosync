package de.flapdoodle.photosync.sync

import de.flapdoodle.photosync.diff.BlobWithMeta
import de.flapdoodle.photosync.diff.DiffEntry
import de.flapdoodle.photosync.rewrite
import java.nio.file.Path

class Diff2SyncCommands(
    private val srcPath: Path,
    private val dstPath: Path
) {

  fun generate(
      diff: List<DiffEntry>
  ): List<CommandGroup> {
    return diff.flatMap {
      when (it) {
        is DiffEntry.Match -> sync(DiffMatchAnalyser(srcPath, dstPath).inspect(it))
        is DiffEntry.NewEntry -> listOf(newEntry(it))
        is DiffEntry.DeletedEntry -> listOf(deleteEntry(it))
        is DiffEntry.Noop -> emptyList()
      }
    }
  }

  private fun sync(result: DiffMatchAnalyser.InspectedMatch): List<CommandGroup> {
    val syncCommands = result.matchingBlobs.map { (source, dest) ->
      sync(source, dest)
    }

    val createCommands = result.copySource.map {
      create(it)
    }

    val movedDestCommands = result.moveDestinations.map { (source, dest) ->
      val movedDest = dest.replaceBase(source.base.path.rewrite(srcPath, dstPath))

      CommandGroup() +
          Command.Move(dest.base.path, movedDest.base.path) +
          CommandGroup(dest.meta.zip(movedDest.meta).map {
            Command.Move(it.first.path, it.second.path)
          }) +
          sync(source, movedDest)
    }

    val removeCommands = result.removeDestinations.map {
      remove(it,Command.Cause.CopyRemovedFromSource)
    }

    return syncCommands + createCommands + movedDestCommands + removeCommands
  }

  private fun sync(source: BlobWithMeta, dest: BlobWithMeta): CommandGroup {
    var commands = CommandGroup()

    source.meta.forEach { sourceMeta ->
      val expectedDestination = sourceMeta.path.rewrite(srcPath, dstPath)
      val matchingDest = dest.meta.find { expectedDestination == it.path }
      if (matchingDest != null) {
        if (sourceMeta.lastModifiedTime.toInstant().isAfter(matchingDest.lastModifiedTime.toInstant())) {
          commands = commands + Command.Copy(sourceMeta.path, expectedDestination)
        }
      } else {
        commands = commands + Command.Copy(sourceMeta.path, expectedDestination)
      }
    }

    return commands
  }

  private fun newEntry(entry: DiffEntry.NewEntry): CommandGroup {
    return entry.src.blobs.fold(CommandGroup()) { commandGroup, blobWithMeta ->
      commandGroup + create(blobWithMeta)
    }
  }

  private fun deleteEntry(entry: DiffEntry.DeletedEntry): CommandGroup {
    return entry.dst.blobs.fold(CommandGroup()) { commandGroup, blobWithMeta ->
      commandGroup + remove(blobWithMeta, Command.Cause.DeletedEntry)
    }
  }

  private fun create(blobWithMeta: BlobWithMeta): CommandGroup {
    return CommandGroup(
        listOf(Command.Copy(blobWithMeta.base.path, blobWithMeta.base.path.rewrite(srcPath, dstPath))) +
            blobWithMeta.meta.map { Command.Copy(it.path, it.path.rewrite(srcPath, dstPath)) }
    )
  }

  private fun remove(blobWithMeta: BlobWithMeta, cause: Command.Cause): CommandGroup {
    return CommandGroup(
        listOf(Command.Remove(blobWithMeta.base.path, cause=cause)) + blobWithMeta.meta.map { Command.Remove(it.path, cause=cause) }
    )
  }
}