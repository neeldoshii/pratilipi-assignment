package com.example.rich_text_editor.editor

import com.example.rich_text_editor.editor.model.BlockDto
import com.example.rich_text_editor.editor.model.DocumentContentDto
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object DocumentJsonCodec {
    const val CURRENT_SCHEMA_VERSION: Int = DocumentContentDto.CURRENT_SCHEMA_VERSION

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = false
    }

    fun emptyDocumentJson(): String = encode(
        DocumentContentDto(
            schemaVersion = CURRENT_SCHEMA_VERSION,
            blocks = listOf(BlockDto.Text(heading = null, text = "", spans = emptyList())),
        ),
    )

    fun encode(content: DocumentContentDto): String = json.encodeToString(content)

    fun decode(raw: String): DocumentContentDto =
        try {
            json.decodeFromString<DocumentContentDto>(raw)
        } catch (_: Exception) {
            DocumentContentDto(
                schemaVersion = CURRENT_SCHEMA_VERSION,
                blocks = listOf(BlockDto.Text(heading = null, text = "", spans = emptyList())),
            )
        }
}
