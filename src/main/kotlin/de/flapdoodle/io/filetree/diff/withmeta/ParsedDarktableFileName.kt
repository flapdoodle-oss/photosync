package de.flapdoodle.io.filetree.diff.withmeta

import java.util.regex.Pattern

/**
 *   // samples
 *   // base -> IMG_1234.CR2
 *   // meta -> IMG_1234.CR2.xmp
 *   //      -> IMG_1234_01.CR2.xmp
 */
data class ParsedDarktableFileName(
  val filename: String,
  val version: String?,
  val extension: List<String>
) {

  fun baseName(): String {
    return if (extension.isNotEmpty())
      "$filename.${extension[0]}"
    else
      filename
  }

  companion object {
    private val BASE_FILENAME= Pattern.compile("(?<name>[^.]+)\\.(?<ext>.+)")
    private val VERSIONED_FILENAME= Pattern.compile("(?<name>.+?)(?<version>_[0-9]+)")

    fun parse(name: String): ParsedDarktableFileName {
      val matcher = BASE_FILENAME.matcher(name)
      return if (matcher.matches()) {
        val baseName = matcher.group("name")
        val extension = matcher.group("ext")
        val versionMatcher = VERSIONED_FILENAME.matcher(baseName)
        if (versionMatcher.matches()) {
          val baseNameWithoutVersion = versionMatcher.group("name")
          val version = versionMatcher.group("version")
          ParsedDarktableFileName(baseNameWithoutVersion, version, extension.split('.'))
        } else {
          ParsedDarktableFileName(baseName, null, extension.split('.'))
        }
      } else {
        ParsedDarktableFileName(name, null, emptyList())
      }
    }
  }
}