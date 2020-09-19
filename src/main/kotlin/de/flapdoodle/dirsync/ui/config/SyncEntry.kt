package de.flapdoodle.dirsync.ui.config

import java.util.*

data class SyncEntry(
        val id: UUID = UUID.randomUUID(),
        val src: String,
        val dst: String,
        val excludes: List<String> = emptyList()
) {
}