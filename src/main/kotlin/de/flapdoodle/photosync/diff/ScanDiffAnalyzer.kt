package de.flapdoodle.photosync.diff

import de.flapdoodle.photosync.filehash.HashStrategy
import de.flapdoodle.photosync.filehash.Hasher
import de.flapdoodle.photosync.filehash.SizeHasher

object ScanDiffAnalyzer {

  fun scan(src: Scan,
           dst: Scan,
           hasher: Hasher<*>
  ): List<DiffEntry> {
    //val strategy = HashStrategy { listOf(SizeHasher()) + hashStrategy.hasher() }

    val srcMap = src.blobs().associateBy { it.any() }
    val dstMap = dst.blobs().associateBy { it.any() }

    val srcGroupedByHash = HashStrategy.groupBlobs(hasher, srcMap.keys)
    val dstGroupedByHash = HashStrategy.groupBlobs(hasher, dstMap.keys)

    val srcDiff = srcGroupedByHash.map { (hash, list) ->
      require(list.size == 1) { "multiple entries not expected: $hash -> $list" }

      val matchedDst = dstGroupedByHash[hash]
      require(matchedDst==null || matchedDst.size==1) { "multiple entries not expected: $hash -> $matchedDst" }

      if (matchedDst!=null) {
        println("found ${list.single()} -> ${matchedDst.single()}")
        DiffEntry.Match(srcMap[list.single()]!!, dstMap[matchedDst.single()]!!)
      } else {
        println("missing ${list.single()}")
        DiffEntry.NewEntry(srcMap[list.single()]!!)
      }
    }

    val dstDiff = dstGroupedByHash.map { (hash, list) ->
      require(list.size == 1) { "multiple entries not expected: $hash -> $list" }

      val matchedSrc = srcGroupedByHash[hash]
      require(matchedSrc==null || matchedSrc.size==1) { "multiple entries not expected: $hash -> $matchedSrc" }

      if (matchedSrc!=null) {
        //println("found ${matchedSrc.single()} -> ${list.single()}")
        DiffEntry.Noop
      } else {
        println("deleted ${list.single()}")
        DiffEntry.DeletedEntry(dstMap[list.single()]!!)
      }
    }

    val diff = srcDiff + dstDiff

    diff.forEach {
      println("--------------------------")
      when (it) {
        is DiffEntry.Match -> println("match: $it")
        is DiffEntry.NewEntry -> println("new: $it")
        is DiffEntry.DeletedEntry -> println("delete: $it")
        is DiffEntry.Noop -> {}
      }
    }

    return diff
  }

}