package de.flapdoodle.io.filetree.simple

import de.flapdoodle.io.filetree.FileTreeCollector
import de.flapdoodle.photosync.LastModified
import de.flapdoodle.types.Either
import de.flapdoodle.types.Stack
import java.nio.file.Path
import kotlin.io.path.name

class NodeTreeCollector : FileTreeCollector {
    private var stack: Stack<Node.Directory> = Stack()
    private var root: Node.Directory? = null;

    fun root() = root

    override fun down(path: Path, lastModifiedTime: LastModified): Boolean {
        println("down -> $path")
        if (stack.isEmpty()) {
            stack.push(Node.Directory("", lastModifiedTime))
        } else {
            val sub = Node.Directory(path.name, lastModifiedTime)
            stack.replace { current -> current.copy(children = current.children + sub) }
            stack.push(sub)
        }

        return true
    }

    override fun up(path: Path) {
        println("up -> $path")
        val up = stack.pop()
        requireNotNull(up) { "nothing left" }
        if (stack.isEmpty()) {
            root = up
        }
    }

    override fun add(path: Path, size: Long, lastModifiedTime: LastModified) {
        println("add -> $path")
        stack.replace { current -> current.copy(children = current.children + Node.File(path.name, lastModifiedTime, size)) }
    }

    override fun addSymlink(path: Path, destination: Path, lastModifiedTime: LastModified) {
        println("symlink -> $path --> $destination")
        stack.replace { current -> current.copy(children = current.children + Node.SymLink(path.name, lastModifiedTime, Either.right(destination))) }
    }
}