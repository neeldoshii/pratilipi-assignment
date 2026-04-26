package com.example.rich_text_editor.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun FormatToolbar(
    enabled: Boolean,
    isBoldActive: Boolean = false,
    isItalicActive: Boolean = false,
    isUnderlineActive: Boolean = false,
    isStrikethroughActive: Boolean = false,
    onBold: () -> Unit,
    onItalic: () -> Unit,
    onUnderline: () -> Unit,
    onStrikethrough: () -> Unit,
    onForeground: (Color) -> Unit,
    onHighlight: (Color) -> Unit,
    onH1: () -> Unit = {},
    onH2: () -> Unit = {},
    onH3: () -> Unit = {},
    onBody: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val swatches = listOf(
        Color(0xFFE53935),
        Color(0xFF1E88E5),
        Color(0xFF43A047),
        Color(0xFFFDD835),
        Color(0xFF8E24AA),
    )
    Surface(
        tonalElevation = 3.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 4.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            IconButton(
                onClick = onBold, 
                enabled = enabled,
                modifier = Modifier.background(
                    if (isBoldActive) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                    RoundedCornerShape(8.dp)
                )
            ) {
                Icon(Icons.Filled.FormatBold, contentDescription = null)
            }
            IconButton(
                onClick = onItalic, 
                enabled = enabled,
                modifier = Modifier.background(
                    if (isItalicActive) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                    RoundedCornerShape(8.dp)
                )
            ) {
                Icon(Icons.Filled.FormatItalic, contentDescription = null)
            }
            IconButton(
                onClick = onUnderline, 
                enabled = enabled,
                modifier = Modifier.background(
                    if (isUnderlineActive) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                    RoundedCornerShape(8.dp)
                )
            ) {
                Icon(Icons.Filled.FormatUnderlined, contentDescription = null)
            }
            IconButton(
                onClick = onStrikethrough, 
                enabled = enabled,
                modifier = Modifier.background(
                    if (isStrikethroughActive) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                    RoundedCornerShape(8.dp)
                )
            ) {
                Icon(Icons.Filled.FormatStrikethrough, contentDescription = null)
            }
            Button(onClick = onH1, enabled = enabled) {
                Text("H1")
            }
            Button(onClick = onH2, enabled = enabled) {
                Text("H2")
            }
            Button(onClick = onH3, enabled = enabled) {
                Text("H3")
            }
            Button(onClick = onBody, enabled = enabled) {
                Text("Body")
            }
            for (c in swatches) {
                IconButton(
                    onClick = { onForeground(c) },
                    enabled = enabled,
                ) {
                    Box(
                        Modifier
                            .size(22.dp)
                            .background(c, CircleShape),
                    )
                }
            }
            for (c in swatches) {
                IconButton(
                    onClick = { onHighlight(c) },
                    enabled = enabled,
                ) {
                    Box(
                        Modifier
                            .size(22.dp)
                            .background(c.copy(alpha = 0.35f), CircleShape),
                    )
                }
            }
        }
    }
}
