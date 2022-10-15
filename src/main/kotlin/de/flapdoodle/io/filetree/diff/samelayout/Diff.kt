package de.flapdoodle.io.filetree.diff.samelayout

import de.flapdoodle.io.filetree.Node
import de.flapdoodle.photosync.Comparision
import de.flapdoodle.photosync.compare
import de.flapdoodle.photosync.filehash.HashSelector
import java.nio.file.Path

data class Diff(
  val src: Path,
  val dest: Path,
  val entries: List<Entry>
) {

  sealed class Entry {
    data class IsEqual(val node: Node) : Entry()
    data class TypeMismatch(val src: Node, val dest: Node) : Entry()

    sealed class Leftover(open val dest: Node) : Entry() {
      data class LeftoverFile(override val dest: Node.File) : Leftover(dest)
      data class LeftoverSymLink(override val dest: Node.SymLink) : Leftover(dest)
      data class LeftoverDirectory(override val dest: Node.Directory, val entries: List<Leftover>) : Leftover(dest)
    }

    sealed class Missing(open val src: Node) : Entry() {
      data class MissingFile(override val src: Node.File) : Missing(src)
      data class MissingSymLink(override val src: Node.SymLink) : Missing(src)
      data class MissingDirectory(override val src: Node.Directory, val entries: List<Missing>) : Missing(src)
    }

    data class FileChanged(
      val src: Node.File,
      val dest: Node.File,
      val contentChanged: Boolean
    ) : Entry() {
      init {
        require(src!=dest || contentChanged) {"$src == $dest && contentChanged==$contentChanged"}
      }

      fun compareTimeStamp(): Comparision {
        return src.lastModifiedTime.compare(dest.lastModifiedTime)!!
      }

      fun contentHasChanged(): Boolean {
        return contentChanged || (src.size != dest.size)
      }
    }

    data class SymLinkChanged(
      val src: Node.SymLink,
      val dest: Node.SymLink
    ) : Entry() {
      init {
        require(src!=dest) {"$src == $dest"}
      }

      fun compareTimeStamp(): Comparision {
        return src.lastModifiedTime.compare(dest.lastModifiedTime)!!
      }

      fun destinationHasChanged(): Boolean {
        return (src.destination != dest.destination)
      }
    }

    data class DirectoryChanged(
      val src: Node.Directory,
      val dest: Node.Directory,
      val entries: List<Entry>
    ) : Entry() {

      fun compareTimeStamp(): Comparision {
        return src.lastModifiedTime.compare(dest.lastModifiedTime)!!
      }
    }
  }

  companion object {
    fun diff(src: Node.Top, dest: Node.Top, hashSelector: HashSelector): Diff {
      return Diff(src.path, dest.path, diff(src.path, src.children, dest.path, dest.children, hashSelector))
    }

    fun diff(
      srcPath: Path,
      src: List<Node>,
      destPath: Path,
      dest: List<Node>,
      hashSelector: HashSelector
    ): List<Entry> {
      val srcByName = src.associateBy(Node::name)
      val destByName = dest.associateBy(Node::name)

      require(srcByName.size == src.size) { "key collision in $src" }
      require(destByName.size == dest.size) { "key collision in $dest" }

      val both = srcByName.keys.intersect(destByName.keys)
      val onlyInSrc = srcByName.keys - destByName.keys
      val onlyInDest = destByName.keys - srcByName.keys

      val changed = both.map { diffNodes(srcPath, srcByName[it]!!, destPath, destByName[it]!!, hashSelector) }

      val missing = src.filter { onlyInSrc.contains(it.name) }
        .map(::missing)

      val removed = dest.filter { onlyInDest.contains(it.name) }
        .map(::removed)

      return missing + removed + changed
    }

    private fun missing(it: Node): Entry.Missing = when (it) {
      is Node.File -> Entry.Missing.MissingFile(it)
      is Node.SymLink -> Entry.Missing.MissingSymLink(it)
      is Node.Directory -> Entry.Missing.MissingDirectory(it, it.children.map(::missing))
    }

    private fun removed(it: Node): Entry.Leftover = when (it) {
      is Node.File -> Entry.Leftover.LeftoverFile(it)
      is Node.SymLink -> Entry.Leftover.LeftoverSymLink(it)
      is Node.Directory -> Entry.Leftover.LeftoverDirectory(it, it.children.map(::removed))
    }

    private fun diffNodes(
      srcPath: Path,
      src: Node,
      destPath: Path,
      dest: Node,
      hashSelector: HashSelector
    ): Entry {
      require(src.name == dest.name) { "different names: $src - $dest" }

      if (src::class != dest::class) {
        return Entry.TypeMismatch(src, dest)
      }
      return when (src) {
        is Node.File -> diff(srcPath, src, destPath, dest as Node.File, hashSelector)
        is Node.SymLink -> diff(src, dest as Node.SymLink)
        is Node.Directory -> diff(srcPath, src, destPath, dest as Node.Directory, hashSelector)
      }
    }

    internal fun diff(
      srcPath: Path,
      src: Node.File,
      destPath: Path,
      dest: Node.File,
      hashSelector: HashSelector
    ): Entry {
      val timeStampChanged = src.lastModifiedTime != dest.lastModifiedTime

      return if (src.size == dest.size) {
        // check for content change
        val hasher = hashSelector.hasherFor(srcPath.resolve(src.name))
        val srcHash = hasher.hash(srcPath.resolve(src.name), src.size)
        val destHash = hasher.hash(destPath.resolve(dest.name), dest.size)
        if (srcHash == destHash) {
          // same content
          if (!timeStampChanged) Entry.IsEqual(src)
          else Entry.FileChanged(src, dest, false)
        } else {
          Entry.FileChanged(src, dest, true)
        }
      } else {
        Entry.FileChanged(src, dest, false)
      }
    }

    internal fun diff(src: Node.SymLink, dest: Node.SymLink): Entry {
      return if (src != dest)
        Entry.SymLinkChanged(src, dest)
      else
        Entry.IsEqual(src)
    }

    internal fun diff(
      srcPath: Path,
      src: Node.Directory,
      destPath: Path,
      dest: Node.Directory,
      hashSelector: HashSelector
    ): Entry {
      val changes = diff(srcPath.resolve(src.name), src.children, destPath.resolve(dest.name), dest.children, hashSelector)
      val nochange = changes.all { it is Entry.IsEqual }

      return if (src == dest && nochange)
        Entry.IsEqual(src)
      else
        Entry.DirectoryChanged(src, dest, changes)
    }
  }
}