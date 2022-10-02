package de.flapdoodle.types

class Stack<T> {
    private val list = mutableListOf<T>()
    private val lastIndex
        get() = list.size - 1

    fun isEmpty() = list.isEmpty()
    
    fun push(item: T) {
        list.add(item)
    }

    fun pop(): T? {
        return if (list.isNotEmpty()) list.removeAt(lastIndex) else null
    }

    fun peek(): T? {
        return if (list.isNotEmpty()) list[lastIndex] else null
    }

    fun replace(map: (T) -> T) {
        if (list.isNotEmpty()) {
            list[lastIndex] = map(list[lastIndex])
        }
    }


    override fun toString(): String {
        return "Stack(${list.joinToString(",") { it.toString() }})"
    }
}