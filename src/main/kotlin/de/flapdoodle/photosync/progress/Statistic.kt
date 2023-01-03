package de.flapdoodle.photosync.progress

import java.util.concurrent.ConcurrentHashMap

object Statistic {

  private val threadCollector = ThreadLocal<Collector>()

  fun collect(collector: Collector = GroupByNameCollector(), action: () -> Unit): List<Entry<out Any>> {
    try {
      threadCollector.set(collector)
      action()
    } finally {
      threadCollector.remove()
    }
    return collector.entries()
  }

  fun <T: Any> set(key: Property<T>, value: T) {
    threadCollector.get()?.set(key, value)
  }

  interface Property<T> {
    val name: String
    val type: Class<T>
    val reduce: (T, T) -> T
    val formatter: (T) -> String
  }

  fun <T> property(
    name: String,
    type: Class<T>,
    reduce: (T, T) -> T,
    formatter: (T) -> String
  ): Property<T> {
    return object : Property<T> {
      override val name: String = name
      override val type: Class<T> = type
      override val reduce: (T, T) -> T = reduce
      override val formatter: (T) -> String = formatter
    }
  }

  interface Collector {
    fun <T: Any> set(key: Property<T>, value: T)
    fun entries(): List<Entry<Any>>
  }

  class GroupByNameCollector : Collector {
    val values = ConcurrentHashMap<Property<Any>, Any>()

    override fun <T: Any> set(key: Property<T>, value: T) {
      values.compute(key as Property<Any>) { k: Property<Any>, old: Any? ->
        if (old!=null) key.reduce(old as T, value) else value
      }
    }

    override fun entries(): List<Entry<Any>> {
      return values.map { entry ->
        Entry(entry.key, entry.value)
      }
    }
  }

  data class Entry<T>(val key: Property<T>, val value: T) {
    fun asHumanReadable() = "${key.name}: ${key.formatter(value)}"
  }
}