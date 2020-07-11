package de.flapdoodle.fx.layout

data class WeightedSize(
    val weight: Double,
    val min: Double,
    val max: Double
) {
  init {
    require(weight >= 0.0) { "invalid weight: $weight" }
    require(min >= 0.0) { "invalid min: $min" }
    require(max >= 0.0 && max >= min) { "invalid max: $max (min: $min)" }
  }

  companion object {
    fun distribute(space: Double, items: List<WeightedSize>): List<Double> {
      return if (items.isNotEmpty())
        distributeNonEmpty(space, items)
      else
        emptyList()
    }

    private fun distributeNonEmpty(space: Double, items: List<WeightedSize>): List<Double> {
      val minWidth = items.sumByDouble { it.min }
      val maxWidth = doubleMaxIfInfinite(items.sumByDouble { it.max })

      if (minWidth >= space) {
        return items.map { it.min }
      }
      if (maxWidth <= space) {
        return items.map { it.max }
      }

      val sumOfWeights = items.sumByDouble { it.weight }
      val sizedItems = items.map(Companion::SizedItems)

      distribute(space, sumOfWeights, sizedItems)

      return sizedItems.map { it.size() }
    }

    private fun distribute(space: Double, sumOfWeights: Double, sizedItems: List<SizedItems>) {
//      println("->>------------------")
//      println("items")
//      sizedItems.forEach { println(it) }

      val itemsWithLimitsReached = sizedItems.count { it.limitReached() }
//      println("itemsWithLimitsReached: $itemsWithLimitsReached")

      var spaceUsed = 0.0

      sizedItems.forEach {
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

      val newItemsWithLimitsReached = sizedItems.count { it.limitReached() }
//      println("newItemsWithLimitsReached: $newItemsWithLimitsReached")

      val anyLimitReached = itemsWithLimitsReached != newItemsWithLimitsReached

      if (anyLimitReached) {
        //val spaceUsed = sizedItems.sumByDouble { if (it.limitReached()) it.size() else 0.0 }
//        println("spaceUsed:  $spaceUsed")
        val spaceLeft = space - spaceUsed
//        println("spaceLeft:  $spaceLeft")
        if (spaceLeft > 0.0 && sizedItems.any { !it.limitReached() }) {
//          println("again:  spaceLeft=$spaceLeft")
          val leftSumOfWeights = sizedItems.sumByDouble { if (it.limitReached()) 0.0 else it.src.weight }
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
        val src: WeightedSize
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
}