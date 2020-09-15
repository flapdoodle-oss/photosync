package de.flapdoodle.fx.layout.weightflex

import de.flapdoodle.photosync.associateByNotNull

data class FlexMap<T : Any>(
        private val map: Map<T, Area> = emptyMap()
) {
    private val columnSet = map.values.flatMap { it.columns() }.toSet().sorted()
    private val rowSet = map.values.flatMap { it.rows() }.toSet().sorted()

    companion object {
        private fun <T : Any> sizes(
                map: Map<T, List<Int>>,
                positionSet: List<Int>,
                weightOf: (Int) -> Weight,
                limitsOf: (T) -> Triple<Double, Double, Double>
        ): Map<Int, WeightedDimension> {
            val weights = positionSet.associateByNotNull(weightOf)

            val positionLimits = map.entries.flatMap { entry ->
                val limits = limitsOf(entry.key)
//                println("-limits---")
//                println(""+entry.key+" -> "+limits)
//                println("----------")

                val weightMap = entry.value
                        .map { it -> it to (weights[it] ?: Weight(0.0)) }

                val weightSum = weightMap.map { it.second }
                        .foldRight(Weight(0.0), { a, b -> a + b })

                weightMap.map { (column, weight) ->
                    val factor = if (weightSum.value>0.0) weight.partOf(weightSum) else (1.0/weightMap.size)
                    column to Dimension(limits.first * factor, limits.second * factor, limits.third * factor)
                }
            }
                    .groupBy({ it.first }) { it.second }
                    .mapValues { entry ->
                        entry.value
                                .foldRight(Dimension(0.0, 0.0, 0.0), { a, b ->
                                    mergeLimits(a, b)
                                })
                    }

            return positionSet.associateByNotNull {
                val weigth = weights[it]!!.value
                val limits = positionLimits[it]!!
                WeightedDimension(weigth, limits.min, limits.max, limits.prefered)
            }
        }

        private fun mergeLimits(a: Dimension, b: Dimension): Dimension {
            val pair = Dimension(Math.max(a.min, b.min),Math.max(a.max, b.max), Math.max(a.prefered,b.prefered))
            return if (pair.min <= pair.max && pair.prefered <= pair.max)
                pair
            else
                Dimension(pair.min, pair.min, pair.min)
        }

    }

    fun columns() = columnSet
    fun rows() = rowSet

    fun add(value: T, area: Area): FlexMap<T> {
        return copy(map = map + (value to area))
    }

    fun remove(value: T): FlexMap<T> {
        return copy(map = map - value)
    }

    fun columnSizes(columnWeight: (Int) -> Weight, limitsOf: (T) -> Triple<Double, Double, Double>): Map<Int, WeightedDimension> {
        return sizes(map.mapValues { it.value.columns() }, this.columnSet, columnWeight, limitsOf)
    }

    fun rowSizes(rowWeight: (Int) -> Weight, limitsOf: (T) -> Triple<Double, Double, Double>): Map<Int, WeightedDimension> {
        return sizes(map.mapValues { it.value.rows() }, this.rowSet, rowWeight, limitsOf)
    }

    fun forEach(action: (T, Area) -> Unit) {
        map.forEach(action)
    }

    fun <D> map(mapper: (T, Area) -> D): List<D> {
        return map.map { mapper(it.key, it.value) }
    }
}