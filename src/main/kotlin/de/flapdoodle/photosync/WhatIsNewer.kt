package de.flapdoodle.photosync

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.validate
import com.github.ajalt.clikt.parameters.types.path
import de.flapdoodle.io.tree.FileTreeEvent
import de.flapdoodle.io.tree.FileTrees
import de.flapdoodle.io.tree.Tree
import de.flapdoodle.photosync.progress.Monitor
import java.nio.file.Path
import java.time.Duration
import java.time.LocalDateTime

object WhatIsNewer {
    const val MAX_PATH_LEN=70;

    class Args : CliktCommand() {
        init {
            context {
                allowInterspersedArgs = false
            }
        }

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
            inspect(source, destination)
        }
    }

    @JvmStatic
    fun main(vararg args: String) {
        Args().main(args.toList())
    }

    private fun inspect(
        source: Path,
        destination: Path
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

            val lastModifiedSourceMap = newestChild(srcTree)
            val lastModifiedDestMap = newestChild(dstTree)

            Result(srcTree, lastModifiedSourceMap, dstTree, lastModifiedDestMap)
        }

        val end = LocalDateTime.now()

        println("- - - - - - - - - - - - - - - - -")
        result.explain();
        println("- - - - - - - - - - - - - - - - -")
        println("Speed: ${Duration.between(start, end).toSeconds()}s")
    }

    private fun newestChild(tree: Tree.Directory): Map<Tree, LastModified?> {
        return tree.children.map { it to latestTimeStamp(it) }
            .toMap()
    }

    private fun latestTimeStamp(tree: Tree): LastModified? {
        return when(tree) {
            is Tree.File -> tree.lastModified
            is Tree.SymLink -> tree.lastModified
            is Tree.Directory -> tree.children.mapNotNull(this::latestTimeStamp)
                .maxOrNull()
        }
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

    class Result(
        val src: Tree.Directory,
        val lastModifiedSourceMap: Map<Tree, LastModified?>,
        val dst: Tree.Directory,
        val lastModifiedDestMap: Map<Tree, LastModified?>
    ) {
        fun explain() {
            val sourceByFilename = lastModifiedSourceMap.mapKeys { it.key.path.fileName }
            val destByFilename = lastModifiedDestMap.mapKeys { it.key.path.fileName }

            (sourceByFilename.keys + destByFilename.keys)
                .sorted()
                .forEach { key ->
                    val lastModifiedSource = sourceByFilename.get(key)
                    val lastModifiedDest = destByFilename.get(key)
                    val compResult = compareAsHumanReadableResult(lastModifiedSource, lastModifiedDest)
                    println("$key -> $compResult")
                }

//            println("--------------------")
//            println("source")
//            lastModifiedSourceMap.forEach { tree, lastModified ->
//                println("${tree.path.fileName} -> $lastModified")
//            }
//            println("--------------------")
//            println("dest")
//            lastModifiedDestMap.forEach { tree, lastModified ->
//                println("${tree.path.fileName} -> $lastModified")
//            }


        }

        private fun compareAsHumanReadableResult(lastModifiedSource: LastModified?, lastModifiedDest: LastModified?): String {
            if (lastModifiedSource!= null) {
                if (lastModifiedDest != null) {
                    val comp: Comparision? = lastModifiedSource.compare(lastModifiedDest)
                    return when (comp) {
                        Comparision.Smaller -> "dest is newer"
                        Comparision.Equal -> "same"
                        Comparision.Bigger -> "source is newer"
                        else -> "what?"
                    }

                } else {
                    return "dest missing"
                }
            } else {
                if (lastModifiedDest != null) {
                    return "source missing"
                } else {
                    return "both missing??"
                }
            }
        }
    }

}