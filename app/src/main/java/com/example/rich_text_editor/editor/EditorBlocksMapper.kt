package com.example.rich_text_editor.editor

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.example.rich_text_editor.editor.model.BlockDto
import com.example.rich_text_editor.editor.model.DocumentContentDto
import com.example.rich_text_editor.editor.model.HeadingLevel
import com.example.rich_text_editor.editor.model.ListItemDto

object EditorBlocksMapper {

    fun fromDocument(doc: DocumentContentDto): List<EditorBlock> {
        val mapped = doc.blocks.map { block ->
            when (block) {
                is BlockDto.Text -> {
                    val heading = HeadingLevel.fromJson(block.heading)
                    val ann = RichTextMapper.spansToAnnotatedString(block.text, block.spans)
                    val end = ann.text.length
                    EditorBlock.Text(
                        heading = heading,
                        value = TextFieldValue(annotatedString = ann, selection = TextRange(end)),
                    )
                }
                is BlockDto.ListBlock -> {
                    EditorBlock.List(
                        ordered = block.ordered,
                        items = block.items.map { item ->
                            val ann = RichTextMapper.spansToAnnotatedString(item.text, item.spans)
                            val end = ann.text.length
                            ListItemRow(
                                value = TextFieldValue(annotatedString = ann, selection = TextRange(end)),
                            )
                        },
                    )
                }
                is BlockDto.Image -> EditorBlock.Image(fileName = block.fileName, widthDp = block.widthDp)
            }
        }
        return if (mapped.isEmpty()) listOf(EditorBlock.Text()) else mapped
    }

    fun toDocument(blocks: List<EditorBlock>): DocumentContentDto {
        val dtoBlocks = blocks.map { block ->
            when (block) {
                is EditorBlock.Text -> {
                    val plain = block.value.annotatedString.text
                    val spans = RichTextMapper.annotatedStringToSpans(block.value.annotatedString)
                    BlockDto.Text(
                        heading = block.heading.toJson(),
                        text = plain,
                        spans = spans,
                    )
                }
                is EditorBlock.List -> {
                    BlockDto.ListBlock(
                        ordered = block.ordered,
                        items = block.items.map { row: ListItemRow ->
                            ListItemDto(
                                text = row.value.annotatedString.text,
                                spans = RichTextMapper.annotatedStringToSpans(row.value.annotatedString),
                            )
                        },
                    )
                }
                is EditorBlock.Image -> BlockDto.Image(fileName = block.fileName, widthDp = block.widthDp)
            }
        }
        return DocumentContentDto(
            schemaVersion = DocumentContentDto.CURRENT_SCHEMA_VERSION,
            blocks = dtoBlocks,
        )
    }
}
