package de.flapdoodle.io.filetree.diff.graph

import java.nio.file.Path

class HashTreeDiff(
  val src: Path,
  val dest: Path,
) {

  companion object {
    fun diff(src: HashTree.Top, dest: HashTree.Top): HashTreeDiff {
      val sourceHashMap = GroupedByHash.build(src.children)
      val destHashMap = GroupedByHash.build(dest.children)

      val both = sourceHashMap.keys().intersect(destHashMap.keys())
      val onlySource = sourceHashMap.keys() - destHashMap.keys()
      val onyDest = destHashMap.keys() - sourceHashMap.keys()

//      println("--------------------")
//      sourceHashMap.map.forEach { hash, paths ->
//        println("$hash:$paths")
//      }
//      println("--------------------")
//      destHashMap.map.forEach { hash, paths ->
//        println("$hash:$paths")
//      }
//      println("--------------------")

      println("both: $both")
      println("new: $onlySource")
      println("deleted: $onyDest")

      both.forEach { hash ->
        println("-----------------")
        println("$hash")
        val srcPaths: Set<Path> = sourceHashMap[hash]
        val destPaths: Set<Path> = destHashMap[hash]
        srcPaths.forEach { println("$it -->") }
        destPaths.forEach { println("--> $it") }
      }

//      println("------------------")
//      println(sourceHashMap.map)
//      println("------------------")
//      println(deshHashMap.map)
//      println("------------------")

      return HashTreeDiff(src.path, dest.path)
    }

    fun emptyDiff(src: Path, dest: Path): HashTreeDiff {
      return HashTreeDiff(src, dest)
    }
  }
}