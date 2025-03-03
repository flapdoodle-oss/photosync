package de.flapdoodle.io.filetree.diff.graph

import de.flapdoodle.photosync.filehash.Hash
import java.nio.file.Path

class HashTreeDiff(
  val src: HashTree.Top,
  val dest: HashTree.Top,
  val sourceHashMap: Map<Hash<*>, Set<Path>>,
  val destinationHashMap: Map<Hash<*>, Set<Path>>,
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

//      println("both: $both")
//      println("new: $onlySource")
//      println("deleted: $onyDest")

//      both.forEach { hash ->
//        println("-----------------")
//        println("$hash")
//        val srcPaths: Set<Path> = sourceHashMap[hash]
//        val destPaths: Set<Path> = destHashMap[hash]
//        srcPaths.forEach { println("$it -->") }
//        destPaths.forEach { println("--> $it") }
//      }

//      println("------------------")
//      println(sourceHashMap.map)
//      println("------------------")
//      println(deshHashMap.map)
//      println("------------------")

      return HashTreeDiff(src, dest, sourceHashMap.map, destHashMap.map)
    }
  }
}