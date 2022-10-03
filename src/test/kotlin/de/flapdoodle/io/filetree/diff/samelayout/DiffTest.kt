package de.flapdoodle.io.filetree.diff.samelayout

import de.flapdoodle.io.filetree.Node
import de.flapdoodle.photosync.LastModified
import de.flapdoodle.types.Either
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class DiffTest {

    @Test
    fun sample() {
        val now = LastModified.now()

        val src = Node.Directory("src", now, children = listOf(
            Node.File("same-file", now, 123L),
            Node.File("changed-size", now, 100L),
            Node.File("changed-time", now + 1, 100L),
            Node.File("changed-hash", now + 1, 200L),

            Node.File("new-file", now + 1, 10L),

            Node.SymLink("changed-sym-link", now, Either.left(Node.NodeReference(listOf("same-file")))),

            Node.Directory("sub", now, children = listOf(
                Node.File("file", now, 123L),
            ))
        ))

        val dest = Node.Directory("src", now, children = listOf(
            Node.File("same-file", now, 123L),
            Node.File("changed-size", now, 200L),
            Node.File("changed-time", now, 200L),
            Node.File("changed-hash", now + 1, 200L),

            Node.File("removed-file", now -10, 10L),

            Node.SymLink("changed-sym-link", now + 1, Either.left(Node.NodeReference(listOf("same-file")))),

            Node.Directory("sub", now + 1, children = listOf(
                Node.File("file", now, 123L),
            ))
        ))

        val diff = Diff.diff(src.children, dest.children)
        print(diff)
    }

    private fun print(diff: Diff) {
        diff.entries.forEach {
            when (it) {
                is Diff.Entry.DirectoryChanged -> {
                    println("--> ${it.src} ? ${it.dest}")
                    print(it.diff)
                }
                else -> println(" -> $it")
            }
        }
    }
}