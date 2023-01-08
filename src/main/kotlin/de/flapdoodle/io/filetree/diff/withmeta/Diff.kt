package de.flapdoodle.io.filetree.diff.withmeta

import de.flapdoodle.io.filetree.Node
import de.flapdoodle.photosync.filehash.HashSelector
import java.nio.file.Path

/**
 * die Idee ist:
 * unveränderliche basis-datei mit metainformationen dient als schlüssel
 * wenn sie in der quelle verschoben wird, soll sie auch im ziel verschoben werden..
 * die meta-dateien werden sonst nach dem standard-verfahren abgeglichen..
 */
class Diff {

  companion object {
    fun diff(
      src: Node.Top,
      dest: Node.Top,
      hashSelector: HashSelector,
      metaFileFilter: MetaFileFilter
    ): Diff {
      //diff(src.path, src.children, dest.path, dest.children, hashSelector)
      inspect(src.path, src.children, hashSelector, metaFileFilter)
      inspect(dest.path, dest.children, hashSelector, metaFileFilter)

      return Diff()
    }

    fun inspect(currentPath: Path, nodes: List<Node>, hashSelector: HashSelector, metaFileFilter: MetaFileFilter) {
      val fileNodes = nodes.filterIsInstance<Node.File>()
      val metaFileMap = metaFileFilter.filter(MetaFileMap.of(fileNodes.map { it.name }.toSet()))
      val baseFilesNames = metaFileMap.baseNames()
      val baseFiles = fileNodes.filter { baseFilesNames.contains(it.name) }

      baseFiles.map { hashSelector.hasherFor(currentPath.resolve(it.name), it.size, it.lastModifiedTime).hash(currentPath.resolve(it.name), it.size, it.lastModifiedTime) }
      // filter meta files
      // hash main files
    }

//    fun diff(
//      srcPath: Path,
//      src: List<Node>,
//      destPath: Path,
//      dest: List<Node>,
//      hashSelector: HashSelector
//    ) {
//
//    }
  }
}