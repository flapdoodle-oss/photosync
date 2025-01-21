package de.flapdoodle.io.filetree.diff.graph

import java.util.regex.Pattern

object DarktableMetafile2Basename : Metafile2Basename {
  // base file -> XYZ.ext --> XYZ_01.ext.fooBar
  private val NAME_EXTENSION = Pattern.compile("(?<name>[^.]+)\\.(?<ext>.+)")
  private val NAME_VERSION = Pattern.compile("(?<name>.+)(?<version>_[0-9]+?)")

  override fun baseNameMap(names: Set<String>): Map<String, String> {
//    val (baseNames,metaFiles) = names.partition { name -> BASE_PATTERN.matcher(name).matches() }
    val parsedFileNames = names.map { fileName ->
      val matcher = NAME_EXTENSION.matcher(fileName)
      if (matcher.matches()) {
        val name = matcher.group("name")
        val nameAndVersion = NAME_VERSION.matcher(name)
        val (baseName, version) = if (nameAndVersion.matches()) {
          nameAndVersion.group("name") to nameAndVersion.group("version")
        } else {
          name to null
        }
        val ext = matcher.group("ext")
        fileName to ParsedFileName(baseName,version, ext.split('.'))
      } else {
        fileName to ParsedFileName(fileName,null,emptyList())
      }
    }

//    println("parsedFileNames:")
//    parsedFileNames.forEach { println("${it.first} -> ${it.second} --> ${it.second.baseNames()}") }

    val metaFiles = parsedFileNames.filter { pair ->  pair.second.baseNames().any { names.contains(it) && pair.first != it } }
//    println("metaFiles: $metaFiles")

    val metaFile2BaseNameMap = metaFiles
      .flatMap { (baseName, parsed) -> parsed.baseNames().map { baseName to it } }
      .filter { (meta, basename) -> names.contains(basename) }
      .toMap()

//    println("map: $metaFile2BaseNameMap")

    return metaFile2BaseNameMap
  }

  data class ParsedFileName(
    val name: String,
    val version: String?,
    val ext: List<String>,
  ) {
    fun baseNames(): List<String> {
      val baseExt = if (ext.size > 1) {
        ".${ext.subList(0, ext.size - 1).joinToString(".")}"
      } else {
        ""
      }
      return if (version!=null)
        listOf(name + baseExt, name + version + baseExt)
      else
        listOf(name + baseExt)
    }
  }
}