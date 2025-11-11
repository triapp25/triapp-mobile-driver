package com.triapp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect class PlatformContext
