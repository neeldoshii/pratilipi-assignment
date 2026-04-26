package com.example.rich_text_editor.editor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp

object SpanMutation {

    fun toggleBold(value: TextFieldValue): TextFieldValue = toggleFontWeight(value)

    fun toggleItalic(value: TextFieldValue): TextFieldValue = toggleFontStyle(value)

    fun toggleUnderline(value: TextFieldValue): TextFieldValue =
        toggleDecorationFlag(value, isUnderline = true)

    fun toggleStrikethrough(value: TextFieldValue): TextFieldValue =
        toggleDecorationFlag(value, isUnderline = false)

    fun applyForegroundColor(value: TextFieldValue, color: Color): TextFieldValue =
        mapTinyRange(value) { it.copy(fg = color.toArgb().toUInt().toLong()) }

    fun applyHighlightColor(value: TextFieldValue, color: Color): TextFieldValue =
        mapTinyRange(value) { it.copy(bg = color.toArgb().toUInt().toLong()) }

    fun clearForegroundColor(value: TextFieldValue): TextFieldValue =
        mapTinyRange(value) { it.copy(fg = null) }

    fun clearHighlightColor(value: TextFieldValue): TextFieldValue =
        mapTinyRange(value) { it.copy(bg = null) }

    fun applyHeading1(value: TextFieldValue): TextFieldValue =
        mapEntireText(value) { it.copy(fontSize = 28, bold = true) }

    fun applyHeading2(value: TextFieldValue): TextFieldValue =
        mapEntireText(value) { it.copy(fontSize = 22, bold = true) }

    fun applyHeading3(value: TextFieldValue): TextFieldValue =
        mapEntireText(value) { it.copy(fontSize = 18, bold = true) }

    fun applyBodyText(value: TextFieldValue): TextFieldValue =
        mapEntireText(value) { it.copy(fontSize = null, bold = false) }

    internal data class Tiny(
        val bold: Boolean,
        val italic: Boolean,
        val underline: Boolean,
        val strikethrough: Boolean,
        val fg: Long?,
        val bg: Long?,
        val fontSize: Int?,
    )

    private fun SpanStyle.toTiny(): Tiny {
        val deco = textDecoration
        return Tiny(
            bold = fontWeight == FontWeight.Bold,
            italic = fontStyle == FontStyle.Italic,
            underline = deco.hasUnderline(),
            strikethrough = deco.hasStrikethrough(),
            fg = color.takeIf { it != Color.Unspecified }?.toArgb()?.toUInt()?.toLong(),
            bg = background.takeIf { it != Color.Unspecified }?.toArgb()?.toUInt()?.toLong(),
            fontSize = fontSize.takeIf { it != androidx.compose.ui.unit.TextUnit.Unspecified }?.value?.toInt(),
        )
    }

    private fun Tiny.toSpanStyle(): SpanStyle {
        val deco = when {
            underline && strikethrough -> TextDecoration.combine(
                listOf(TextDecoration.Underline, TextDecoration.LineThrough),
            )
            underline -> TextDecoration.Underline
            strikethrough -> TextDecoration.LineThrough
            else -> null
        }
        return SpanStyle(
            fontWeight = if (bold) FontWeight.Bold else null,
            fontStyle = if (italic) FontStyle.Italic else null,
            textDecoration = deco,
            fontSize = fontSize?.sp ?: androidx.compose.ui.unit.TextUnit.Unspecified,
            color = fg?.let { Color((it and 0xFFFFFFFFL).toInt()) } ?: Color.Unspecified,
            background = bg?.let { Color((it and 0xFFFFFFFFL).toInt()) } ?: Color.Unspecified,
        )
    }

    private fun TextDecoration?.hasUnderline(): Boolean =
        this != null && this != TextDecoration.None && this.contains(TextDecoration.Underline)

    private fun TextDecoration?.hasStrikethrough(): Boolean =
        this != null && this != TextDecoration.None && this.contains(TextDecoration.LineThrough)

    private fun expandTiny(text: AnnotatedString): Array<Tiny> {
        val n = text.length
        val arr = Array(n) { Tiny(false, false, false, false, null, null, null) }
        text.spanStyles.forEach { range ->
            val t = range.item.toTiny()
            for (i in range.start until minOf(range.end, n)) {
                if (i >= 0) {
                    arr[i] = mergeTiny(arr[i], t)
                }
            }
        }
        return arr
    }

    private fun mergeTiny(a: Tiny, b: Tiny): Tiny = Tiny(
        bold = a.bold || b.bold,
        italic = a.italic || b.italic,
        underline = a.underline || b.underline,
        strikethrough = a.strikethrough || b.strikethrough,
        fg = b.fg ?: a.fg,
        bg = b.bg ?: a.bg,
        fontSize = b.fontSize ?: a.fontSize,
    )
    
    private fun getCurrentFormattingAt(value: TextFieldValue, position: Int): Tiny {
        if (value.text.isEmpty()) return Tiny(false, false, false, false, null, null, null)
        val arr = expandTiny(value.annotatedString)
        return when {
            position > 0 -> arr[position - 1]
            position < arr.size -> arr[position]
            else -> Tiny(false, false, false, false, null, null, null)
        }
    }
    
    private fun toggleFormattingAtCursor(value: TextFieldValue, cursorPos: Int, transform: (Tiny) -> Tiny): TextFieldValue {
        // For cursor position (no selection), apply formatting to existing character at cursor
        if (cursorPos < value.text.length) {
            val arr = expandTiny(value.annotatedString)
            arr[cursorPos] = transform(arr[cursorPos])
            return rebuild(value, arr)
        }
        // If at end of text, just return value - typing state will handle new characters
        return value
    }

    private fun toggleFontWeight(value: TextFieldValue): TextFieldValue {
        val s = value.selection.min
        val e = value.selection.max
        if (s >= e) {
            return toggleFormattingAtCursor(value, s) { it.copy(bold = !it.bold) }
        }
        
        // Handle text selection
        val arr = expandTiny(value.annotatedString)
        val on = !(s until e).any { arr[it].bold }
        for (i in s until e) {
            arr[i] = arr[i].copy(bold = on)
        }
        return rebuild(value, arr)
    }

    private fun toggleFontStyle(value: TextFieldValue): TextFieldValue {
        val s = value.selection.min
        val e = value.selection.max
        if (s >= e) {
            return toggleFormattingAtCursor(value, s) { it.copy(italic = !it.italic) }
        }
        val arr = expandTiny(value.annotatedString)
        val on = !(s until e).any { arr[it].italic }
        for (i in s until e) {
            arr[i] = arr[i].copy(italic = on)
        }
        return rebuild(value, arr)
    }

    private fun toggleDecorationFlag(value: TextFieldValue, isUnderline: Boolean): TextFieldValue {
        val s = value.selection.min
        val e = value.selection.max
        if (s >= e) {
            return if (isUnderline) {
                toggleFormattingAtCursor(value, s) { it.copy(underline = !it.underline) }
            } else {
                toggleFormattingAtCursor(value, s) { it.copy(strikethrough = !it.strikethrough) }
            }
        }
        val arr = expandTiny(value.annotatedString)
        val flag: (Tiny) -> Boolean = if (isUnderline) Tiny::underline else Tiny::strikethrough
        val on = !(s until e).any { flag(arr[it]) }
        for (i in s until e) {
            arr[i] = if (isUnderline) arr[i].copy(underline = on) else arr[i].copy(strikethrough = on)
        }
        return rebuild(value, arr)
    }

    private fun mapTinyRange(value: TextFieldValue, map: (Tiny) -> Tiny): TextFieldValue {
        val s = value.selection.min
        val e = value.selection.max
        if (s >= e) {
            return toggleFormattingAtCursor(value, s, map)
        }
        val arr = expandTiny(value.annotatedString)
        for (i in s until e) {
            arr[i] = map(arr[i])
        }
        return rebuild(value, arr)
    }

    private fun mapEntireText(value: TextFieldValue, map: (Tiny) -> Tiny): TextFieldValue {
        val arr = expandTiny(value.annotatedString)
        for (i in arr.indices) {
            arr[i] = map(arr[i])
        }
        return rebuild(value, arr)
    }

    private fun rebuild(value: TextFieldValue, tiny: Array<Tiny>): TextFieldValue {
        val plain = value.annotatedString.text
        if (plain.isEmpty()) {
            return value.copy(annotatedString = AnnotatedString(""), selection = value.selection)
        }
        val builder = AnnotatedString.Builder()
        builder.append(plain)
        var i = 0
        while (i < plain.length) {
            val t = tiny[i]
            val style = t.toSpanStyle()
            var j = i + 1
            while (j < plain.length && tiny[j] == t) j++
            if (!t.isEmpty()) {
                builder.addStyle(style, i, j)
            }
            i = j
        }
        return value.copy(annotatedString = builder.toAnnotatedString(), selection = value.selection)
    }

    private fun Tiny.isEmpty(): Boolean =
        !bold && !italic && !underline && !strikethrough && fg == null && bg == null && fontSize == null
        
    // Public API for EditorViewModel
    data class FormattingState(
        val bold: Boolean = false,
        val italic: Boolean = false,
        val underline: Boolean = false,
        val strikethrough: Boolean = false,
        val foregroundColor: Long? = null,
        val highlightColor: Long? = null,
        val fontSize: Int? = null
    ) {
        internal fun toTiny() = Tiny(bold, italic, underline, strikethrough, foregroundColor, highlightColor, fontSize)
    }
    
    fun getFormattingAtPosition(value: TextFieldValue, position: Int): FormattingState {
        val tiny = getCurrentFormattingAt(value, position)
        return FormattingState(
            bold = tiny.bold,
            italic = tiny.italic,
            underline = tiny.underline,
            strikethrough = tiny.strikethrough,
            foregroundColor = tiny.fg,
            highlightColor = tiny.bg,
            fontSize = tiny.fontSize
        )
    }
    
    fun applyFormattingToRange(
        value: TextFieldValue, 
        start: Int, 
        end: Int, 
        formatting: FormattingState
    ): TextFieldValue {
        if (start >= end || start < 0 || end > value.text.length) return value
        
        val arr = expandTiny(value.annotatedString)
        val tiny = formatting.toTiny()
        
        for (i in start until end) {
            if (i < arr.size) {
                arr[i] = mergeTiny(arr[i], tiny)
            }
        }
        
        return rebuild(value, arr)
    }
}
