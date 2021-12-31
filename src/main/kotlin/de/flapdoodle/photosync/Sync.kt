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
import de.flapdoodle.io.resolve.metainfo.ExpectSameContentDiff2Commands
import de.flapdoodle.io.tree.FileTreeEvent
import de.flapdoodle.io.tree.FileTrees
import de.flapdoodle.io.tree.Tree
import de.flapdoodle.photosync.filehash.*
import de.flapdoodle.photosync.progress.Monitor
import java.nio.file.Path
import java.time.Duration
import java.time.LocalDateTime

object Sync {
    const val MAX_PATH_LEN=70;

    class Args : CliktCommand() {
        init {
            context {
                allowInterspersedArgs = false
            }
        }

        val mode by option(
            "-m", "--mode", help = "mode (default is same-layout)"
        ).groupChoice(
            "same-layout" to SyncMode.SameLayout(),
            "same-time" to SyncMode.SameLayoutLastModified()
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
                srcMetaTree, dstMetaTree, mode.hasher()
            )

            SyncResult(srcMetaTree, dstMetaTree, diff)
        }

        val end = LocalDateTime.now()

        println("- - - - - - - - - - - - - - - - -")
        val commands = ExpectSameContentDiff2Commands.syncCommandsFor(result.src, result.dst, result.diff)
        commands.forEach {
            println(" $it")
        }
//        UnixCommandListRenderer.execute(result.result)
////    val end = LocalDateTime.now()
        println("- - - - - - - - - - - - - - - - -")
        println("Speed: ${Duration.between(start, end).toSeconds()}s")
        println("Source: ${result.srcDiskSpaceUsed / (1024 * 1024)} MB")
        println("Backup: ${result.dstDiskSpaceUsed / (1024 * 1024)} MB")
    }

    private fun humanReadable(event: FileTreeEvent): String {
        return when (event) {
            is FileTreeEvent.Enter -> "scan ${short(event.path)}"
            is FileTreeEvent.Leave -> "done ${short(event.path)}"
            is FileTreeEvent.File -> "file ${short(event.path)}"
            is FileTreeEvent.SymLink -> "symlink ${short(event.path)}"
        }
    }

    private fun short(path: Path): String {
        val fullPath = path.toString()
        return if (fullPath.length > MAX_PATH_LEN) {
            "..."+fullPath.substring(fullPath.length-(MAX_PATH_LEN-3), fullPath.length)
        } else {
            fullPath
        }
    }

    sealed class SyncMode(name: String) : OptionGroup(name) {
        abstract fun hasher(): List<Hasher<*>>
        class SameLayout : SyncMode("options for same layout") {
            override fun hasher(): List<Hasher<*>> = listOf(
                MonitoringHasher(SizeHash) { path, size -> "hash ${short(path)} (size)"},
                MonitoringHasher(QuickHash) { path, size -> "hash ${short(path)} (quickhash)"},
            )
        }

        // TODO does not work as expected
        class SameLayoutLastModified : SyncMode("same modification date") {
            override fun hasher(): List<Hasher<*>> = listOf(
                MonitoringHasher(SizeHash) { path, size -> "hash ${short(path)} (size)"},
                MonitoringHasher(LastModificationHash) { path, size -> "hash ${short(path)} (lastModified)"},
            )
        }
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