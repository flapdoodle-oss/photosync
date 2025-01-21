package de.flapdoodle.io.filetree.diff.graph

import de.flapdoodle.photosync.filehash.Hash
import java.nio.file.Path

data class GroupedByHash(
  val map: Map<Hash<*>, Set<Path>> = emptyMap(),
) {

  fun keys() = map.keys

  internal fun add(hash: Hash<*>, path: Path): GroupedByHash {
    val existing = map[hash] ?: emptySet()
    val added = existing + setOf(path)
    return copy(map = map + (hash to added))
  }

  operator fun get(hash: Hash<*>): Set<Path> {
    return map[hash] ?: emptySet<Path>()
  }

  companion object {
    fun build(list: List<HashTree>): GroupedByHash {
      return add(GroupedByHash(), list)
    }

    private fun add(map: GroupedByHash, list: List<HashTree>): GroupedByHash {
      return list.fold(map) { m,tree -> m
        when (tree) {
          is HashTree.File -> m.add(tree.hash, tree.path)
          is HashTree.MetaFile -> m
          is HashTree.Directory -> add(m, tree.children)
          is HashTree.SymLink -> {
            throw IllegalArgumentException("symlink not supported for ${tree.path}")
          }
        }
      }
    }
  }

}