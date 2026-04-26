package com.example.rich_text_editor.editor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import com.example.rich_text_editor.editor.model.HeadingLevel
import com.example.rich_text_editor.editor.model.SpanDto

object RichTextMapper {

    fun spansToAnnotatedString(
        plain: String,
        spans: List<SpanDto>,
        paragraphStyle: SpanStyle? = null,
    ): AnnotatedString = buildAnnotatedString {
        append(plain)
        paragraphStyle?.let { addStyle(it, 0, plain.length) }
        for (span in spans) {
            val start = span.start.coerceIn(0, plain.length)
            val end = span.end.coerceIn(start, plain.length)
            if (start >= end) continue
            val decorations = buildList {
                if (span.underline) add(TextDecoration.Underline)
                if (span.strikethrough) add(TextDecoration.LineThrough)
            }
            val decoration =
                if (decorations.isEmpty()) null
                else TextDecoration.combine(decorations)
            addStyle(
                SpanStyle(
                    fontWeight = if (span.bold) FontWeight.Bold else null,
                    fontStyle = if (span.italic) FontStyle.Italic else null,
                    textDecoration = decoration,
                    color = span.foregroundColor?.let { Color((it and 0xFFFFFFFFL).toInt()) } ?: Color.Unspecified,
                    background = span.highlightColor?.let { Color((it and 0xFFFFFFFFL).toInt()) } ?: Color.Unspecified,
                ),
                start,
                end,
            )
        }
    }

    fun annotatedStringToSpans(text: CharSequence): List<SpanDto> {
        if (text !is AnnotatedString) return emptyList()
        val plain = text.text
        if (plain.isEmpty()) return emptyList()
        val out = mutableListOf<SpanDto>()
        text.spanStyles.forEach { range ->
            val style = range.item
            val bold = style.fontWeight == FontWeight.Bold
            val italic = style.fontStyle == FontStyle.Italic
            val deco = style.textDecoration
            val underline = deco.contains(TextDecoration.Underline)
            val strikethrough = deco.contains(TextDecoration.LineThrough)
            val fg = style.color.takeIf { it != Color.Unspecified }?.toArgb()?.toUInt()?.toLong()
            val bg = style.background.takeIf { it != Color.Unspecified }?.toArgb()?.toUInt()?.toLong()
            if (bold || italic || underline || strikethrough || fg != null || bg != null) {
                out.add(
                    SpanDto(
                        start = range.start,
                        end = range.end,
                        bold = bold,
                        italic = italic,
                        underline = underline,
                        strikethrough = strikethrough,
                        foregroundColor = fg,
                        highlightColor = bg,
                    ),
                )
            }
        }
        return mergeAdjacentIdentical(out)
    }

    private fun TextDecoration?.contains(part: TextDecoration): Boolean =
        (this ?: TextDecoration.None).let { it.contains(part) }

    private fun mergeAdjacentIdentical(spans: List<SpanDto>): List<SpanDto> {
        if (spans.isEmpty()) return spans
        val sorted = spans.sortedBy { it.start }
        val merged = mutableListOf<SpanDto>()
        var cur = sorted.first()
        for (i in 1 until sorted.size) {
            val next = sorted[i]
            if (cur.end == next.start && spansEqualStyle(cur, next)) {
                cur = cur.copy(end = next.end)
            } else {
                merged.add(cur)
                cur = next
            }
        }
        merged.add(cur)
        return merged
    }

    private fun spansEqualStyle(a: SpanDto, b: SpanDto): Boolean =
        a.bold == b.bold &&
            a.italic == b.italic &&
            a.underline == b.underline &&
            a.strikethrough == b.strikethrough &&
            a.foregroundColor == b.foregroundColor &&
            a.highlightColor == b.highlightColor

    fun headingParagraphStyle(level: HeadingLevel, color: Color = Color.Unspecified): SpanStyle {
        val base = HeadingLevel.toTextStyle(level)
        return SpanStyle(
            fontSize = base.fontSize,
            fontWeight = base.fontWeight,
            fontStyle = base.fontStyle,
            letterSpacing = base.letterSpacing,
            color = color,
        )
    }
}
