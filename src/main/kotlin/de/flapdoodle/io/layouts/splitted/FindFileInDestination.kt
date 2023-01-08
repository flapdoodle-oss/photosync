package de.flapdoodle.io.layouts.splitted

import de.flapdoodle.io.filetree.Node
import de.flapdoodle.photosync.LastModified
import de.flapdoodle.photosync.filehash.Hash
import de.flapdoodle.photosync.filehash.HashSelector
import de.flapdoodle.photosync.filehash.Hasher
import de.flapdoodle.photosync.progress.Monitor
import java.nio.file.Path

object FindFileInDestination {
  fun find(src: Node.Top, dest: Node.Top, compare: Compare, hashSelector: HashSelector): List<Match> {
    val hasherCache = HashCacheLookup()
    return find(src.path, src.children, dest.path, dest.children, compare, hashSelector, hasherCache)
  }

  private fun find(
    srcPath: Path,
    srcNodes: List<Node>,
    destPath: Path,
    dest: List<Node>,
    compare: Compare,
    hashSelector: HashSelector,
    cacheLookup: HashCacheLookup
  ): List<Match> {
    var matches = listOf<Match>()

    srcNodes.forEach { s ->
      val src = srcPath.resolve(s.name)
      when (s) {
        is Node.File -> {
          Monitor.message("inspect $src")
          val hasher = hashSelector.hasherFor(src, s.size, s.lastModifiedTime)
          val cachedHasher = cacheLookup.cacheFor(hasher)
          val srcHash = cachedHasher.hash(src, s.size, s.lastModifiedTime)
          val destinations = findMatches(s, srcHash, destPath, dest, compare, cachedHasher)
          matches = matches + Match(src, destinations)
        }

        is Node.Directory -> {
          matches = matches + find(src, s.children, destPath, dest, compare, hashSelector, cacheLookup)
        }

        else -> {
          Monitor.message("skip $src")
        }
      }
    }

    return matches
  }

  private fun findMatches(src: Node.File, srcHash: Hash<*>, destPath: Path, destNodes: List<Node>, compare: Compare, hasher: Hasher<*>): List<Destination> {
    var matches= listOf<Destination>()

    Monitor.message("search in $destPath")
    
    destNodes.forEach { d ->
      val dest = destPath.resolve(d.name)
      when (d) {
        is Node.File -> {
          if (src.name == d.name || compare == Compare.ByHashOnly) {
            if (src.size == d.size) {
              val destHash = hasher.hash(dest, d.size, d.lastModifiedTime)
              if (srcHash == destHash) {
                Monitor.message("found match: ${src.name} -> $dest")
                matches = matches + Destination(dest, MatchType.SameContent)
              }
            } else {
              if (compare == Compare.ByName) {
                Monitor.message("different size: ${src.name} (${src.size}) != $dest (${d.size})")
                matches = matches + Destination(dest, MatchType.DifferentSize)
              }
            }
          }
        }
        is Node.Directory -> {
          matches = matches + findMatches(src, srcHash, dest, d.children, compare, hasher)
        }
        else -> {
          Monitor.message("skip $dest")
        }
      }
    }

    return matches
  }

  enum class Compare {
    ByName, ByHashOnly
  }
  data class Match(val src: Path, val dest: List<Destination>)
  data class Destination(val path: Path, val type: MatchType)
  enum class MatchType {
    SameContent, DifferentSize
  }

  class HashCacheLookup() {
    private var hasherCache = mapOf<Hasher<*>, HashCache<*>>()

    fun <T: Hash<T>> cacheFor(hasher: Hasher<T>): HashCache<T> {
      val cache = hasherCache[hasher]
      if (cache!=null) {
        return cache as HashCache<T>
      }
      val newCache = HashCache(hasher)
      hasherCache = hasherCache + (hasher to newCache)
      return newCache
    }
  }

  class HashCache<T: Hash<T>>(val delegate: Hasher<T>): Hasher<T> {
    private var hashCache= mapOf<Pair<Path, Long>, T>()

    override fun hash(path: Path, size: Long, lastModifiedTime: LastModified): T {
      val key = path to size
      val hash = hashCache[key]
      if (hash!=null) {
        return hash
      }
      val newHash = delegate.hash(path, size, lastModifiedTime)
      hashCache = hashCache + (key to newHash)
      return newHash
    }
  }
}