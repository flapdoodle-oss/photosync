package de.flapdoodle.io.layouts.metainfo

import de.flapdoodle.io.layouts.common.Diff
import de.flapdoodle.io.tree.childWithPath
import de.flapdoodle.photosync.filehash.Hasher
import de.flapdoodle.photosync.paths.expectParent
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
            val expectedDestination = dstBase.resolve(srcBase.relativize(srcChild.path))
            val dstChild = dst.children.childWithPath(expectedDestination)

            if (dstChild != null) {
                diffs = when (srcChild) {
                    is MetaView.Directory -> {
                        when (dstChild) {
                            is MetaView.Directory -> diffs + dirDiff(srcBase, dstBase, srcChild, dstChild, hashers)
                            else -> diffs + MetaDiff.TypeMissmatch(srcChild, dstChild)
                        }
                    }
                    is MetaView.Node -> {
                        when (dstChild) {
                            is MetaView.Node -> diffs + fileDiff(srcChild, dstChild, hashers)
                            else -> diffs + MetaDiff.TypeMissmatch(srcChild, dstChild)
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
        src: MetaView.Node,
        dst: MetaView.Node,
        hashers: List<Hasher<*>>
    ): List<MetaDiff> {
        val baseDiff = Diff.diff(src.path.expectParent(), dst.path.expectParent(), listOf(src.base), listOf(dst.base), hashers) {
                _,_ -> throw IllegalArgumentException("call not expected")
        }

        val metaDiff = Diff.diff(src.path.expectParent(), dst.path.expectParent(), src.metaFiles, dst.metaFiles, hashers) {
                _,_ -> throw IllegalArgumentException("call not expected")
        }

        return if (baseDiff.isNotEmpty() || metaDiff.isNotEmpty()) {
            listOf(MetaDiff.Missmatch(src,dst, baseDiff, metaDiff))
        } else
            emptyList()
    }
}