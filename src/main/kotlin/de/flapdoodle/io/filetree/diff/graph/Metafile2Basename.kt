package de.flapdoodle.io.filetree.diff.graph

fun interface Metafile2Basename {
  fun baseNameMap(names: Set<String>): Map<String, String>
}