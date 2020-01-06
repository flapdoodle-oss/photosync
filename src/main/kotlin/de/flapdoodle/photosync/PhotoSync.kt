package de.flapdoodle.photosync

import de.flapdoodle.photosync.analyze.GroupMetaData
import de.flapdoodle.photosync.analyze.GroupSameContent
import de.flapdoodle.photosync.collector.BlobCollector
import de.flapdoodle.photosync.collector.DumpingPathCollector
import de.flapdoodle.photosync.collector.FileVisitorAdapter
import java.nio.file.Files
import java.nio.file.Path
import kotlin.streams.toList

object PhotoSync {

  @JvmStatic
  fun main(vararg args: String) {
    require(args.isNotEmpty()) { "usage: <path>" }

    val path = args[0]

    val dumpCollector = DumpingPathCollector()
    val blobCollector = BlobCollector()
    Files.walkFileTree(Path.of(path), FileVisitorAdapter(blobCollector.andThen(dumpCollector)))

    dumpCollector.report()
    println("-----------------------")

    val groupMeta = GroupMetaData(blobCollector.blobs())

    blobCollector.blobs().forEach {
      println("${it.path} -> ${groupMeta.isMeta(it)}")
    }

    GroupSameContent(groupMeta.baseBlobs())
  }
}