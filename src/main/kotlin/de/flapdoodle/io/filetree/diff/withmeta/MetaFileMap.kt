package de.flapdoodle.io.filetree.diff.withmeta

data class MetaFileMap(private val map: Map<String, Set<String>>) {
  fun asMap(): Map<String, Set<String>> {
    return map
  }

  fun baseNames(): Set<String> = map.keys

  fun move(metaName: String, baseName: String): MetaFileMap {
    val metaNameMetaFiles = map[metaName]

    require(metaNameMetaFiles != null) { "not found: $metaName" }
    require(metaNameMetaFiles.isEmpty()) { "$metaName is not empty: $metaNameMetaFiles" }

    val baseNameMetaFiles = map[baseName]
    require(baseNameMetaFiles != null) { "not found: $baseName" }

    return MetaFileMap(map = map - metaName + (baseName to (baseNameMetaFiles + metaName)))
  }

  companion object {
    fun of(names: Set<String>): MetaFileMap {
      return MetaFileMap(names.map { it to emptySet<String>() }.toMap())
    }

    fun of(vararg names: String): MetaFileMap {
      return of(setOf(*names))
    }
  }
}