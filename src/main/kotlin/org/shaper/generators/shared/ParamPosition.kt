package org.shaper.generators.shared

data class ParamPosition<T>(private val _iter: Iterator<T>, var currentVal: T? = null) : Iterator<T> {
    var wasReset = true
    private var iter: Iterator<T> = _iter
        set(value) {
            wasReset = true
            field = value
            next()
        }

    val isEmpty = !iter.hasNext()

    override fun hasNext(): Boolean {
        return iter.hasNext()
    }

    override fun next(): T {
            wasReset = false
            currentVal = iter.next()
            return currentVal!!
    }
}
