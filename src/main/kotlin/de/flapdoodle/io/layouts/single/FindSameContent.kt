package de.flapdoodle.io.layouts.single

import de.flapdoodle.io.tree.Tree
import de.flapdoodle.io.tree.flatMap
import de.flapdoodle.photosync.filehash.HashStrategy
import de.flapdoodle.photosync.filehash.Hasher

object FindSameContent {

    fun find(
        tree: Tree.Directory,
        hashers: List<Hasher<*>>
    ) {
        val files = tree.flatMap {
            when (it) {
                is Tree.File -> listOf(it)
                else -> throw IllegalArgumentException("not supported: $it")
            }
        }

        val groupedByHash = HashStrategy.groupBy(hashers, files)
        val noCollisions = groupedByHash.none { it.value.size>1 }
        if (noCollisions) {
            println("no two same files")
        } else {
            val sorted = groupedByHash.toList()
                .sortedBy { it -> it.second.firstOrNull()?.size ?: 0 }

            sorted.forEach { (hash, list) ->
                if (list.size>1) {
                    println("----------------")
                    println("hash: $hash")
                    println("- - - - - - - - ")
                    list.forEach {
                        println(" ${it.path}")
                    }
                }
            }
        }
    }
}