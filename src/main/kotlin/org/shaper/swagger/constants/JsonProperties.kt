package org.shaper.swagger.constants

object JsonProperties {
    const val keyDelim = "|~>"

    fun compressDelimitedKey(key: String): String{
        return key.replace(keyDelim,"")
    }
}