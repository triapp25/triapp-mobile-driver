package com.triappdriver.utils

import platform.Foundation.NSString
import platform.Foundation.stringByAddingPercentEncodingWithAllowedCharacters
import platform.Foundation.stringByRemovingPercentEncoding
import platform.Foundation.NSCharacterSet
import platform.Foundation.URLQueryAllowedCharacterSet
import platform.Foundation.create

actual object UrlEncoder {
    actual fun encode(value: String): String {
        // Converte para NSString para ter acesso aos métodos do iOS
        val nsString = NSString.create(string = value)
        // Faz o encode
        return nsString.stringByAddingPercentEncodingWithAllowedCharacters(
            NSCharacterSet.URLQueryAllowedCharacterSet
        ) ?: value
    }

    actual fun decode(value: String): String {
        val nsString = NSString.create(string = value)
        return nsString.stringByRemovingPercentEncoding() ?: value
    }
}