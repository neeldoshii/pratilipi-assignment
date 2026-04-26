package com.example.rich_text_editor.editor

import com.example.rich_text_editor.editor.model.BlockDto
import com.example.rich_text_editor.editor.model.DocumentContentDto
import com.example.rich_text_editor.editor.model.ListItemDto
import com.example.rich_text_editor.editor.model.SpanDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DocumentJsonCodecTest {

    @Test
    fun roundTrip_preservesHeadingsListsAndSpans() {
        val original = DocumentContentDto(
            schemaVersion = 2,
            blocks = listOf(
                BlockDto.Text(heading = "h1", text = "Title", spans = emptyList()),
                BlockDto.Text(heading = "h3", text = "Section", spans = emptyList()),
                BlockDto.ListBlock(
                    ordered = false,
                    items = listOf(
                        ListItemDto("a", emptyList()),
                        ListItemDto("b", listOf(SpanDto(0, 1, bold = true))),
                    ),
                ),
                BlockDto.ListBlock(
                    ordered = true,
                    items = listOf(ListItemDto("one", emptyList())),
                ),
            ),
        )
        val json = DocumentJsonCodec.encode(original)
        val decoded = DocumentJsonCodec.decode(json)
        assertEquals(original.schemaVersion, decoded.schemaVersion)
        assertEquals(original.blocks.size, decoded.blocks.size)
        val b0 = decoded.blocks[0] as BlockDto.Text
        assertEquals("h1", b0.heading)
        assertEquals("Title", b0.text)
        val list = decoded.blocks[2] as BlockDto.ListBlock
        assertTrue(!list.ordered)
        assertEquals(2, list.items.size)
        assertEquals("b", list.items[1].text)
        assertEquals(1, list.items[1].spans.size)
        assertTrue(list.items[1].spans[0].bold)
    }

    @Test
    fun sampleFixture_fromPlan_decodes() {
        val doc = DocumentJsonCodec.decode(SampleDocuments.SAMPLE_CONTENT_JSON)
        assertEquals(2, doc.schemaVersion)
        assertTrue(doc.blocks.size >= 5)
        val lorem = doc.blocks.first { it is BlockDto.Text && (it as BlockDto.Text).text.startsWith("Lorem Ipsum") } as BlockDto.Text
        assertEquals("Lorem Ipsum", lorem.text.substring(0, 11))
        val boldIntro = lorem.spans.first { it.start == 0 && it.end == 11 }
        assertTrue(boldIntro.bold)
        val boldMid = lorem.spans.first { it.start == 153 && it.end == 286 }
        assertTrue(boldMid.bold)
        val italic = lorem.spans.first { it.start == 419 && it.end == 457 }
        assertTrue(italic.italic)
    }
}
