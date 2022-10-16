package de.flapdoodle.io.filetree.diff.withmeta

interface MetaFileFilter {
  fun metaFileNames(map: MetaFileMap): MetaFileMap
}