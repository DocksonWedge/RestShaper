package org.shaper.swagger.constants

object JsonProperties {
    const val keyDelim = "|~>"

    fun compressPropertyKey(key: String): String {
        return key.toLowerCase().replace(keyDelim, "")
    }

    fun splitPropertyKey(property: String): List<String> {
        return property.split(keyDelim)
    }
}