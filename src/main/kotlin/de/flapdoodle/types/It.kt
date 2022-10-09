package de.flapdoodle.types

fun <T,R> T.letThis(mapping: T.() -> R): R {
  return mapping(this)
}