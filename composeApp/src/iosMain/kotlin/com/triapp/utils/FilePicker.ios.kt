package com.triapp.utils

import platform.UIKit.*
import platform.Foundation.*
import kotlinx.cinterop.*
import kotlinx.coroutines.suspendCancellableCoroutine

actual suspend fun pickFile(): PickedFile? = suspendCancellableCoroutine { cont ->
    val picker = UIDocumentPickerViewController(forOpeningContentTypes = listOf(UTTypeItem))
    picker.setAllowsMultipleSelection(false)
    picker.delegate = object : NSObject(), UIDocumentPickerDelegateProtocol {
        override fun documentPicker(controller: UIDocumentPickerViewController, didPickDocumentsAtURLs: List<*>?) {
            val url = didPickDocumentsAtURLs?.firstOrNull() as? NSURL
            val data = NSData.dataWithContentsOfURL(url!!)
            val bytes = data?.toByteArray()
            cont.resume(PickedFile(url.lastPathComponent ?: "file", bytes ?: byteArrayOf()))
        }
    }
    val vc = UIApplication.sharedApplication.keyWindow?.rootViewController
    vc?.presentViewController(picker, animated = true, completion = null)
}
