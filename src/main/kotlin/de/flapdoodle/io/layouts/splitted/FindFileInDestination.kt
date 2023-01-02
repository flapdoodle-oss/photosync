package de.flapdoodle.io.layouts.splitted

import de.flapdoodle.io.filetree.Node
import de.flapdoodle.photosync.filehash.Hash
import de.flapdoodle.photosync.filehash.HashSelector
import de.flapdoodle.photosync.filehash.Hasher
import de.flapdoodle.photosync.progress.Monitor
import java.nio.file.Path

object FindFileInDestination {
  fun find(src: Node.Top, dest: Node.Top, hashSelector: HashSelector): List<Match> {
    return find(src.path, src.children, dest.path, dest.children, hashSelector)
  }

  private fun find(srcPath: Path, src: List<Node>, destPath: Path, dest: List<Node>, hashSelector: HashSelector): List<Match> {
    var matches = listOf<Match>()

    src.forEach { s ->
      val src = srcPath.resolve(s.name)
      when (s) {
        is Node.File -> {
          Monitor.message("inspect $src")
          val hasher = hashSelector.hasherFor(src)
          val srcHash = hasher.hash(src, s.size)
          val destinations = findMatches(s, srcHash, destPath, dest, hasher)
          matches = matches + Match(src, destinations)
        }

        is Node.Directory -> {
          matches = matches + find(src, s.children, destPath, dest, hashSelector)
        }

        else -> {
          Monitor.message("skip $src")
        }
      }
    }

    return matches
  }

  private fun findMatches(src: Node.File, srcHash: Hash<*>, destPath: Path, dest: List<Node>, hasher: Hasher<*>): List<Path> {
    var matches= listOf<Path>()

    Monitor.message("search in $destPath")
    
    dest.forEach { d ->
      val dest = destPath.resolve(d.name)
      when (d) {
        is Node.File -> {
          if (src.name == d.name && src.size == d.size) {
            val destHash = hasher.hash(dest, d.size)
            if (srcHash == destHash) {
              Monitor.message("found match: ${src.name} -> $dest")
              matches = matches.plusElement(dest)
            }
          }
        }
        is Node.Directory -> {
          matches = matches + findMatches(src, srcHash, dest, d.children, hasher)
        }
        else -> {
          Monitor.message("skip $dest")
        }
      }
    }

    return matches
  }

  data class Match(val src: Path, val dest: List<Path>)
}