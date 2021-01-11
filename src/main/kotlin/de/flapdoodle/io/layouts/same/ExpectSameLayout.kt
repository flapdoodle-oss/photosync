package de.flapdoodle.io.layouts.same

import de.flapdoodle.io.layouts.common.Diff
import de.flapdoodle.io.tree.Tree
import de.flapdoodle.io.tree.childWithPath
import de.flapdoodle.photosync.filehash.HashStrategy
import de.flapdoodle.photosync.filehash.Hasher
import java.nio.file.Path

object ExpectSameLayout {

    fun diff(
            src: Tree.Directory,
            dst: Tree.Directory,
            hashers: List<Hasher<*>>
    ): List<Diff> {
        val srcBase = src.path
        val dstBase = dst.path

        return dirDiff(srcBase, dstBase, src, dst, hashers)
    }

    private fun dirDiff(srcBase: Path, dstBase: Path, src: Tree.Directory, dst: Tree.Directory, hashers: List<Hasher<*>>): List<Diff> {
        val relSrc = srcBase.relativize(src.path)
        val relDst = dstBase.relativize(dst.path)

        require(relSrc == relDst) { "path mismatch: $relSrc != $relDst" }

        return Diff.diff(
            src,
            dst,
            hashers
        ) { s, d -> dirDiff(srcBase, dstBase, s, d, hashers) }
    }
}