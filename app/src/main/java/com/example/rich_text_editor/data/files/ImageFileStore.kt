package com.example.rich_text_editor.data.files

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class ImageFileStore(context: Context) {
    private val appContext = context.applicationContext
    private val imagesDir: File =
        File(appContext.filesDir, "images").also { it.mkdirs() }

    fun resolveFile(fileName: String): File = File(imagesDir, fileName)

    suspend fun importFromUri(uri: Uri): String = withContext(Dispatchers.IO) {
        val ext = resolveExtension(uri)
        val name = "${UUID.randomUUID()}.$ext"
        val outFile = File(imagesDir, name)
        appContext.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(outFile).use { output -> input.copyTo(output) }
        } ?: error("Unable to open $uri")
        name
    }

    private fun resolveExtension(uri: Uri): String {
        val type = appContext.contentResolver.getType(uri)
        return when (type) {
            "image/png" -> "png"
            "image/webp" -> "webp"
            "image/gif" -> "gif"
            else -> "jpg"
        }
    }
}
