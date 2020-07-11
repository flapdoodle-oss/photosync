package de.flapdoodle.photosync.ui.config

import java.util.*

data class SyncEntry(
        val id: UUID = UUID.randomUUID(),
        val src: String,
        val dst: String
)