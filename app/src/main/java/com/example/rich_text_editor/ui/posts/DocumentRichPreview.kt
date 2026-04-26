package com.example.rich_text_editor.ui.posts

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.example.rich_text_editor.editor.DocumentPreview

@Composable
fun DocumentRichPreview(
    contentJson: String,
    modifier: Modifier = Modifier,
    maxLines: Int = 2,
) {
    val annotated = DocumentPreview.buildPreviewAnnotated(contentJson)
    Text(
        text = annotated,
        modifier = modifier,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
    )
}
