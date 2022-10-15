package de.flapdoodle.io.layouts.common

import de.flapdoodle.io.Check.fileSizeIs
import de.flapdoodle.io.Check.wasLastModifiedAt
import de.flapdoodle.io.resolve.commands.Command
import de.flapdoodle.io.resolve.commands.Intention
import de.flapdoodle.io.resolve.commands.Precondition.Companion.expectDestination
import de.flapdoodle.io.resolve.commands.Precondition.Companion.expectSource
import de.flapdoodle.io.resolve.commands.SyncCommandMapper
import de.flapdoodle.photosync.Comparision
import de.flapdoodle.photosync.compare

object Diff2Commands : SyncCommandMapper<Diff> {

    override fun syncCommandsFor(entry: Diff): List<Command> {
        return when (entry) {
            is Diff.TypeMismatch -> listOf(
                Command.Manual(entry.src.path, entry.dst.path, "type mismatch", Intention.UpdateDestination)
            )
            is Diff.TimeStampMissmatch ->
                when (entry.src.lastModified.compare(entry.dst.lastModified)) {
                    Comparision.Bigger, Comparision.Equal -> listOf(
                        Command.CopyTimestamp(
                            entry.src.path,
                            entry.dst.path,
                            Intention.UpdateDestination,
                            expectSource(wasLastModifiedAt(entry.src.lastModified))
                        )
                    )
                    else -> listOf(
                        Command.CopyTimestamp(
                            entry.dst.path,
                            entry.src.path,
                            Intention.UpdateSource,
                            expectSource(wasLastModifiedAt(entry.dst.lastModified))
                        )
                    )
                }
            is Diff.ContentMismatch ->
                when (entry.src.lastModified.compare(entry.dst.lastModified)) {
                    Comparision.Bigger, Comparision.Equal -> listOf(
                        Command.Copy(
                            entry.src.path,
                            entry.dst.path,
                            Intention.UpdateDestination,
                            expectSource(
                                fileSizeIs(entry.src.size) +
                                        wasLastModifiedAt(entry.src.lastModified)
                            )
                        )
                    )
                    else -> listOf(
                        Command.Copy(
                            entry.dst.path,
                            entry.src.path,
                            Intention.UpdateSource,
                            expectSource(
                                fileSizeIs(entry.dst.size) +
                                        wasLastModifiedAt(entry.dst.lastModified)
                            )
                                    + expectDestination(
                                wasLastModifiedAt(entry.src.lastModified)
                            )
                        )
                    )
                }
            else -> {
                println("---------------------")
                println("not implemented: $entry")
                println("---------------------")
                emptyList()
            }
        }
    }
}