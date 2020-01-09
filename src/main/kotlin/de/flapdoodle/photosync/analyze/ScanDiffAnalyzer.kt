package de.flapdoodle.photosync.analyze

import de.flapdoodle.photosync.Scan
import de.flapdoodle.photosync.filehash.HashStrategy
import de.flapdoodle.photosync.filehash.SizeHasher

class ScanDiffAnalyzer(
    src: Scan,
    dst: Scan,
    hashStrategy: HashStrategy
) {
  init {
    val strategy = HashStrategy { listOf(SizeHasher()) + hashStrategy.hasher() }

    val srcGroupedByHash = HashStrategy.groupBlobs(strategy, src.blobs())
    val dstGroupedByHash = HashStrategy.groupBlobs(strategy, dst.blobs())

    srcGroupedByHash.forEach { (hash, list) ->
      require(list.size == 1) { "multiple entries not expected: $hash -> $list" }

      val matchedDst = dstGroupedByHash[hash]
      require(matchedDst==null || matchedDst.size==1) { "multiple entries not expected: $hash -> $matchedDst" }

      if (matchedDst!=null) {
        println("found ${list.single()} -> ${matchedDst.single()}")
      } else {
        println("missing ${list.single()}")
      }
    }

    dstGroupedByHash.forEach { (hash, list) ->
      require(list.size == 1) { "multiple entries not expected: $hash -> $list" }

      val matchedSrc = srcGroupedByHash[hash]
      require(matchedSrc==null || matchedSrc.size==1) { "multiple entries not expected: $hash -> $matchedSrc" }

      if (matchedSrc!=null) {
        //println("found ${matchedSrc.single()} -> ${list.single()}")
      } else {
        println("deleted ${list.single()}")
      }
    }
  }
}