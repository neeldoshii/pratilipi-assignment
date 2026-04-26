package com.example.rich_text_editor.ui.editor

import android.content.Context
import android.os.Build
import java.io.File
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.rich_text_editor.R
import com.example.rich_text_editor.RichEditorApp
import com.example.rich_text_editor.editor.DocumentPreview
import com.example.rich_text_editor.editor.EditorBlock
import com.example.rich_text_editor.editor.EditorBlocksMapper
import com.example.rich_text_editor.editor.ListItemRow
import com.example.rich_text_editor.editor.model.HeadingLevel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val app = context.applicationContext as RichEditorApp
    val title by viewModel.title.collectAsStateWithLifecycle()
    val blocks by viewModel.blocks.collectAsStateWithLifecycle()
    val focus by viewModel.focus.collectAsStateWithLifecycle()
    val loading by viewModel.isLoading.collectAsStateWithLifecycle()
    val hasUnsavedChanges by viewModel.hasUnsavedChanges.collectAsStateWithLifecycle()

    var menuExpanded by remember { mutableStateOf(false) }
    var isPreviewMode by remember { mutableStateOf(false) }

    val pickMedia = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) viewModel.importImageFromUri(uri)
    }
    val getContent = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        if (uri != null) viewModel.importImageFromUri(uri)
    }

    fun launchImagePicker() {
        if (Build.VERSION.SDK_INT >= 33) {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        } else {
            getContent.launch("image/*")
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.editor_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.saveManually() },
                        enabled = hasUnsavedChanges
                    ) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = "Save",
                            tint = if (hasUnsavedChanges) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TextButton(onClick = { isPreviewMode = !isPreviewMode }) {
                        Text(if (isPreviewMode) "Edit" else "Preview")
                    }
                    IconButton(onClick = { launchImagePicker() }) {
                        Icon(Icons.Filled.Image, contentDescription = stringResource(R.string.insert_image_content_description))
                    }
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = stringResource(R.string.editor_overflow_content_description))
                    }
                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.editor_add_paragraph)) },
                            onClick = {
                                menuExpanded = false
                                viewModel.addTextBlock()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.editor_add_bullets)) },
                            onClick = {
                                menuExpanded = false
                                viewModel.addListBlock(ordered = false)
                            },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.editor_add_numbered)) },
                            onClick = {
                                menuExpanded = false
                                viewModel.addListBlock(ordered = true)
                            },
                        )
                    }
                },
            )
        },
        bottomBar = {
            val activeFormats by viewModel.activeFormats.collectAsStateWithLifecycle()
            FormatToolbar(
                enabled = focus != null,
                isBoldActive = activeFormats.bold,
                isItalicActive = activeFormats.italic,
                isUnderlineActive = activeFormats.underline,
                isStrikethroughActive = activeFormats.strikethrough,
                onBold = { viewModel.toggleBold() },
                onItalic = { viewModel.toggleItalic() },
                onUnderline = { viewModel.toggleUnderline() },
                onStrikethrough = { viewModel.toggleStrikethrough() },
                onForeground = { viewModel.applyForeground(it) },
                onHighlight = { viewModel.applyHighlight(it) },
                onH1 = { viewModel.applyHeading1() },
                onH2 = { viewModel.applyHeading2() },
                onH3 = { viewModel.applyHeading3() },
                onBody = { viewModel.applyBodyText() },
                modifier = Modifier
                    .navigationBarsPadding()
                    .imePadding(),
            )
        },
    ) { padding ->
        if (loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            if (isPreviewMode) {
                PreviewMode(
                    title = title,
                    blocks = blocks,
                    viewModel = viewModel,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp)
                )
            } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = viewModel::onTitleChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.editor_document_title_label)) },
                    singleLine = true,
                )
                Spacer(Modifier.height(8.dp))
            }
            itemsIndexed(blocks, key = { _, block -> block.id }) { index, block ->
                when (block) {
                    is EditorBlock.Text -> TextBlockEditor(
                        block = block,
                        index = index,
                        viewModel = viewModel,
                    )
                    is EditorBlock.List -> ListBlockEditor(
                        block = block,
                        index = index,
                        viewModel = viewModel,
                    )
                    is EditorBlock.Image -> ImageBlockEditor(
                        block = block,
                        index = index,
                        viewModel = viewModel,
                        imageLoaderContext = context,
                        resolvePath = { viewModel.resolveImagePath(it) },
                    )
                }
                HorizontalDivider()
            }
            item {
                TextButton(onClick = { viewModel.addTextBlock() }) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Text(stringResource(R.string.editor_add_paragraph))
                }
            }
        }
            }
        }
    }
}

@Composable
private fun PreviewMode(
    title: String,
    blocks: List<EditorBlock>,
    viewModel: EditorViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        itemsIndexed(blocks) { _, block ->
            when (block) {
                is EditorBlock.Text -> {
                    PreviewTextBlock(
                        block = block,
                        viewModel = viewModel,
                        context = context
                    )
                }
                is EditorBlock.List -> {
                    PreviewListBlock(block = block)
                }
                is EditorBlock.Image -> {
                    PreviewImageBlock(
                        block = block,
                        viewModel = viewModel,
                        context = context
                    )
                }
            }
        }
    }
}

@Composable
private fun PreviewTextBlock(
    block: EditorBlock.Text,
    viewModel: EditorViewModel,
    context: Context
) {
    val text = block.value.text
    val imagePattern = Regex("""\[img:([^\]]+)\]""")
    val hasImages = imagePattern.containsMatchIn(text)
    
    if (hasImages) {
        // Render mixed text and images like in editor
        PreviewRichTextWithInlineImages(
            textFieldValue = block.value,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
            resolvePath = { viewModel.resolveImagePath(it) },
            context = context
        )
    } else {
        Text(
            text = block.value.annotatedString,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun PreviewListBlock(
    block: EditorBlock.List
) {
    Column {
        block.items.forEachIndexed { index, item ->
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (block.ordered) "${index + 1}. " else "• ",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = item.value.annotatedString,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun PreviewImageBlock(
    block: EditorBlock.Image,
    viewModel: EditorViewModel,
    context: Context
) {
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(viewModel.resolveImagePath(block.fileName))
            .crossfade(true)
            .build(),
        contentDescription = "Preview image",
        modifier = Modifier
            .width(block.widthDp.dp)
            .height(180.dp),
        contentScale = ContentScale.Crop
    )
}

@Composable
private fun PreviewRichTextWithInlineImages(
    textFieldValue: androidx.compose.ui.text.input.TextFieldValue,
    textStyle: androidx.compose.ui.text.TextStyle,
    resolvePath: (String) -> File,
    context: Context,
) {
    val text = textFieldValue.text
    val imagePattern = Regex("""\[img:([^\]]+)\]""")
    val parts = mutableListOf<TextPart>()
    
    var lastIndex = 0
    imagePattern.findAll(text).forEach { match ->
        // Add text before image
        if (match.range.first > lastIndex) {
            parts.add(TextPart.Text(text.substring(lastIndex, match.range.first)))
        }
        // Add image
        val fileName = match.groupValues[1]
        parts.add(TextPart.Image(fileName))
        lastIndex = match.range.last + 1
    }
    // Add remaining text
    if (lastIndex < text.length) {
        parts.add(TextPart.Text(text.substring(lastIndex)))
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        parts.forEach { part ->
            when (part) {
                is TextPart.Text -> {
                    if (part.content.isNotEmpty()) {
                        Text(
                            text = part.content,
                            style = textStyle,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                        )
                    }
                }
                is TextPart.Image -> {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(resolvePath(part.fileName))
                            .crossfade(true)
                            .build(),
                        contentDescription = "Inline image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(vertical = 4.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

@Composable
private fun TextBlockEditor(
    block: EditorBlock.Text,
    index: Int,
    viewModel: EditorViewModel,
) {
    // Use base typography but let AnnotatedString handle all formatting
    val baseTextStyle = MaterialTheme.typography.bodyLarge.copy(
        color = MaterialTheme.colorScheme.onSurface
    )
    
    val context = LocalContext.current
    
    Column {
        // Check if text contains inline images
        val text = block.value.text
        val imagePattern = Regex("""\[img:([^\]]+)\]""")
        val hasImages = imagePattern.containsMatchIn(text)
        
        if (hasImages) {
            // Render mixed text and images
            RichTextWithInlineImages(
                textFieldValue = block.value,
                onValueChange = { viewModel.onTextBlockChange(index, it) },
                onFocusChanged = { focused ->
                    if (focused) {
                        viewModel.onFocusChange(EditorFocus.Text(index))
                    }
                },
                textStyle = baseTextStyle,
                resolvePath = { viewModel.resolveImagePath(it) },
                context = context
            )
        } else {
            // Standard text field for text without images
            BasicTextField(
                value = block.value,
                onValueChange = { viewModel.onTextBlockChange(index, it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { state ->
                        if (state.isFocused) {
                            viewModel.onFocusChange(EditorFocus.Text(index))
                        }
                    },
                textStyle = baseTextStyle,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                decorationBox = { innerTextField ->
                    if (block.value.text.isEmpty()) {
                        Text("Start writing...", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                    }
                    innerTextField()
                },
            )
        }
        
        TextButton(onClick = { viewModel.removeBlock(index) }) {
            Text(stringResource(R.string.editor_remove_block))
        }
    }
}


@Composable
private fun ListBlockEditor(
    block: EditorBlock.List,
    index: Int,
    viewModel: EditorViewModel,
) {
    Column {
        Text(
            if (block.ordered) stringResource(R.string.editor_numbered_list) else stringResource(R.string.editor_bullet_list),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        block.items.forEachIndexed { itemIndex, row ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
            ) {
                Text(
                    text = if (block.ordered) "${itemIndex + 1}. " else "• ",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                BasicTextField(
                    value = row.value,
                    onValueChange = { viewModel.onListItemChange(index, itemIndex, it) },
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged { state ->
                            if (state.isFocused) {
                                viewModel.onFocusChange(EditorFocus.ListItem(index, itemIndex))
                            }
                        },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    decorationBox = { innerTextField ->
                        if (row.value.text.isEmpty()) {
                            Text("List item...", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                        }
                        innerTextField()
                    },
                )
            }
        }
        TextButton(onClick = { viewModel.addListItem(index) }) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Text(stringResource(R.string.editor_add_list_item))
        }
        TextButton(onClick = { viewModel.removeBlock(index) }) {
            Text(stringResource(R.string.editor_remove_block))
        }
    }
}

@Composable
private fun ImageBlockEditor(
    block: EditorBlock.Image,
    index: Int,
    viewModel: EditorViewModel,
    imageLoaderContext: Context,
    resolvePath: (String) -> File,
) {
    Column {
        AsyncImage(
            model = ImageRequest.Builder(imageLoaderContext)
                .data(resolvePath(block.fileName))
                .crossfade(true)
                .build(),
            contentDescription = stringResource(R.string.image_content_description),
            modifier = Modifier
                .width(block.widthDp.dp)
                .height(180.dp),
        )
        Slider(
            value = block.widthDp.toFloat(),
            onValueChange = { viewModel.setImageWidth(index, it.roundToInt()) },
            valueRange = 80f..480f,
        )
        TextButton(onClick = { viewModel.removeBlock(index) }) {
            Text(stringResource(R.string.editor_remove_block))
        }
    }
}

@Composable
private fun RichTextWithInlineImages(
    textFieldValue: androidx.compose.ui.text.input.TextFieldValue,
    onValueChange: (androidx.compose.ui.text.input.TextFieldValue) -> Unit,
    onFocusChanged: (Boolean) -> Unit,
    textStyle: androidx.compose.ui.text.TextStyle,
    resolvePath: (String) -> File,
    context: Context,
) {
    val text = textFieldValue.text
    val imagePattern = Regex("""\[img:([^\]]+)\]""")
    val parts = mutableListOf<TextPart>()
    
    var lastIndex = 0
    imagePattern.findAll(text).forEach { match ->
        // Add text before image
        if (match.range.first > lastIndex) {
            parts.add(TextPart.Text(text.substring(lastIndex, match.range.first)))
        }
        // Add image
        val fileName = match.groupValues[1]
        parts.add(TextPart.Image(fileName))
        lastIndex = match.range.last + 1
    }
    // Add remaining text
    if (lastIndex < text.length) {
        parts.add(TextPart.Text(text.substring(lastIndex)))
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        if (parts.isEmpty() || (parts.size == 1 && parts[0] is TextPart.Text && (parts[0] as TextPart.Text).content.isEmpty())) {
            // Show placeholder when empty
            BasicTextField(
                value = textFieldValue,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { onFocusChanged(it.isFocused) },
                textStyle = textStyle,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                decorationBox = { innerTextField ->
                    if (textFieldValue.text.isEmpty()) {
                        Text(
                            "Start writing...", 
                            style = MaterialTheme.typography.bodyLarge, 
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                    innerTextField()
                },
            )
        } else {
            // For mixed content, show as read-only text with images for now
            // This is a simplified approach - full editing of mixed content is complex
            parts.forEach { part ->
                when (part) {
                    is TextPart.Text -> {
                        if (part.content.isNotEmpty()) {
                            Text(
                                text = part.content,
                                style = textStyle,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                            )
                        }
                    }
                    is TextPart.Image -> {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(resolvePath(part.fileName))
                                .crossfade(true)
                                .build(),
                            contentDescription = "Inline image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .padding(vertical = 4.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}

private sealed class TextPart {
    data class Text(val content: String) : TextPart()
    data class Image(val fileName: String) : TextPart()
}
