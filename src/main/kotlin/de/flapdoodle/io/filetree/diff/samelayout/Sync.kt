package de.flapdoodle.io.filetree.diff.samelayout

import de.flapdoodle.io.filetree.diff.Action
import de.flapdoodle.photosync.Comparision
import de.flapdoodle.photosync.LastModified
import de.flapdoodle.types.letThis
import java.lang.IllegalArgumentException
import java.nio.file.Path

class Sync(
  val mode: Mode = Mode.ONLY_NEW,
  val changes: Changes,
  val removed: Removed
) {
  enum class Mode {
    ONLY_NEW, // apply new changes from source
    OVERWRITE, // overwrite dest even if dest is newer
    COPY_BACK, // copy back if destination is newer
    FULL_SYNC // copy back and remove from source
  }

  enum class Changes {
    IF_CHANGED, // any change -> copy
    ONLY_NEW // any change -> copy only if new
  }

  enum class Removed {
    DELETE, COPY_BACK, SKIP
  }

  fun actions(diff: Diff): List<Action> {
    return actions(diff.src, diff.dest, diff.entries)
  }

  private fun actions(src: Path, dest: Path, entries: List<Diff.Entry>): List<Action> {
    return entries.flatMap { actions(src, dest, it) }
  }

  private fun actions(srcPath: Path, destPath: Path, entry: Diff.Entry): List<Action> {
    return when (entry) {
      is Diff.Entry.TypeMismatch -> throw IllegalArgumentException("not supported: $entry")
      is Diff.Entry.IsEqual -> emptyList()
      is Diff.Entry.Missing -> create(srcPath, destPath, entry)
      is Diff.Entry.Removed -> when(removed) {
        Removed.DELETE -> remove(srcPath, entry)
        Removed.COPY_BACK -> copyBack(srcPath, destPath, entry)
        else -> emptyList()
      }
      is Diff.Entry.FileChanged -> copyFile(srcPath, destPath, entry, changes)
      else -> TODO("not implemented: $entry")
    }
  }



  private fun remove(srcPath: Path, entry: Diff.Entry.Removed): List<Action> {
    return when (entry) {
      is Diff.Entry.Removed.RemovedDirectory -> {
        val destNode = entry.dest
        val src = srcPath.resolve(destNode.name)

        listOf(Action.Remove(src)) +
            entry.entries.flatMap { x -> remove(src,x) }
      }

      is Diff.Entry.Removed.RemovedFile -> {
        val destNode = entry.dest
        val src = srcPath.resolve(destNode.name)

        listOf(
          Action.Remove(src)
        )
      }

      is Diff.Entry.Removed.RemovedSymLink -> {
        val destNode = entry.dest
        val src = srcPath.resolve(destNode.name)
        listOf(
          Action.Remove(src)
        )
      }
    }
  }

  private fun copyBack(srcPath: Path, destPath: Path, entry: Diff.Entry.Removed): List<Action> {
    return when (entry) {
      is Diff.Entry.Removed.RemovedDirectory -> {
        val destNode = entry.dest
        val src = srcPath.resolve(destNode.name)
        val dest = destPath.resolve(destNode.name)

        makeDir(src, destNode.lastModifiedTime, entry.entries.flatMap { copyBack(src,dest,it) })
      }

      is Diff.Entry.Removed.RemovedFile -> {
        val destNode = entry.dest
        copyFile(destPath.resolve(destNode.name), srcPath.resolve(destNode.name), destNode.size, destNode.lastModifiedTime)
      }

      is Diff.Entry.Removed.RemovedSymLink -> {
        TODO("not implemented: $entry")
      }
    }
  }


  companion object {
    internal fun create(srcPath: Path, destPath: Path, entry: Diff.Entry.Missing): List<Action> {
      return when (entry) {
        is Diff.Entry.Missing.MissingDirectory -> {
          entry.src.letThis {
            val src = srcPath.resolve(name)
            val dest = destPath.resolve(name)
            makeDir(dest,lastModifiedTime, entry.entries.flatMap { x -> create(src,dest,x) })
          }
        }

        is Diff.Entry.Missing.MissingFile -> {
          entry.src.letThis {
            copyFile(srcPath.resolve(name), destPath.resolve(name), size, lastModifiedTime)
          }
        }

        is Diff.Entry.Missing.MissingSymLink -> {
          TODO("not implemented")
        }
      }
    }

    internal fun copyFile(srcPath: Path, destPath: Path, entry: Diff.Entry.FileChanged, mode: Changes): List<Action> {
      val compareTimeStamp = entry.compareTimeStamp()
      val contentHasChanged = entry.contentHasChanged()
      val isNewer = compareTimeStamp == Comparision.Bigger

      val shouldCopyFile = when (mode) {
        Changes.ONLY_NEW -> isNewer && contentHasChanged
        Changes.IF_CHANGED -> contentHasChanged
      }
      val shouldSetLastModified = when (mode) {
        Changes.ONLY_NEW -> isNewer
        Changes.IF_CHANGED -> true
      }

      return if (shouldCopyFile) {
        copyFile(srcPath.resolve(entry.src.name), destPath.resolve(entry.dest.name), entry.src.size, entry.src.lastModifiedTime)
      } else {
        if (shouldSetLastModified) {
          listOf(Action.SetLastModified(destPath.resolve(entry.dest.name), entry.src.lastModifiedTime))
        } else {
          emptyList()
        }
      }
    }

    internal fun copyFile(src: Path, dest: Path, size: Long, lastModified: LastModified): List<Action> {
      return listOf(
        Action.CopyFile(src,dest,size),
        Action.SetLastModified(dest,lastModified)
      )
    }

    internal fun makeDir(dest: Path, lastModified: LastModified, children: List<Action>): List<Action> {
      return listOf(Action.MakeDirectory(dest)) +
          children +
          Action.SetLastModified(dest, lastModified)
    }
  }
}