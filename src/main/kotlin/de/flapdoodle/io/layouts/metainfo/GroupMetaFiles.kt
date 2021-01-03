package de.flapdoodle.io.layouts.metainfo

import de.flapdoodle.photosync.paths.Meta
import java.nio.file.Path

interface GroupMetaFiles {
    fun <S, D> groupMetaFiles(
        elements: List<S>,
        pathOfElement: (S) -> Path,
        map: (S, List<S>) -> D
    ): List<D>

    companion object {
        fun default(): GroupMetaFiles = Default

        object Default : GroupMetaFiles {
            override fun <S, D> groupMetaFiles(
                elements: List<S>,
                pathOfElement: (S) -> Path,
                map: (S, List<S>) -> D
            ): List<D> {
                val pathsMap = elements.associateBy(pathOfElement)
                val basePathsMap = Meta.groupByBasePath(pathsMap.keys)
                return pathsMap.flatMap { (path, element) ->
                    val metaPaths = basePathsMap[path]
                    if (metaPaths!=null)
                        listOf(map(element, metaPaths.map { pathsMap[it]!! }))
                    else
                        emptyList()
                }
            }
        }
    }
}