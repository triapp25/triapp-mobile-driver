package com.triappdriver.utils

import androidx.activity.ComponentActivity
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.InputStream
import java.lang.ref.WeakReference
import kotlin.coroutines.resume


internal var currentActivityRef: WeakReference<ComponentActivity>? = null

fun registerCurrentActivity(activity: ComponentActivity) {
    currentActivityRef = WeakReference(activity)
}

actual suspend fun pickFile(): PickedFile? = suspendCancellableCoroutine { cont ->
    val activity = currentActivityRef?.get()
        ?: run {
            cont.resume(null)
            return@suspendCancellableCoroutine
        }

    val launcher = activity.activityResultRegistry.register(
        "filePicker_${System.currentTimeMillis()}",
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) {
            cont.resume(null)
        } else {
            val name = uri.lastPathSegment ?: "arquivo"
            val input: InputStream? = activity.contentResolver.openInputStream(uri)
            val bytes = input?.readBytes()
            cont.resume(PickedFile(name, bytes))
        }
    }

    launcher.launch(arrayOf("*/*"))

    cont.invokeOnCancellation {
        launcher.unregister()
    }
}
