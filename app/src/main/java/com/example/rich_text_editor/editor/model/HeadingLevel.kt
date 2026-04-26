package com.example.rich_text_editor.editor.model

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.rich_text_editor.ui.theme.Typography

enum class HeadingLevel {
    H1,
    H2,
    H3,
    Body;

    fun toJson(): String? = when (this) {
        H1 -> "h1"
        H2 -> "h2"
        H3 -> "h3"
        Body -> null
    }

    companion object {
        fun fromJson(value: String?): HeadingLevel = when (value?.lowercase()) {
            "h1" -> H1
            "h2" -> H2
            "h3" -> H3
            else -> Body
        }

        fun toTextStyle(level: HeadingLevel): TextStyle = when (level) {
            H1 -> Typography.bodyLarge.copy(fontSize = 28.sp, fontWeight = FontWeight.Bold)
            H2 -> Typography.bodyLarge.copy(fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
            H3 -> Typography.bodyLarge.copy(fontSize = 18.sp, fontWeight = FontWeight.Medium)
            Body -> Typography.bodyLarge
        }
    }
}
