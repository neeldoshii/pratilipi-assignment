package com.example.rich_text_editor.ui.editor

sealed class EditorFocus {
    data class Text(val index: Int) : EditorFocus()
    data class ListItem(val blockIndex: Int, val itemIndex: Int) : EditorFocus()
}
