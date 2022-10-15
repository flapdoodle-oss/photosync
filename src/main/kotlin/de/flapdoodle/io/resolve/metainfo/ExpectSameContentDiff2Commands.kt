package de.flapdoodle.io.resolve.metainfo

import de.flapdoodle.io.layouts.common.Diff
import de.flapdoodle.io.layouts.common.Diff2Commands
import de.flapdoodle.io.layouts.metainfo.ExpectSameContent
import de.flapdoodle.io.layouts.metainfo.MetaView
import de.flapdoodle.io.resolve.commands.Command
import de.flapdoodle.io.resolve.commands.Intention
import de.flapdoodle.io.tree.Tree
import java.nio.file.Path

object ExpectSameContentDiff2Commands {
    fun syncCommandsFor(
        src: MetaView.Directory,
        dst: MetaView.Directory,
        diff: List<ExpectSameContent.MetaDiff>
    ): List<Command> {
        val diffLookup = diff.flatMap {
            val keys = when (it) {
                is ExpectSameContent.MetaDiff.SourceIsMissing -> listOf(it.dst)
                is ExpectSameContent.MetaDiff.DestinationIsMissing -> listOf(it.src)
                is ExpectSameContent.MetaDiff.Renamed -> listOf(it.src, it.dst)
                is ExpectSameContent.MetaDiff.Moved -> listOf(it.src, it.dst)
                is ExpectSameContent.MetaDiff.ChangeMetaFiles -> listOf(it.src, it.dst)
                is ExpectSameContent.MetaDiff.TypeMissmatch -> listOf(it.src, it.dst)
                is ExpectSameContent.MetaDiff.MultipleMappings -> listOf(it.element)
            }
            keys.map { key -> key to it }
        }.toMap()

        return sourceToDestination(src, diffLookup)
    }

    private fun sourceToDestination(
        src: MetaView.Directory,
        diffLookup: Map<MetaView, ExpectSameContent.MetaDiff>
    ): List<Command> {
        return src.children.flatMap {
            when (it) {
                is MetaView.Directory -> sourceToDestination(it, diffLookup)
                is MetaView.Node -> syncCommandsForNode(it, diffLookup)
            }
        }
    }

    private fun syncCommandsForNode(
        node: MetaView.Node,
        diffLookup: Map<MetaView, ExpectSameContent.MetaDiff>
    ): List<Command> {
        val diff = diffLookup[node]
        return when (diff) {
            is ExpectSameContent.MetaDiff.ChangeMetaFiles -> {
                syncCommandsFor(diff.metaFileDiff)
            }
            is ExpectSameContent.MetaDiff.Moved -> {
                syncCommandsForMove(diff)
            }
            null -> {
                emptyList()
            }
            else -> {
                println("not implemented: $diff")
                emptyList()
            }
        }
    }

    private fun syncCommandsForMove(diff: ExpectSameContent.MetaDiff.Moved): List<Command> {
        println("---------------------")
        println("src: ${diff.src}")
        println("dst: ${diff.dst}")
        println("expectedDestionation: ${diff.expectedDestionation}")
        println("diff: ${diff.metaFileDiff}")
        println("---------------------")

        val empty = emptyList<Command>()
        // move dst to expected destination
        return empty + diff.dst.base.moveTo(diff.expectedDestionation)
        // sync diff items
    }

    private fun Tree.moveTo(destination: Path): Command {
        return when (this) {
            is Tree.File -> Command.Move(this.path, destination, Intention.UpdateDestination)
            else -> throw IllegalArgumentException("not implemented: $this")
        }
    }
    private fun syncCommandsFor(src: List<Diff>): List<Command> {
        return src.flatMap(Diff2Commands::syncCommandsFor)
    }
}

