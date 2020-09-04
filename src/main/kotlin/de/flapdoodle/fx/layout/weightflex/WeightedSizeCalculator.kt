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

        val sumOfWeights = items.values.sumByDouble { it.weight }
        val sizedItems = items.mapValues { it -> SizedItems(it.value) }

        distributeX(space, sizedItems)
//        distribute(space, sumOfWeights, sizedItems)

        return sizedItems.mapValues { it.value.size() }
    }

    private fun Map<Int, SizedItems>.sumOfWeights() = values.filter { !it.limitReached() }.sumByDouble { it.src.weight }
    private fun Map<Int, SizedItems>.limitReachedCount() = values.count { it.limitReached() }

    private fun distributeX(space: Double, sizedItems: Map<Int, SizedItems>) {
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

    private fun distribute(space: Double, sizedItems: Map<Int, SizedItems>) {
        var spaceUsedInMax = 0.0

        // calculate space needed and mark only item which exceeded max
        do {
            val sumOfWeights = sizedItems.sumOfWeights()
            var spaceUsed = 0.0
            val spaceLeft = space - spaceUsedInMax - spaceUsed
            val limitReachedCount = sizedItems.values.count { !it.limitReached() }

            // process items which can not grow further
            sizedItems.forEach { entry ->
                val it = entry.value
                if (!it.limitReached()) {
                    val spaceNeeded = spaceLeft * it.src.weight / sumOfWeights
                    if (it.src.max <= spaceNeeded) {
                        it.setSize(it.src.max, true)
                        spaceUsedInMax = spaceUsedInMax + it.src.max
                    } else {
                        it.setSize(spaceNeeded)
                        spaceUsed = spaceUsed + spaceNeeded
                    }
                }
            }
            val limitReachedCountAfter = sizedItems.values.count { !it.limitReached() }
            val spaceLeftAfter = space - spaceUsedInMax - spaceUsed
        } while ((spaceLeftAfter > 0.0) || (limitReachedCount != limitReachedCountAfter))

        val itemsWithMinConstrainViolation = sizedItems.values.filter { it.size() < it.src.min }

        require(itemsWithMinConstrainViolation.isEmpty()) { "min constraint violation: $itemsWithMinConstrainViolation" }
    }

    private fun distribute(space: Double, sumOfWeights: Double, sizedItems: Map<Int, SizedItems>) {
//      println("->>------------------")
//      println("items")
//      sizedItems.forEach { println(it) }

        val itemsWithLimitsReached = sizedItems.values.count { it.limitReached() }
//      println("itemsWithLimitsReached: $itemsWithLimitsReached")

        var spaceUsed = 0.0

        sizedItems.forEach { entry ->
            val it = entry.value
            if (!it.limitReached()) {
                val spaceNeeded = space * it.src.weight / sumOfWeights
                when {
                    spaceNeeded <= it.src.min -> it.setSize(it.src.min, true)
                    spaceNeeded >= it.src.max -> it.setSize(it.src.max, true)
                    else -> it.setSize(spaceNeeded)
                }
                if (it.limitReached()) {
                    spaceUsed = spaceUsed + it.size()
                }
            }
        }

        val newItemsWithLimitsReached = sizedItems.values.count { it.limitReached() }
//      println("newItemsWithLimitsReached: $newItemsWithLimitsReached")

        val anyLimitReached = itemsWithLimitsReached != newItemsWithLimitsReached

        if (anyLimitReached) {
            //val spaceUsed = sizedItems.sumByDouble { if (it.limitReached()) it.size() else 0.0 }
//        println("spaceUsed:  $spaceUsed")
            val spaceLeft = space - spaceUsed
//        println("spaceLeft:  $spaceLeft")
            if (spaceLeft > 0.0 && sizedItems.values.any { !it.limitReached() }) {
//          println("again:  spaceLeft=$spaceLeft")
                val leftSumOfWeights = sizedItems.values.sumByDouble { if (it.limitReached()) 0.0 else it.src.weight }
                distribute(spaceLeft, leftSumOfWeights, sizedItems)
            } else {
//          println("finished:  spaceLeft=$spaceLeft")
//          println("items")
//          sizedItems.forEach { println(it) }
            }
        } else {
//        println("finished:  no new limit reached")
//        println("items")
//        sizedItems.forEach { println(it) }
        }
//      println("-<<------------------")
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