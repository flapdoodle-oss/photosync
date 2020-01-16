package de.flapdoodle.photosync.diff

sealed class DiffEntry {
  data class Match(
      val src: GroupedBlobs,
      val dst: GroupedBlobs
  ) : DiffEntry()

  data class NewEntry(
      val src: GroupedBlobs
  ) : DiffEntry()

  data class DeletedEntry(
      val dst: GroupedBlobs
  ) : DiffEntry()

  object Noop : DiffEntry()
}