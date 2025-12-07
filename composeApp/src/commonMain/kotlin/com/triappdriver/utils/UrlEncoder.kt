package com.triappdriver.utils

expect object UrlEncoder {
    fun encode(value: String): String
    fun decode(value: String): String
}