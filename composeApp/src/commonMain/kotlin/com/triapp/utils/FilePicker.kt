package com.triapp.utils

data class PickedFile(val name: String, val bytes: ByteArray?)

expect suspend fun pickFile(): PickedFile?