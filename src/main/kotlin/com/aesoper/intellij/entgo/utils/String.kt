package com.aesoper.intellij.entgo.utils

import java.util.*

fun String.beginWithLowerCase(): String {
    return when (this.length) {
        0 -> ""
        1 -> this.lowercase(Locale.getDefault())
        else -> this[0].lowercaseChar() + this.substring(1)
    }
}

/**
 * Make first letter upper case
 *
 * ```
 * "abc".beginWithUpperCase() // => "Abc"
 * ```
 */
fun String.beginWithUpperCase(): String {
    return when (this.length) {
        0 -> ""
        1 -> this.uppercase(Locale.getDefault())
        else -> this[0].uppercaseChar() + this.substring(1)
    }
}

/**
 * Make snake case string to camel case
 *
 * ```
 * "a_b_c".toCamelCase() // => "ABC"
 * ```
 */
fun String.toCamelCase(): String {
    return this.split('_').map {
        it.beginWithUpperCase()
    }.joinToString("")
}

/**
 * Make camel case string to snake case
 *
 * ```
 * "ABC".toSnakeCase() // => "a_b_c"
 * ```
 */
fun String.toSnakeCase(): String {
    var text: String = ""
    var isFirst = true
    this.forEach {
        if (it.isUpperCase()) {
            if (isFirst) isFirst = false
            else text += "_"
            text += it.lowercaseChar()
        } else {
            text += it
        }
    }
    return text
}