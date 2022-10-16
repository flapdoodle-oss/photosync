package de.flapdoodle.io.filetree.diff.withmeta

class DarktableMetaFileFilter : MetaFileFilter {

  override fun metaFileNames(map: MetaFileMap): MetaFileMap {
    val parsedNames = map.baseNames().map {
      ParsedDarktableFileName.parse(it) to it
    }

    val metaFileToBaseName = parsedNames
      .map { it.second to it.first.baseName() }
      .filter { it.first != it.second }
      .toMap()

    var current = map

    metaFileToBaseName.forEach { (metaName, baseName) ->
      current = current.move(metaName, baseName)
    }



//    println("--> $parsedNames")
//    println("--> $metaFileToBaseName")
//    //val baseFiles = names.filter { !META_PATTERN.matcher(it).matches() && BASE_PATTERN.matcher(it).matches() }
//    //println("baseFiles: $baseFiles")
////    val baseFiles = names.filter { BASE_PATTERN.matcher(it).matches() }
////    val left = names - baseFiles
////    val metaFiles = left.filter { META_PATTERN.matcher(it).matches() }

    return current
  }

}