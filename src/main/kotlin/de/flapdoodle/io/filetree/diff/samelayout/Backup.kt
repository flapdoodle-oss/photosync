package de.flapdoodle.io.filetree.diff.samelayout

import de.flapdoodle.io.filetree.Node
import de.flapdoodle.io.filetree.diff.Action
import java.nio.file.Path

class Backup {
    fun actions(diff: Diff): List<Action> {
        return actions(diff.src, diff.dest, diff.entries)
    }

    private fun actions(src: Path, dest: Path, entries: List<Diff.Entry>): List<Action> {
        return entries.flatMap { actions(src, dest, it) }
    }

    private fun actions(src: Path, dest: Path, entry: Diff.Entry): List<Action> {
        return when(entry) {
            is Diff.Entry.Missing -> {
                return when (entry.src) {
                    is Node.File -> listOf(
                        Action.CopyFile(src.resolve(entry.src.name), src.resolve(entry.src.name), entry.src.size),
                        Action.SetLastModified(src.resolve(entry.src.name), entry.src.lastModifiedTime)
                    )
                    is Node.SymLink -> listOf(
                        TODO("not implemented")
                    )
                    is Node.Directory -> listOf(
                        // and copy everything in there
                        Action.SetLastModified(src.resolve(entry.src.name), entry.src.lastModifiedTime)
                    )
                    else -> emptyList()
                }
            }
            else -> emptyList()
        }
    }
}