package de.flapdoodle.io.filetree.diff

object Sync {
  enum class Copy {
    IF_CHANGED, // any change -> copy
    ONLY_NEW // any change -> copy only if new
  }

  enum class Leftover {
    IGNORE, DELETE, COPY_BACK
  }
}