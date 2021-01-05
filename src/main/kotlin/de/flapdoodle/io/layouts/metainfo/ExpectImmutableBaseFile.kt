package de.flapdoodle.io.layouts.metainfo

import de.flapdoodle.io.layouts.common.Diff
import de.flapdoodle.io.tree.childWithPath
import de.flapdoodle.photosync.filehash.Hasher
import java.lang.IllegalArgumentException
import java.nio.file.Path

object ExpectImmutableBaseFile {

    sealed class MetaDiff {
        data class Missmatch(
            val src: MetaView.Node,
            val dst: MetaView.Node,
            val baseDiff: List<Diff>,
            val metaDiff: List<Diff>
        ) : MetaDiff()

        data class SourceIsMissing(val expectedPath: Path, val dst: MetaView) : MetaDiff()
        data class DestinationIsMissing(val src: MetaView, val expectedPath: Path) : MetaDiff()
        data class TypeMissmatch(val src: MetaView, val dst: MetaView) : MetaDiff()
    }

    fun diff(
        src: MetaView.Directory,
        dst: MetaView.Directory,
        hashers: List<Hasher<*>>
    ): List<MetaDiff> {
        return dirDiff(src.path, dst.path, src, dst, hashers)
    }

    private fun dirDiff(
        srcBase: Path,
        dstBase: Path,
        src: MetaView.Directory,
        dst: MetaView.Directory,
        hashers: List<Hasher<*>>
    ): List<MetaDiff> {
        val relSrc = srcBase.relativize(src.path)
        val relDst = dstBase.relativize(dst.path)

        require(relSrc == relDst) { "path mismatch: $relSrc != $relDst" }

        var diffs = emptyList<MetaDiff>()
        src.children.forEach { srcChild ->
            val childPath = srcBase.relativize(srcChild.path)
            val expectedDestination = dstBase.resolve(childPath)
            val dstChild = dst.children.childWithPath(expectedDestination)

            if (dstChild != null) {
                when (srcChild) {
                    is MetaView.Directory -> {
                        when (dstChild) {
                            is MetaView.Directory -> diffs =
                                diffs + dirDiff(srcBase, dstBase, srcChild, dstChild, hashers)
                            else -> diffs = diffs + MetaDiff.TypeMissmatch(srcChild, dstChild)
                        }
                    }
                    is MetaView.Node -> {
                        when (dstChild) {
                            is MetaView.Node -> diffs = diffs + fileDiff(srcBase, dstBase, srcChild, dstChild, hashers)
                            else -> diffs = diffs + MetaDiff.TypeMissmatch(srcChild, dstChild)
                        }
                    }
                }
            } else {
                diffs = diffs + MetaDiff.DestinationIsMissing(srcChild, expectedDestination)
            }
        }

        dst.children.forEach { dstChild ->
            val childPath = dstBase.relativize(dstChild.path)
            val expectedSource = srcBase.resolve(childPath)
            val srcChild = src.children.childWithPath(expectedSource)
            if (srcChild == null) {
                diffs = diffs + MetaDiff.SourceIsMissing(expectedSource, dstChild)
            }
        }
        return diffs
    }

    private fun fileDiff(
        srcBase: Path,
        dstBase: Path,
        src: MetaView.Node,
        dst: MetaView.Node,
        hashers: List<Hasher<*>>
    ): List<MetaDiff> {
        val baseDiff = Diff.diff(srcBase, dstBase, listOf(src.base), listOf(dst.base), hashers) {
                _,_ -> throw IllegalArgumentException("call not expected")
        }

        val metaDiff = Diff.diff(srcBase, dstBase, src.metaFiles, dst.metaFiles, hashers) {
                _,_ -> throw IllegalArgumentException("call not expected")
        }

        return if (baseDiff.isNotEmpty() || metaDiff.isNotEmpty()) {
            listOf(MetaDiff.Missmatch(src,dst, baseDiff, metaDiff))
        } else
            emptyList()
    }
}