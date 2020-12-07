package de.flapdoodle.io.diff

import de.flapdoodle.io.tree.Tree
import de.flapdoodle.io.tree.mapFiles
import de.flapdoodle.photosync.filehash.HashStrategy
import de.flapdoodle.photosync.filehash.Hasher

class TreeDiff {

    companion object {
        fun diff(
                src: Tree.Directory,
                dst: Tree.Directory,
                hashers: List<Hasher<*>>
        ) {
            val srcFiles = src.mapFiles { it }
            val dstFiles = dst.mapFiles { it }
            val files = srcFiles + dstFiles

            val hashGroupedFiles = HashStrategy.groupBy(hashers, files)

            hashGroupedFiles.forEach { hash, list ->
                println("hash -> $hash")
                list.forEach {
                    println(" -> ${it.path}")
                }
            }
        }
    }
}