package de.flapdoodle.io.layouts.splitted

import de.flapdoodle.io.filetree.Node
import de.flapdoodle.photosync.LastModified
import de.flapdoodle.photosync.filehash.FullHash
import de.flapdoodle.photosync.filehash.Hash
import de.flapdoodle.photosync.filehash.MonitoringHasher
import de.flapdoodle.photosync.filehash.SizedQuickHash
import de.flapdoodle.photosync.progress.Monitor
import java.nio.file.Path

object FindDuplicatesByHash {
  val hasher = MonitoringHasher(SizedQuickHash)
  val safeHasher = MonitoringHasher(FullHash)

  fun find(src: Node.Top, safeHash: Boolean): Map<Grouped, List<Path>> {
    val groupedBySize = groupBySize(src.path, src.children)
    return splitByHash(groupedBySize, safeHash)
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

  private fun splitByHash(map: Map<Long, List<Path>>, safeHash: Boolean): Map<Grouped, List<Path>> {
    return buildMap {
      map.forEach { (size, paths) ->
        if (paths.size==1) {
          put(Grouped.BySize(size), paths)
        } else {
          val groupedByHash = paths.groupBy { hasher.hash(it, size, LastModified.from(it)) }
          groupedByHash.forEach { (hash, grouped) ->
            if (grouped.size>1 && safeHash) {
              val hashedAgain = grouped.groupBy { safeHasher.hash(it, size, LastModified.from(it)) }
              hashedAgain.forEach { (fullHash, compared) ->
                put(Grouped.ByHash(fullHash), compared)
              }
            } else {
              put(Grouped.ByHash(hash), grouped)
            }
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