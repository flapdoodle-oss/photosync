package de.flapdoodle.dirsync.ui.config

import java.util.*

class SyncConfig(val entries: List<SyncEntry> = emptyList()) {
    fun entry(id: UUID): SyncEntry {
        val matching = entries.filter { it.id == id }
        require(matching.size == 1)
        return matching[0]
    }
}