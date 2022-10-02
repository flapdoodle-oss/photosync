package de.flapdoodle.io.filetree.simple

import de.flapdoodle.io.filetree.FileTreeCollector
import de.flapdoodle.photosync.LastModified
import de.flapdoodle.types.Either
import de.flapdoodle.types.Stack
import java.nio.file.Path
import kotlin.io.path.name

class NodeTreeCollector : FileTreeCollector {
    private var stack: Stack<Node.Directory> = Stack()
    private var basePath: Path? = null
    private var root: Node.Directory? = null

    fun root() = root
    fun content() = root!!.children

    override fun down(path: Path, lastModifiedTime: LastModified): Boolean {
        if (stack.isEmpty()) {
            basePath = path
        }
        stack.push(Node.Directory(path.name, lastModifiedTime))

        return true
    }

    override fun up(path: Path) {
        val up = stack.pop()
        requireNotNull(up) { "nothing left" }
        if (stack.isEmpty()) {
            requireNotNull(basePath) { "basePath not set"}
            root = resolveLocalSymlinks(basePath!!, up)
            basePath = null
        } else {
            stack.replace { current -> current.copy(children = current.children + up) }
        }
    }

    override fun add(path: Path, size: Long, lastModifiedTime: LastModified) {
        require(!stack.isEmpty()) { "no parent directory" }
        stack.replace { current -> current.copy(children = current.children + Node.File(path.name, lastModifiedTime, size)) }
    }

    override fun addSymlink(path: Path, destination: Path, lastModifiedTime: LastModified) {
        require(!stack.isEmpty()) { "no parent directory" }
        stack.replace { current -> current.copy(children = current.children + Node.SymLink(path.name, lastModifiedTime, Either.right(destination))) }
    }

    companion object {
        internal fun resolveLocalSymlinks(basePath: Path, src: Node.Directory): Node.Directory {
            return resolveLocalSymlinks(basePath, basePath, src)
        }

        private fun resolveLocalSymlinks(basePath: Path, localPath: Path, src: Node.Directory): Node.Directory {
            val mappedChildren = src.children.map {
                when (it) {
                    is Node.Directory -> resolveLocalSymlinks(basePath, localPath.resolve(it.name), it)
                    is Node.SymLink -> resolveLocalSymlink(it, basePath)
                    else -> it
                }
            }
            return src.copy(children = mappedChildren)
        }

        private fun resolveLocalSymlink(
            symLink: Node.SymLink,
            basePath: Path
        ): Node.SymLink {
            if (symLink.destination is Either.Right) {
                val dest = symLink.destination.value
                if (dest.startsWith(basePath)) {
                    val relativePath = basePath.relativize(symLink.destination.value)
                    return symLink.copy(destination = Either.left(Node.NodeReference.of(relativePath)))
                }
            }
            return symLink
        }
    }
}