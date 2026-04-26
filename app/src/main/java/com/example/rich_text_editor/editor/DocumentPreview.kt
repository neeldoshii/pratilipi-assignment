package com.example.rich_text_editor.editor

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import com.example.rich_text_editor.editor.model.BlockDto
import com.example.rich_text_editor.editor.model.DocumentContentDto

object DocumentPreview {

    private const val MaxPreviewCodeUnits = 320

    /**
     * Flattens the first text and list blocks into one [AnnotatedString] for list previews.
     * Reuses [RichTextMapper] for inline styles; image blocks become a short glyph row.
     */
    fun buildPreviewAnnotated(contentJson: String): AnnotatedString {
        val doc = DocumentJsonCodec.decode(contentJson)
        return buildPreviewAnnotated(doc)
    }

    fun buildPreviewAnnotated(doc: DocumentContentDto): AnnotatedString = buildAnnotatedString {
        var orderedIndex = 1
        for (block in doc.blocks) {
            if (length >= MaxPreviewCodeUnits) break
            when (block) {
                is BlockDto.Text -> {
                    appendClamped(RichTextMapper.spansToAnnotatedString(block.text, block.spans))
                    append("\n")
                }
                is BlockDto.ListBlock -> {
                    block.items.forEach { item ->
                        if (length >= MaxPreviewCodeUnits) return@buildAnnotatedString
                        val prefix = if (block.ordered) "${orderedIndex++}. " else "• "
                        append(prefix)
                        appendClamped(RichTextMapper.spansToAnnotatedString(item.text, item.spans))
                        append("\n")
                    }
                }
                is BlockDto.Image -> {
                    append("📷\n")
                }
            }
        }
    }

    private fun AnnotatedString.Builder.appendClamped(piece: AnnotatedString) {
        val room = MaxPreviewCodeUnits - length
        if (room <= 0) return
        if (piece.length <= room) {
            append(piece)
        } else {
            append(piece.subSequence(0, room))
        }
    }
}
