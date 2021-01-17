package de.flapdoodle.photosync

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.validate
import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.groups.groupChoice
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.path
import de.flapdoodle.io.layouts.metainfo.*
import de.flapdoodle.io.tree.FileTreeEvent
import de.flapdoodle.io.tree.FileTrees
import de.flapdoodle.io.tree.Tree
import de.flapdoodle.photosync.filehash.MonitoringHasher
import de.flapdoodle.photosync.filehash.QuickHash
import de.flapdoodle.photosync.filehash.SizeHash
import de.flapdoodle.photosync.progress.Monitor
import java.nio.file.Path
import java.time.Duration
import java.time.LocalDateTime
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension

object Sync {
    class Args : CliktCommand() {
        init {
            context {
                allowInterspersedArgs = false
            }
        }

        val mode by option(
            "-m", "--mode", help = "mode (default is same-layout)"
        ).groupChoice(
            "same-layout" to SyncMode.SameLayout()
        )

        val source by argument("source")
            .path(
                mustExist = true,
                canBeFile = false,
                canBeDir = true
            ).validate {
                require(it.toFile().isDirectory) { "is not a directory" }
            }

        val destination by argument("destination")
            .path(
                mustExist = true,
                canBeFile = false,
                canBeDir = true
            ).validate {
                require(it.toFile().isDirectory) { "is not a directory" }
            }

        //        val pattern by option(help = "file pattern regex").convert {
//            Pattern.compile(it)
//        }
//
        override fun run() {
            sync(source, destination, mode ?: SyncMode.SameLayout())
        }
    }

    @JvmStatic
    fun main(vararg args: String) {
        Args().main(args.toList())
    }

    private fun sync(
        source: Path,
        destination: Path,
        mode: SyncMode
    ) {
        val start = LocalDateTime.now()

        val result = Monitor.execute(Monitor.ConsoleReporter()) {
            val srcTree = FileTrees.asTree(source, listener = {
                Monitor.message("source ${humanReadable(it)}")
            })
            val dstTree = FileTrees.asTree(destination, listener = {
                Monitor.message("destination ${humanReadable(it)}")
            })
            Monitor.message("DONE")

            val srcMetaTree = MetaView.map(srcTree, GroupMetaFiles.default())
            val dstMetaTree = MetaView.map(dstTree, GroupMetaFiles.default());

            val diff = ExpectSameContent.diff(
                srcMetaTree, dstMetaTree, listOf(
                    MonitoringHasher(SizeHash),
                    MonitoringHasher(QuickHash)
                )
            )

            SyncResult(srcMetaTree, dstMetaTree, diff)
        }

        val end = LocalDateTime.now()

        println("- - - - - - - - - - - - - - - - -")
        println("Changed: ${result.diff.size}")
//        UnixCommandListRenderer.execute(result.result)
////    val end = LocalDateTime.now()
        println("- - - - - - - - - - - - - - - - -")
        println("Speed: ${Duration.between(start, end).toSeconds()}s")
        println("Source: ${result.srcDiskSpaceUsed / (1024 * 1024)} MB")
        println("Backup: ${result.dstDiskSpaceUsed / (1024 * 1024)} MB")
    }

    private fun humanReadable(event: FileTreeEvent): String {
        when (event) {
            is FileTreeEvent.Enter -> "scan ${short(event.path)}"
            is FileTreeEvent.Leave -> "done ${short(event.path)}"
            is FileTreeEvent.File -> "file ${short(event.path)}"
            is FileTreeEvent.SymLink -> "symlink ${short(event.path)}"
        }
        return event.toString()
    }

    private fun short(path: Path): String {
        val fullPath = path.toString()
        val fileName = path.fileName.toString()
        val leftForPath = 60 - fileName.length - 3

        return if (fullPath.length > leftForPath + fileName.length) {
            fullPath.substring(0, leftForPath) + "..." + fileName
        } else fullPath
    }

    sealed class SyncMode(name: String) : OptionGroup(name) {
        class SameLayout : SyncMode("options for same layout")
    }

    class SyncResult(
        val src: MetaView.Directory,
        val dst: MetaView.Directory,
        val diff: List<ExpectSameContent.MetaDiff>
    ) {
        val srcDiskSpaceUsed = src.fold(0L) { f, it -> f + it.fileSizes() }
        val dstDiskSpaceUsed = dst.fold(0L) { f, it -> f + it.fileSizes() }
    }

    fun MetaView.Node.fileSizes(): Long {
        return when (this.base) {
            is Tree.File -> this.base.size
            else -> 0L
        } + this.metaFiles.fold(0L) { folded, it ->
            when (it) {
                is Tree.File -> folded + it.size
                else -> folded
            }
        }
    }
}