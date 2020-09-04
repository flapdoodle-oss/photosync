package de.flapdoodle.fx.layout.weightflex

data class WeightedDimension(
    val weight: Double,
    val min: Double,
    val max: Double,
    val prefered: Double
) {
  init {
    require(weight >= 0.0) { "invalid weight: $weight" }
    require(min >= 0.0) { "invalid min: $min" }
    require(max >= min) { "invalid max: $max (min: $min)" }
    require(max >= prefered && prefered >= min) { "invalid prefered: $prefered (min: $min, max: $max)" }
  }
}