package com.triapp.utils

import java.net.URLEncoder
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

actual object UrlEncoder {
    actual fun encode(value: String): String {
        return URLEncoder.encode(value, StandardCharsets.UTF_8.toString())
    }

    actual fun decode(value: String): String {
        return URLDecoder.decode(value, StandardCharsets.UTF_8.toString())
    }
}