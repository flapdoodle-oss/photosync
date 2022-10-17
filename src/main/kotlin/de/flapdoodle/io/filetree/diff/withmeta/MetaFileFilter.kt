package de.flapdoodle.io.filetree.diff.withmeta

interface MetaFileFilter {
  fun filter(map: MetaFileMap): MetaFileMap
}