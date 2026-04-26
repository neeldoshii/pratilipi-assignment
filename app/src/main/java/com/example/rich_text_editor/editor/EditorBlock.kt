package com.example.rich_text_editor.editor

import androidx.compose.ui.text.input.TextFieldValue
import com.example.rich_text_editor.editor.model.HeadingLevel
import java.util.UUID

sealed class EditorBlock {
    abstract val id: String

    data class Text(
        override val id: String = UUID.randomUUID().toString(),
        val heading: HeadingLevel = HeadingLevel.Body,
        val value: TextFieldValue = TextFieldValue(""),
    ) : EditorBlock()

    data class List(
        override val id: String = UUID.randomUUID().toString(),
        val ordered: Boolean,
        val items: kotlin.collections.List<ListItemRow>,
    ) : EditorBlock()

    data class Image(
        override val id: String = UUID.randomUUID().toString(),
        val fileName: String,
        val widthDp: Int = 280,
    ) : EditorBlock()
}

data class ListItemRow(
    val id: String = UUID.randomUUID().toString(),
    val value: TextFieldValue = TextFieldValue(""),
)
