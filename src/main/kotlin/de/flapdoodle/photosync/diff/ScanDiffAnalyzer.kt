package de.flapdoodle.photosync.diff

import de.flapdoodle.photosync.filehash.HashStrategy
import de.flapdoodle.photosync.filehash.SizeHasher

class ScanDiffAnalyzer(
    src: Scan,
    dst: Scan,
    hashStrategy: HashStrategy
) {

  init {
    val strategy = HashStrategy { listOf(SizeHasher()) + hashStrategy.hasher() }

    val srcMap = src.blobs().associateBy { it.any() }
    val dstMap = dst.blobs().associateBy { it.any() }

    val srcGroupedByHash = HashStrategy.groupBlobs(strategy, srcMap.keys)
    val dstGroupedByHash = HashStrategy.groupBlobs(strategy, dstMap.keys)

    val srcDiff = srcGroupedByHash.map { (hash, list) ->
      require(list.size == 1) { "multiple entries not expected: $hash -> $list" }

      val matchedDst = dstGroupedByHash[hash]
      require(matchedDst==null || matchedDst.size==1) { "multiple entries not expected: $hash -> $matchedDst" }

      if (matchedDst!=null) {
        println("found ${list.single()} -> ${matchedDst.single()}")
        Match(srcMap[list.single()]!!, dstMap[matchedDst.single()]!!)
      } else {
        println("missing ${list.single()}")
        NewEntry(srcMap[list.single()]!!)
      }
    }

    val dstDiff = dstGroupedByHash.map { (hash, list) ->
      require(list.size == 1) { "multiple entries not expected: $hash -> $list" }

      val matchedSrc = srcGroupedByHash[hash]
      require(matchedSrc==null || matchedSrc.size==1) { "multiple entries not expected: $hash -> $matchedSrc" }

      if (matchedSrc!=null) {
        //println("found ${matchedSrc.single()} -> ${list.single()}")
        Noop
      } else {
        println("deleted ${list.single()}")
        DeletedEntry(dstMap[list.single()]!!)
      }
    }

    val diff = srcDiff + dstDiff

    diff.forEach {
      println("--------------------------")
      when (it) {
        is Match -> println("match: $it")
        is NewEntry -> println("new: $it")
        is DeletedEntry -> println("delete: $it")
        is Noop -> {}
      }
    }
  }

  interface Entry

  data class Match(
      val src: GroupedBlobs,
      val dst: GroupedBlobs
  ) : Entry

  data class NewEntry(
      val src: GroupedBlobs
  ) : Entry

  data class DeletedEntry(
      val dst: GroupedBlobs
  ) : Entry

  object Noop : Entry
}