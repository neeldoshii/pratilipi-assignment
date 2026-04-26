package com.example.rich_text_editor.editor.model

import kotlinx.serialization.Serializable

@Serializable
data class SpanDto(
    val start: Int,
    val end: Int,
    val bold: Boolean = false,
    val italic: Boolean = false,
    val underline: Boolean = false,
    val strikethrough: Boolean = false,
    val foregroundColor: Long? = null,
    val highlightColor: Long? = null,
)
