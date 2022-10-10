package de.flapdoodle.io.filetree.diff.samelayout

import de.flapdoodle.io.filetree.diff.Action
import de.flapdoodle.photosync.Comparision
import de.flapdoodle.photosync.LastModified
import de.flapdoodle.types.letThis
import java.lang.IllegalArgumentException
import java.nio.file.Path
import kotlin.io.path.div

class Sync(
  private val copy: Copy=Copy.ONLY_NEW,
  private val leftover: Leftover=Leftover.IGNORE
) {

  enum class Copy {
    IF_CHANGED, // any change -> copy
    ONLY_NEW // any change -> copy only if new
  }

  enum class Leftover {
    IGNORE, DELETE, COPY_BACK
  }

  fun actions(diff: Diff): List<Action> {
    return actions(diff.src, diff.dest, diff.entries)
  }

  private fun actions(src: Path, dest: Path, entries: List<Diff.Entry>): List<Action> {
    return entries.flatMap { actions(src, dest, it, copy, leftover) }
  }




  companion object {
    private fun actions(
      srcPath: Path,
      destPath: Path,
      entry: Diff.Entry,
      copy: Copy,
      leftover: Leftover
    ): List<Action> {
      return when (entry) {
        is Diff.Entry.TypeMismatch -> throw IllegalArgumentException("not supported: $entry")
        is Diff.Entry.IsEqual -> emptyList()
        is Diff.Entry.Missing -> create(srcPath, destPath, entry)
        is Diff.Entry.Leftover -> leftover(srcPath,destPath,entry, leftover)
        is Diff.Entry.FileChanged -> copyFile(srcPath, destPath, entry, copy)
        is Diff.Entry.DirectoryChanged -> copyDirectory(srcPath, destPath, entry, copy, leftover)
        else -> TODO("not implemented: $entry")
      }
    }

    internal fun leftover(srcPath: Path, destPath: Path, entry: Diff.Entry.Leftover, leftover: Leftover): List<Action> {
      return when(leftover) {
        Leftover.IGNORE -> emptyList()
        Leftover.DELETE -> remove(srcPath, entry)
        Leftover.COPY_BACK -> copyBack(srcPath, destPath, entry)
      }
    }

    internal fun remove(srcPath: Path, entry: Diff.Entry.Leftover): List<Action> {
      return when (entry) {
        is Diff.Entry.Leftover.LeftoverDirectory -> {
          val destNode = entry.dest
          val src = srcPath.resolve(destNode.name)

          listOf(Action.Remove(src)) +
              entry.entries.flatMap { x -> remove(src,x) }
        }

        is Diff.Entry.Leftover.LeftoverFile -> {
          val destNode = entry.dest
          val src = srcPath.resolve(destNode.name)

          listOf(
            Action.Remove(src)
          )
        }

        is Diff.Entry.Leftover.LeftoverSymLink -> {
          val destNode = entry.dest
          val src = srcPath.resolve(destNode.name)
          listOf(
            Action.Remove(src)
          )
        }
      }
    }

    internal fun copyBack(srcPath: Path, destPath: Path, entry: Diff.Entry.Leftover): List<Action> {
      return when (entry) {
        is Diff.Entry.Leftover.LeftoverDirectory -> {
          val destNode = entry.dest
          val src = srcPath.resolve(destNode.name)
          val dest = destPath.resolve(destNode.name)

          makeDir(src, destNode.lastModifiedTime, entry.entries.flatMap { copyBack(src,dest,it) })
        }

        is Diff.Entry.Leftover.LeftoverFile -> {
          val destNode = entry.dest
          copyFile(destPath.resolve(destNode.name), srcPath.resolve(destNode.name), destNode.size, destNode.lastModifiedTime)
        }

        is Diff.Entry.Leftover.LeftoverSymLink -> {
          TODO("not implemented: $entry")
        }
      }
    }

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

    internal fun copyDirectory(
      srcPath: Path,
      destPath: Path,
      entry: Diff.Entry.DirectoryChanged,
      copy: Copy,
      leftover: Leftover
    ): List<Action> {
      val actions = entry.entries.flatMap { actions(srcPath / entry.src.name,destPath / entry.dest.name,it,copy,leftover) }
      val isNewer = entry.compareTimeStamp() == Comparision.Bigger

      return if (actions.isNotEmpty()) {
        if (isNewer || copy == Copy.IF_CHANGED) {
          actions + Action.SetLastModified(destPath / entry.dest.name, entry.src.lastModifiedTime)
        } else {
          actions + Action.SetLastModified(destPath / entry.dest.name, entry.dest.lastModifiedTime)
        }
      } else {
        if (isNewer) {
          listOf(Action.SetLastModified(destPath / entry.dest.name, entry.src.lastModifiedTime))
        } else {
          emptyList()
        }
      }
    }

    internal fun copyFile(srcPath: Path, destPath: Path, entry: Diff.Entry.FileChanged, mode: Copy): List<Action> {
      val compareTimeStamp = entry.compareTimeStamp()
      val contentHasChanged = entry.contentHasChanged()
      val isNewer = compareTimeStamp == Comparision.Bigger

      val shouldCopyFile = when (mode) {
        Copy.ONLY_NEW -> isNewer && contentHasChanged
        Copy.IF_CHANGED -> contentHasChanged
      }
      val shouldSetLastModified = when (mode) {
        Copy.ONLY_NEW -> isNewer
        Copy.IF_CHANGED -> true
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