package de.flapdoodle.io.filetree.diff.graph

import de.flapdoodle.io.filetree.Node
import de.flapdoodle.photosync.LastModified
import de.flapdoodle.photosync.filehash.Hash
import de.flapdoodle.photosync.filehash.HashCache
import de.flapdoodle.photosync.filehash.HashSelector
import de.flapdoodle.types.Either
import java.nio.file.Path

sealed class HashTree(
  open val path: Path,
  open val name: String,
  open val lastModifiedTime: LastModified
) {
  data class Top(
    val path: Path,
    val lastModifiedTime: LastModified,
    val children: List<HashTree> = emptyList()
  )

  data class Directory(
    override val path: Path,
    override val name: String,
    override val lastModifiedTime: LastModified,
    val children: List<HashTree> = emptyList()
  ) : HashTree(path, name, lastModifiedTime) {
    init {
      val nameCollisions = children.groupBy { it.name }.filterValues { it.size != 1 }
      require(nameCollisions.isEmpty()) { "name collisions: $nameCollisions" }
    }
  }

  data class File(
    override val path: Path,
    override val name: String,
    override val lastModifiedTime: LastModified,
    val size: Long,
    val hash: Hash<*>,
  ) : HashTree(path, name, lastModifiedTime)

  data class MetaFile(
    override val path: Path,
    override val name: String,
    override val lastModifiedTime: LastModified,
    val size: Long,
    val baseFile: String,
  ) : HashTree(path, name, lastModifiedTime)

  data class SymLink(
    override val path: Path,
    override val name: String,
    override val lastModifiedTime: LastModified,
    val destination: Either<Node.NodeReference, Path>
  ) : HashTree(path, name, lastModifiedTime)

  companion object {
    fun asHashTree(src: Node.Top, hashSelector: HashSelector, hashCache: HashCache?): Top {
      return Top(src.path, src.lastModifiedTime, asHashTree(src.path, src.children, hashSelector, hashCache))
    }

    private fun asHashTree(
      path: Path,
      nodes: List<Node>,
      selector: HashSelector,
      cache: HashCache?
    ): List<HashTree> {
      return nodes.map { asHashTree(path, it, selector, cache) }
    }

    private fun asHashTree(
      path: Path,
      node: Node,
      hashSelector: HashSelector,
      hashCache: HashCache?
    ): HashTree {
      val nodePath = path.resolve(node.name)

      return when (node) {
        is Node.File -> {
          val hasher = hashSelector.hasherFor(nodePath, node.size, node.lastModifiedTime)
          val hash = hashCache?.hash(nodePath, node.size, node.lastModifiedTime, hasher)
            ?: hasher.hash(nodePath, node.size, node.lastModifiedTime)

          File(nodePath, node.name, node.lastModifiedTime, node.size, hash)
        }

        is Node.Directory -> Directory(nodePath, node.name, node.lastModifiedTime, asHashTree(path.resolve(node.name), node.children, hashSelector, hashCache))
        is Node.SymLink -> SymLink(nodePath, node.name, node.lastModifiedTime, node.destination)
      }
    }
    
    fun filterMetaFiles(src: Top, metafile2Basename: Metafile2Basename): Top {
      return src.copy(children = filterMetaFiles(src.children, metafile2Basename))
    }

    private fun filterMetaFiles(
      children: List<HashTree>,
      basename: Metafile2Basename
    ): List<HashTree> {
      return children.map { filterMetaFiles(it, basename) }
    }

    private fun filterMetaFiles(
      tree: HashTree,
      basename: Metafile2Basename
    ): HashTree {
      return when (tree) {
        is Directory -> {
          val (files, other) = tree.children.partition { it is File }
          val names = files.map { it.name }.toSet()
          val meta2basename = basename.baseNameMap(names)

          val (baseFiles,metaFiles) = files.partition { !meta2basename.contains(it.name) }
          val mappedMetaFiles = metaFiles.map { node ->
            val file = node as File
            MetaFile(file.path, file.name, file.lastModifiedTime, file.size, meta2basename[file.name] ?: throw IllegalArgumentException("could not get baseName for ${file.name}"))
          }

//          println("baseFiles: $baseFiles")
//          println("metaFiles: $metaFiles")
//          println("others: $other")

          tree.copy(children = baseFiles + mappedMetaFiles + filterMetaFiles(other, basename))
        }
        else -> tree
      }
    }
  }
}
