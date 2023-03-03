package de.flapdoodle.io.filetree.diff.withmeta

interface GroupMetaFiles {
  fun group(src: MetaFileMap): MetaFileMap
}