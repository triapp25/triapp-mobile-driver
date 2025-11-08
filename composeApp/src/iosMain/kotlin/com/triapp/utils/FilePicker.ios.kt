package com.triapp.utils

import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.*
import platform.UIKit.*
import platform.UniformTypeIdentifiers.*
import kotlinx.cinterop.*
import kotlin.coroutines.resume
import platform.darwin.NSObject

actual suspend fun pickFile(): PickedFile? = suspendCancellableCoroutine { cont ->
    val picker = UIDocumentPickerViewController(forOpeningContentTypes = listOf(UTTypeData))

    picker.setAllowsMultipleSelection(false)
    picker.delegate = object : NSObject(), UIDocumentPickerDelegateProtocol {

        override fun documentPicker(controller: UIDocumentPickerViewController, didPickDocumentsAtURLs: List<*>) {
            val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL ?: return
            val data = NSData.dataWithContentsOfURL(url)
            val bytes = data?.toByteArray()
            cont.resume(PickedFile(url.lastPathComponent ?: "file", bytes ?: byteArrayOf()))
        }
    }

    val vc = UIApplication.sharedApplication.keyWindow?.rootViewController
    vc?.presentViewController(picker, animated = true, completion = null)
}

@OptIn(ExperimentalForeignApi::class)
fun NSData.toByteArray(): ByteArray {
    val length = this.length.toInt()
    val bytes = ByteArray(length)
    memScoped {
        val buffer = allocArray<ByteVar>(length)
        this@toByteArray.getBytes(buffer, length.toULong())
        for (i in 0 until length) {
            bytes[i] = buffer[i]
        }
    }
    return bytes
}
