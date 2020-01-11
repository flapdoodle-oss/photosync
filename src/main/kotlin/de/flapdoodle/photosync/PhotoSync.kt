package de.flapdoodle.photosync

import de.flapdoodle.photosync.analyze.GroupMetaData
import de.flapdoodle.photosync.analyze.GroupSameContent
import de.flapdoodle.photosync.diff.ScanDiffAnalyzer
import de.flapdoodle.photosync.collector.BlobCollector
import de.flapdoodle.photosync.collector.DumpingPathCollector
import de.flapdoodle.photosync.collector.FileVisitorAdapter
import de.flapdoodle.photosync.diff.Scan
import de.flapdoodle.photosync.filehash.HashStrategy
import de.flapdoodle.photosync.filehash.QuickHash
import de.flapdoodle.photosync.report.DiffAsCommandsReporter
import java.nio.file.Files
import java.nio.file.Path

object PhotoSync {

  @JvmStatic
  fun main(vararg args: String) {
    require(args.size > 1) { "usage: <src> <dst>" }

    val src = scan(Path.of(args[0]))
    val dst = scan(Path.of(args[1]))

    println("---------------------")
    println(" Scan Diff")
    println("---------------------")

    val diff = ScanDiffAnalyzer.scan(src, dst, QuickHash)

    println("---------------------")
    println(" Result")
    println("---------------------")

    DiffAsCommandsReporter(src.path, dst.path).generate(diff)
  }

  private fun scan(
      path: Path,
      hashStrategy: HashStrategy = HashStrategy { listOf(QuickHash) }
  ): Scan {
    val dumpCollector = DumpingPathCollector()
    val blobCollector = BlobCollector()
    Files.walkFileTree(path, FileVisitorAdapter(blobCollector.andThen(dumpCollector)))

    dumpCollector.report()
    println("-----------------------")

    val groupMeta = GroupMetaData(blobCollector.blobs())

    blobCollector.blobs().forEach {
      println("${it.path} -> ${groupMeta.isMeta(it)}")
    }

    val groupedByContent = GroupSameContent(
        blobs = groupMeta.baseBlobs(),
        hashStrategy = hashStrategy
    )

    println("--------------------------\n")
    println("unique blobs: ")
    groupedByContent.uniqueBlobs().forEach {
      println("${it.path}")
    }

    println("--------------------------\n")
    println("blobs with multiple locations: ")
    groupedByContent.collisions().forEach { (h, blobs) ->
      println("- - - - - - - - \n")
      println("hash: $h")
      blobs.forEach {
        println("${it.path}")
      }
    }

    return Scan.of(path, groupedByContent, groupMeta)
  }

}