package org.shaper.generators.shared

data class ParamPosition<T>(private val _iter: Iterator<T>, var currentVal: T? = null) : Iterator<T> {
    var wasReset = true
    var index = -1
    private var iter: Iterator<T> = _iter
        set(value) {
            wasReset = true
            index = -1
            field = value
            next()
        }

    val isEmpty = !iter.hasNext()

    override fun hasNext(): Boolean {
        return iter.hasNext()
    }

    override fun next(): T {
        index ++
        wasReset = false
        currentVal = iter.next()
        return currentVal!!
    }
}
