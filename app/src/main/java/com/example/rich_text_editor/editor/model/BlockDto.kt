package com.example.rich_text_editor.editor.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@Serializable
data class DocumentContentDto(
    val schemaVersion: Int = CURRENT_SCHEMA_VERSION,
    val blocks: List<BlockDto> = emptyList(),
) {
    companion object {
        const val CURRENT_SCHEMA_VERSION: Int = 2
    }
}

@Serializable
@JsonClassDiscriminator("type")
sealed interface BlockDto {
    @Serializable
    @SerialName("text")
    data class Text(
        val heading: String? = null,
        val text: String,
        val spans: List<SpanDto> = emptyList(),
    ) : BlockDto

    @Serializable
    @SerialName("list")
    data class ListBlock(
        val ordered: Boolean,
        val items: List<ListItemDto>,
    ) : BlockDto

    @Serializable
    @SerialName("image")
    data class Image(
        val fileName: String,
        val widthDp: Int = 280,
    ) : BlockDto
}
