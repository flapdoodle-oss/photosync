package de.flapdoodle.fx.layout.weightflex

object WeightedSizeCalculator {
    fun distribute(space: Double, items: Map<Int, WeightedDimension>): Map<Int, Double> {
        return if (items.isNotEmpty())
            distributeNonEmpty(space, items)
        else
            emptyMap()
    }

    private fun distributeNonEmpty(space: Double, items: Map<Int, WeightedDimension>): Map<Int, Double> {
        val minWidth = items.values.sumByDouble { it.min }
        val maxWidth = doubleMaxIfInfinite(items.values.sumByDouble { it.max })

        if (minWidth >= space) {
            return items.mapValues { it.value.min }
        }
        if (maxWidth <= space) {
            return items.mapValues { it.value.max }
        }

        val sizedItems = items.mapValues { it -> SizedItems(it.value) }

        distribute(space, sizedItems)

        return sizedItems.mapValues { it.value.size() }
    }

    private fun Map<Int, SizedItems>.sumOfWeights() = values.filter { !it.limitReached() }.sumByDouble { it.src.weight }
    private fun Map<Int, SizedItems>.limitReachedCount() = values.count { it.limitReached() }

    private fun distribute(space: Double, sizedItems: Map<Int, SizedItems>) {
        var spaceUsedInMax = 0.0
        var spaceUsedInMin = 0.0

        do {
            val limitReachedCount = sizedItems.limitReachedCount()

            val spaceToGrow = space - spaceUsedInMax - spaceUsedInMin

            var sumOfWeights = sizedItems.sumOfWeights()
            sizedItems.values
                    .filter { !it.limitReached() }
                    .forEach {
                        val spaceNeeded = spaceToGrow * it.src.weight / sumOfWeights
                        if (it.src.max <= spaceNeeded) {
                            it.setSize(it.src.max, true)
                            spaceUsedInMax = spaceUsedInMax + it.src.max
                        }
                    }

            val spaceToShrink = space - spaceUsedInMax - spaceUsedInMin
            sumOfWeights = sizedItems.sumOfWeights()

            sizedItems.values
                    .filter { !it.limitReached() }
                    .forEach {
                        val spaceNeeded = spaceToShrink * it.src.weight / sumOfWeights
                        if (it.src.min >= spaceNeeded) {
                            it.setSize(it.src.min, true)
                            spaceUsedInMin = spaceUsedInMin + it.src.min
                        }
                    }

            var spaceUsed = 0.0

            sumOfWeights = sizedItems.sumOfWeights()
            sizedItems.values
                    .filter { !it.limitReached() }
                    .forEach {
                        val spaceNeeded = spaceToShrink * it.src.weight / sumOfWeights
                        if (it.src.min <= spaceNeeded && it.src.max >= spaceNeeded) {
                            it.setSize(spaceNeeded)
                            spaceUsed = spaceUsed + spaceNeeded
                        }
                    }


            val spaceLeft = space - spaceUsedInMax - spaceUsedInMin - spaceUsed
            val limitReachedCountAfter = sizedItems.limitReachedCount()

        } while ((spaceLeft > 0.0) && (limitReachedCount != limitReachedCountAfter))

        val itemsWithMinConstrainViolation = sizedItems.values.filter { it.size() < it.src.min }

        require(itemsWithMinConstrainViolation.isEmpty()) { "min constraint violation: $itemsWithMinConstrainViolation" }
    }

    private fun doubleMaxIfInfinite(value: Double): Double {
        return if (value.isInfinite()) Double.MAX_VALUE else value
    }

    private class SizedItems(
            val src: WeightedDimension
    ) {
        private var size: Double = 0.0
        private var limitReached: Boolean = false

        fun limitReached() = limitReached

        fun size() = size
        fun setSize(size: Double, limitReached: Boolean = false) {
            this.size = size
            this.limitReached = limitReached
        }

        override fun toString(): String {
            return "SizedItem: $src -> limitReached: $limitReached, size=$size"
        }
    }

}