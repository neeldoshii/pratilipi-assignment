package com.example.rich_text_editor.editor.model

import kotlinx.serialization.Serializable

@Serializable
data class ListItemDto(
    val text: String,
    val spans: List<SpanDto> = emptyList(),
)
