package de.flapdoodle.photosync

import de.flapdoodle.photosync.analyze.GroupMetaData
import de.flapdoodle.photosync.analyze.GroupSameContent
import de.flapdoodle.photosync.diff.ScanDiffAnalyzer
import de.flapdoodle.photosync.collector.BlobCollector
import de.flapdoodle.photosync.collector.FileVisitorAdapter
import de.flapdoodle.photosync.collector.ProgressReportPathCollector
import de.flapdoodle.photosync.diff.Scan
import de.flapdoodle.photosync.filehash.HashStrategy
import de.flapdoodle.photosync.filehash.QuickHash
import de.flapdoodle.photosync.sync.Diff2SyncCommands
import de.flapdoodle.photosync.sync.UnixCommandListRenderer
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.time.LocalDateTime

object PhotoSync {

  @JvmStatic
  fun main(vararg args: String) {
    require(args.size > 1) { "usage: <src> <dst>" }

    val start = LocalDateTime.now()

    println("---------------------")
    println(" Scan")
    println("---------------------")

    val src = scan(Path.of(args[0]))
    val dst = scan(Path.of(args[1]))

    println("---------------------")
    println(" Diff")
    println("---------------------")

    val diff = ScanDiffAnalyzer.scan(src, dst, QuickHash)

    println("---------------------")
    println(" Sync")
    println("---------------------")

    val commands = Diff2SyncCommands(src.path, dst.path).generate(diff)

    UnixCommandListRenderer.execute(commands)

    val end = LocalDateTime.now()

    println("---------------------")
    println(" Stats")
    println("---------------------")
    println("Speed: ${Duration.between(start, end).toSeconds()}s")
    println("Source: ${src.diskSpaceUsed() / (1024 * 1024)} MB")
    println("Backup: ${dst.diskSpaceUsed() / (1024 * 1024)} MB")

  }

  private fun scan(
      path: Path,
      hashStrategy: HashStrategy = HashStrategy { listOf(QuickHash) }
  ): Scan {
    //val dumpCollector = DumpingPathCollector()
    val blobCollector = BlobCollector()
    Files.walkFileTree(path, FileVisitorAdapter(blobCollector.andThen(ProgressReportPathCollector())))

//    dumpCollector.report()
//    println("-----------------------")

    val groupMeta = GroupMetaData(blobCollector.blobs())

//    blobCollector.blobs().forEach {
//      println("${it.path} -> ${groupMeta.isMeta(it)}")
//    }

    val groupedByContent = GroupSameContent(
        blobs = groupMeta.baseBlobs(),
        hashStrategy = hashStrategy
    )

//    println("--------------------------\n")
//    println("unique blobs: ")
//    groupedByContent.uniqueBlobs().forEach {
//      println("${it.path}")
//    }

//    println("--------------------------\n")
//    println("blobs with multiple locations: ")
//    groupedByContent.collisions().forEach { (h, blobs) ->
//      println("- - - - - - - - \n")
//      println("hash: $h")
//      blobs.forEach {
//        println("${it.path}")
//      }
//    }

    return Scan.of(path, groupedByContent, groupMeta)
  }

}