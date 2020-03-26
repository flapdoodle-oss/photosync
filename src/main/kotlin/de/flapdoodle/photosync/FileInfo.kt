package de.flapdoodle.photosync

import org.apache.tika.detect.DefaultDetector
import org.apache.tika.metadata.Metadata
import java.io.File
import java.nio.file.Path

object FileInfo {

  @JvmStatic
  fun main(vararg args: String) {
    require(args.size >= 1) { "usage: <file>" }

    val detector = DefaultDetector()
    val file = Path.of(args[0]).toFile()

    file.inputStream().buffered().use {
      val metadata = Metadata()
      metadata.set(Metadata.RESOURCE_NAME_KEY, args[0]);
      val mimeType = detector.detect(it, metadata)
      println("mimeType: $mimeType")
    }

    println("lastModified: ${file.lastModified()}")
  }
}