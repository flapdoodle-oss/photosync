package de.flapdoodle.fx.extensions

import kotlin.reflect.KClass

abstract class Key<T: Any> {
  companion object {
    fun <T: Any> ofType(type: KClass<T>): Key<T> {
      return TypeKey(type)
    }

    fun <T: Any> ofType(scope: KClass<out Any>, type: KClass<T>): Key<T> {
      return Scoped(scope, type)
    }
  }

  private data class TypeKey<T: Any>(
      private val type: KClass<T>
  ) : Key<T>()

  private data class Scoped<T: Any>(
      private val scope: KClass<out Any>,
      private val type: KClass<T>
  ) : Key<T>()
}