package de.flapdoodle.io.filetree.diff.withmeta

import java.util.regex.Pattern

object GroupCommonMetaFiles : GroupMetaFiles {
  private val FILE_WITH_EXTENSIONS= Pattern.compile("(?<name>[^.]+)\\.(?<ext>.+?)(\\.(?<opt>.+))?")

  override fun group(src: MetaFileMap): MetaFileMap {
    var current = src

    val baseFiles = src.baseNames().flatMap { name ->
      val (baseName, ext, opt) = nameAndExtensions(name)
      if (opt == null) listOf(baseName to ext) else emptyList()
    }

    src.baseNames().forEach { name ->
      val (baseName, ext, opt) = nameAndExtensions(name)
      if (opt != null) {
        val match = baseFiles.firstOrNull { entry ->
          baseName.startsWith(entry.first) && ext==entry.second
        }
        if (match != null) {
          current = current.move(name, "${match.first}.${match.second}")
        }
      }
    }

    return current
  }

  internal fun nameAndExtensions(src: String): Triple<String,String?,String?> {
    val matcher = FILE_WITH_EXTENSIONS.matcher(src)
    return if (matcher.matches()) {
      Triple(matcher.group("name"), matcher.group("ext"), matcher.group("opt"))
    } else {
      Triple(src, null, null)
    }
  }
}