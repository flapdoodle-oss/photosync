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
    Path.of(args[0]).toFile().inputStream().use {
      val metadata = Metadata()
      metadata.set(Metadata.RESOURCE_NAME_KEY, args[0]);
      detector.detect(it, metadata)
    }
  }
}