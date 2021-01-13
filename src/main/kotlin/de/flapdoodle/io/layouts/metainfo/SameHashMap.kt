package de.flapdoodle.io.layouts.metainfo

import de.flapdoodle.io.tree.Tree
import de.flapdoodle.photosync.filehash.Hash

class SameHashMap<T>(list: List<SameHash<T>>) {
    private val map = list.flatMap { sameHash ->
            when(sameHash) {
                is SameHash.OnlySource -> listOf(sameHash.src)
                is SameHash.OnlyDestination -> listOf(sameHash.dst)
                is SameHash.Direct -> listOf(sameHash.src, sameHash.dst)
                is SameHash.Multi -> sameHash.src + sameHash.dst
            }.map { it to sameHash }
        }.toMap()

    fun get(key: T): SameHash<T> {
        return requireNotNull(map[key]) { "no entry found for $key" }
    }

    sealed class SameHash<T> {
        class OnlySource<T>(val src: T, val hash: Hash<*>) : SameHash<T>()
        class OnlyDestination<T>(val dst: T, val hash: Hash<*>) : SameHash<T>()
        class Direct<T>(val src: T, val dst: T, val hash: Hash<*>) : SameHash<T>()
        class Multi<T>(val src: List<T>, val dst: List<T>, val hash: Hash<*>): SameHash<T>()
    }

    companion object {

        fun <K, T> from(
            srcMap: Map<K, T>,
            dstMap: Map<K, T>,
            groupedByHash: Map<Hash<*>, List<K>>
        ): SameHashMap<T> {
            val sameHash: List<SameHash<T>> = groupedByHash.map { (hash, files) ->
                val srcNodes = files.mapNotNull(srcMap::get)
                val dstNodes = files.mapNotNull(dstMap::get)
                sameHash(srcNodes, dstNodes, hash)
            }
            return SameHashMap(sameHash)
        }

        private fun <T> sameHash(src: List<T>, dst: List<T>, hash: Hash<*>): SameHash<T> {
            require(src.isNotEmpty() || dst.isNotEmpty()) { "src and dst is empty"}
            return if (src.size==1 && dst.size==1) {
                SameHash.Direct(src[0], dst[0], hash)
            } else {
                if (src.size>1 || dst.size>1) {
                    SameHash.Multi(src,dst,hash)
                } else {
                    if (src.isNotEmpty()) {
                        SameHash.OnlySource(src[0],hash)
                    } else {
                        SameHash.OnlyDestination(dst[0],hash)
                    }
                }
            }
        }

    }
}