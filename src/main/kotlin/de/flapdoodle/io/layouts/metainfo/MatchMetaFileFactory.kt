package de.flapdoodle.io.layouts.metainfo

import de.flapdoodle.photosync.paths.Meta
import java.nio.file.Path

fun interface MatchMetaFileFactory {
    fun create(paths: List<Path>): MatchMetaFile

    companion object {
        fun default(): MatchMetaFileFactory = Default()
        private class Default : MatchMetaFileFactory {
            override fun create(paths: List<Path>): MatchMetaFile {
                val pathsOfBasePath = Meta.groupByBasePath(paths)
                val basePathOfPath = pathsOfBasePath
                        .flatMap { it.value.map { path -> path to it.key } }.toMap()

                return MatchMetaFile {
                    basePathOfPath[it]
                }
            }
        }
    }
}