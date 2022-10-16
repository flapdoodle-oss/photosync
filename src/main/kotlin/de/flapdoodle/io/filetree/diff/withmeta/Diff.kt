package de.flapdoodle.io.filetree.diff.withmeta

import de.flapdoodle.io.filetree.Node
import de.flapdoodle.photosync.filehash.HashSelector
import java.nio.file.Path

class Diff {

  companion object {
    fun diff(src: Node.Top, dest: Node.Top, hashSelector: HashSelector): Diff {
      //diff(src.path, src.children, dest.path, dest.children, hashSelector)
      inspect(src.path, src.children, hashSelector)
      inspect(dest.path, dest.children, hashSelector)

      return Diff()
    }

    fun inspect(currentPath: Path, nodes: List<Node>, hashSelector: HashSelector) {
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