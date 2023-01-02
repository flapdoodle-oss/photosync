package de.flapdoodle.io.layouts.splitted

import de.flapdoodle.io.filetree.Node
import de.flapdoodle.photosync.filehash.Hash
import de.flapdoodle.photosync.filehash.Hasher
import de.flapdoodle.photosync.progress.Monitor
import java.nio.file.Path

object FindDuplicatesByHash {
  fun find(src: Node.Top, hasher: Hasher<*>): Map<Grouped, List<Path>> {
    val groupedBySize = groupBySize(src.path, src.children)
    return splitByHash(groupedBySize, hasher)
  }

  private fun groupBySize(path: Path, children: List<Node>): Map<Long, List<Path>> {
    var groupedBySize = mapOf<Long, List<Path>>()

    children.forEach { c ->
      val nodePath = path.resolve(c.name)
      when (c) {
        is Node.File -> {
          groupedBySize = merge(groupedBySize, mapOf(c.size to listOf(nodePath)))
        }
        is Node.Directory -> {
          groupedBySize = merge(groupedBySize, groupBySize(nodePath, c.children))
        }
        else -> Monitor.message("skip $nodePath")
      }
    }

    return groupedBySize
  }

  private fun splitByHash(map: Map<Long, List<Path>>, hasher: Hasher<*>): Map<Grouped, List<Path>> {
    return buildMap {
      map.forEach { (size, paths) ->
        if (paths.size==1) {
          put(Grouped.BySize(size), paths)
        } else {
          val groupedByHash = paths.groupBy { hasher.hash(it, size) }
          groupedByHash.forEach { (hash, p) ->
            put(Grouped.ByHash(hash), p)
          }
        }
      }
    }
  }


  private fun <K, V> merge(a: Map<K, List<V>>, b: Map<K, List<V>>): Map<K, List<V>> {
    val allKeys = a.keys + b.keys
    return buildMap(allKeys.size) {
      allKeys.forEach { key ->
        put(key, (a[key] ?: emptyList()) + (b[key] ?: emptyList()))
      }
    }
  }

  sealed class Grouped {
    data class BySize(val value: Long): Grouped()
    data class ByHash(val hash: Hash<*>): Grouped()
  }
}