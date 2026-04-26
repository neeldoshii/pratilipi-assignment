package com.example.rich_text_editor.ui.editor

import android.net.Uri
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rich_text_editor.data.files.ImageFileStore
import com.example.rich_text_editor.data.local.PostEntity
import com.example.rich_text_editor.data.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.example.rich_text_editor.editor.DocumentJsonCodec
import com.example.rich_text_editor.editor.EditorBlock
import com.example.rich_text_editor.editor.EditorBlocksMapper
import com.example.rich_text_editor.editor.ListItemRow
import com.example.rich_text_editor.editor.SpanMutation
import com.example.rich_text_editor.editor.model.HeadingLevel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@HiltViewModel
class EditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: PostRepository,
    private val imageFileStore: ImageFileStore,
) : ViewModel() {

    private val postId: String = checkNotNull(savedStateHandle["postId"])

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _blocks = MutableStateFlow<List<EditorBlock>>(emptyList())
    val blocks: StateFlow<List<EditorBlock>> = _blocks.asStateFlow()

    private val _focus = MutableStateFlow<EditorFocus?>(null)
    val focus: StateFlow<EditorFocus?> = _focus.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // Track current typing formatting state
    private var currentTypingFormat = SpanMutation.FormattingState()
    
    private val _activeFormats = MutableStateFlow(SpanMutation.FormattingState())
    val activeFormats: StateFlow<SpanMutation.FormattingState> = _activeFormats.asStateFlow()

    private var saveJob: Job? = null
    private var _hasUnsavedChangesFlag = false
    private val _hasUnsavedChanges = MutableStateFlow(false)
    val hasUnsavedChanges: StateFlow<Boolean> = _hasUnsavedChanges.asStateFlow()

    init {
        _blocks.onEach { blocks ->
            Log.d("EditorViewModel", "Blocks changed: ${blocks.size} blocks, content: ${blocks.joinToString { it.javaClass.simpleName }}")
        }.launchIn(viewModelScope)
        
        viewModelScope.launch {
            val post = repository.getPost(postId)
            if (post != null) {
                applyPost(post)
            } else {
                _blocks.value = listOf(EditorBlock.Text())
            }
            _isLoading.value = false
        }
    }

    private fun applyPost(post: PostEntity) {
        _title.value = post.title
        val doc = DocumentJsonCodec.decode(post.contentJson)
        _blocks.value = EditorBlocksMapper.fromDocument(doc)
    }

    fun onTitleChange(value: String) {
        _title.value = value
        _hasUnsavedChangesFlag = true
        _hasUnsavedChanges.value = true
        // scheduleSave()
    }

    fun onFocusChange(focus: EditorFocus?) {
        _focus.value = focus
        // Reset typing format when focus changes
        currentTypingFormat = SpanMutation.FormattingState()
        updateActiveFormats()
    }

    fun onTextBlockChange(index: Int, newValue: TextFieldValue) {
        val oldBlock = getCurrentBlock(index)
        val oldValue = oldBlock?.value ?: TextFieldValue()
        
        
        _blocks.update { list ->
            list.mapIndexed { i, block ->
                if (i == index && block is EditorBlock.Text) {
                    val finalValue = when {
                        // Same text = cursor/selection change only
                        oldValue.text == newValue.text -> {
                            if (oldValue.annotatedString.spanStyles.isNotEmpty() && newValue.annotatedString.spanStyles.isEmpty()) {
                                oldValue.copy(selection = newValue.selection, composition = newValue.composition)
                            } else {
                                newValue
                            }
                        }
                        // Text added = apply formatting to new chars
                        newValue.text.length > oldValue.text.length -> {
                            applyFormattingToNewText(oldValue, newValue)
                        }
                        // Text removed or other change
                        else -> newValue
                    }
                    
                    // Reset typing format on cursor-only moves
                    if (oldValue.text == newValue.text && oldValue.selection != newValue.selection) {
                        currentTypingFormat = SpanMutation.FormattingState()
                        updateActiveFormats()
                    }
                    
                    block.copy(value = finalValue)
                } else {
                    block
                }
            }
        }
        _hasUnsavedChangesFlag = true
        _hasUnsavedChanges.value = true
        // scheduleSave()
    }
    
    private fun applyFormattingToNewText(oldValue: TextFieldValue, newValue: TextFieldValue): TextFieldValue {
        val insertPos = oldValue.selection.start
        val newChars = newValue.text.length - oldValue.text.length
        
        // Step 1: Copy old spans to new text
        val builder = androidx.compose.ui.text.AnnotatedString.Builder()
        builder.append(newValue.text)
        
        // Copy existing spans, adjusting positions after insertion point
        oldValue.annotatedString.spanStyles.forEach { range ->
            val adjustedStart = if (range.start <= insertPos) range.start else range.start + newChars
            val adjustedEnd = if (range.end <= insertPos) range.end else range.end + newChars
            builder.addStyle(range.item, adjustedStart, adjustedEnd)
        }
        
        // Step 2: Apply formatting to new characters
        val formatState = if (hasActiveTypingFormat()) {
            currentTypingFormat
        } else {
            SpanMutation.getFormattingAtPosition(oldValue, insertPos)
        }
        
        // Apply spans for each formatting type
        if (formatState.bold) {
            builder.addStyle(androidx.compose.ui.text.SpanStyle(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold), insertPos, insertPos + newChars)
        }
        if (formatState.italic) {
            builder.addStyle(androidx.compose.ui.text.SpanStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic), insertPos, insertPos + newChars)
        }
        if (formatState.underline) {
            builder.addStyle(androidx.compose.ui.text.SpanStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline), insertPos, insertPos + newChars)
        }
        if (formatState.strikethrough) {
            builder.addStyle(androidx.compose.ui.text.SpanStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough), insertPos, insertPos + newChars)
        }
        formatState.foregroundColor?.let { color ->
            builder.addStyle(androidx.compose.ui.text.SpanStyle(color = androidx.compose.ui.graphics.Color((color and 0xFFFFFFFFL).toInt())), insertPos, insertPos + newChars)
        }
        formatState.highlightColor?.let { color ->
            builder.addStyle(androidx.compose.ui.text.SpanStyle(background = androidx.compose.ui.graphics.Color((color and 0xFFFFFFFFL).toInt())), insertPos, insertPos + newChars)
        }
        
        return newValue.copy(annotatedString = builder.toAnnotatedString())
    }
    
    private fun getCurrentBlock(index: Int): EditorBlock.Text? {
        return _blocks.value.getOrNull(index) as? EditorBlock.Text
    }

    
    private fun hasActiveTypingFormat(): Boolean {
        return currentTypingFormat.bold || currentTypingFormat.italic || 
               currentTypingFormat.underline || currentTypingFormat.strikethrough ||
               currentTypingFormat.foregroundColor != null || currentTypingFormat.highlightColor != null ||
               currentTypingFormat.fontSize != null
    }

    fun onListItemChange(blockIndex: Int, itemIndex: Int, newValue: TextFieldValue) {
        _blocks.update { list ->
            list.mapIndexed { bi, block ->
                if (bi == blockIndex && block is EditorBlock.List) {
                    block.copy(
                        items = block.items.mapIndexed { ii: Int, row: ListItemRow ->
                            if (ii == itemIndex) {
                                val oldValue = row.value
                                val finalValue = if (oldValue.text == newValue.text && 
                                                   oldValue.annotatedString.spanStyles.isNotEmpty() && 
                                                   newValue.annotatedString.spanStyles.isEmpty()) {
                                    oldValue.copy(selection = newValue.selection, composition = newValue.composition)
                                } else {
                                    newValue
                                }
                                row.copy(value = finalValue)
                            } else {
                                row
                            }
                        },
                    )
                } else {
                    block
                }
            }
        }
        _hasUnsavedChangesFlag = true
        _hasUnsavedChanges.value = true
        // scheduleSave()
    }

    fun setHeading(blockIndex: Int, heading: HeadingLevel) {
        _blocks.update { list ->
            list.mapIndexed { i, block ->
                if (i == blockIndex && block is EditorBlock.Text) {
                    block.copy(heading = heading)
                } else {
                    block
                }
            }
        }
        _hasUnsavedChangesFlag = true
        _hasUnsavedChanges.value = true
        // scheduleSave()
    }

    fun addTextBlock() {
        _blocks.update { it + EditorBlock.Text() }
        scheduleSave()
    }

    fun addListBlock(ordered: Boolean) {
        _blocks.update {
            it + EditorBlock.List(ordered = ordered, items = listOf(ListItemRow()))
        }
        _hasUnsavedChangesFlag = true
        _hasUnsavedChanges.value = true
        // scheduleSave()
    }

    fun addListItem(blockIndex: Int) {
        _blocks.update { list ->
            list.mapIndexed { i, block ->
                if (i == blockIndex && block is EditorBlock.List) {
                    block.copy(items = block.items + ListItemRow())
                } else {
                    block
                }
            }
        }
        _hasUnsavedChangesFlag = true
        _hasUnsavedChanges.value = true
        // scheduleSave()
    }

    fun removeBlock(index: Int) {
        _blocks.update { list ->
            if (list.size <= 1) list else list.filterIndexed { i, _ -> i != index }
        }
        _hasUnsavedChangesFlag = true
        _hasUnsavedChanges.value = true
        // scheduleSave()
    }

    fun setImageWidth(blockIndex: Int, widthDp: Int) {
        _blocks.update { list ->
            list.mapIndexed { i, block ->
                if (i == blockIndex && block is EditorBlock.Image) {
                    block.copy(widthDp = widthDp.coerceIn(80, 480))
                } else {
                    block
                }
            }
        }
        _hasUnsavedChangesFlag = true
        _hasUnsavedChanges.value = true
        // scheduleSave()
    }

    fun insertImage(fileName: String) {
        val imagePlaceholder = "[img:$fileName]"
        when (val f = _focus.value) {
            is EditorFocus.Text -> {
                _blocks.update { list ->
                    list.mapIndexed { index, block ->
                        if (index == f.index && block is EditorBlock.Text) {
                            val currentText = block.value.text
                            val cursorPosition = block.value.selection.start
                            val newText = currentText.substring(0, cursorPosition) + 
                                         imagePlaceholder + 
                                         currentText.substring(cursorPosition)
                            block.copy(
                                value = block.value.copy(
                                    text = newText,
                                    selection = androidx.compose.ui.text.TextRange(cursorPosition + imagePlaceholder.length)
                                )
                            )
                        } else block
                    }
                }
            }
            is EditorFocus.ListItem -> {
                _blocks.update { list ->
                    list.mapIndexed { index, block ->
                        if (index == f.blockIndex && block is EditorBlock.List) {
                            val updatedItems = block.items.mapIndexed { itemIndex, item ->
                                if (itemIndex == f.itemIndex) {
                                    val currentText = item.value.text
                                    val cursorPosition = item.value.selection.start
                                    val newText = currentText.substring(0, cursorPosition) + 
                                                 imagePlaceholder + 
                                                 currentText.substring(cursorPosition)
                                    item.copy(
                                        value = item.value.copy(
                                            text = newText,
                                            selection = androidx.compose.ui.text.TextRange(cursorPosition + imagePlaceholder.length)
                                        )
                                    )
                                } else item
                            }
                            block.copy(items = updatedItems)
                        } else block
                    }
                }
            }
            null -> {
                // No focus, add to end of last text block or create new one
                _blocks.update { list ->
                    if (list.isNotEmpty() && list.last() is EditorBlock.Text) {
                        val lastBlock = list.last() as EditorBlock.Text
                        val newText = lastBlock.value.text + imagePlaceholder
                        list.dropLast(1) + lastBlock.copy(
                            value = lastBlock.value.copy(
                                text = newText,
                                selection = androidx.compose.ui.text.TextRange(newText.length)
                            )
                        )
                    } else {
                        list + EditorBlock.Text(value = androidx.compose.ui.text.input.TextFieldValue(imagePlaceholder))
                    }
                }
            }
        }
        _hasUnsavedChangesFlag = true
        _hasUnsavedChanges.value = true
        // scheduleSave()
    }

    fun importImageFromUri(uri: Uri) {
        viewModelScope.launch {
            val name = imageFileStore.importFromUri(uri)
            insertImage(name)
        }
    }

    fun resolveImagePath(name: String) = imageFileStore.resolveFile(name)

    fun toggleBold() {
        val focus = _focus.value
        val hasSelection = when (focus) {
            is EditorFocus.Text -> textValueAt(focus.index).selection.length > 0
            is EditorFocus.ListItem -> listItemValueAt(focus.blockIndex, focus.itemIndex).selection.length > 0
            else -> false
        }
        
        if (hasSelection) {
            // Apply to selected text, don't keep button active
            mutateFocused { SpanMutation.toggleBold(it) }
            _activeFormats.value = _activeFormats.value.copy(bold = false)
            currentTypingFormat = currentTypingFormat.copy(bold = false)
        } else {
            // No selection, toggle for future typing
            val currentActive = _activeFormats.value.bold
            val newBold = !currentActive
            updateTypingFormat { it.copy(bold = newBold) }
            _activeFormats.value = _activeFormats.value.copy(bold = newBold)
        }
    }
    
    fun toggleItalic() {
        val focus = _focus.value
        val hasSelection = when (focus) {
            is EditorFocus.Text -> textValueAt(focus.index).selection.length > 0
            is EditorFocus.ListItem -> listItemValueAt(focus.blockIndex, focus.itemIndex).selection.length > 0
            else -> false
        }
        
        if (hasSelection) {
            mutateFocused { SpanMutation.toggleItalic(it) }
            _activeFormats.value = _activeFormats.value.copy(italic = false)
            currentTypingFormat = currentTypingFormat.copy(italic = false)
        } else {
            val currentActive = _activeFormats.value.italic
            val newItalic = !currentActive
            updateTypingFormat { it.copy(italic = newItalic) }
            _activeFormats.value = _activeFormats.value.copy(italic = newItalic)
        }
    }
    
    fun toggleUnderline() {
        val focus = _focus.value
        val hasSelection = when (focus) {
            is EditorFocus.Text -> textValueAt(focus.index).selection.length > 0
            is EditorFocus.ListItem -> listItemValueAt(focus.blockIndex, focus.itemIndex).selection.length > 0
            else -> false
        }
        
        if (hasSelection) {
            mutateFocused { SpanMutation.toggleUnderline(it) }
            _activeFormats.value = _activeFormats.value.copy(underline = false)
            currentTypingFormat = currentTypingFormat.copy(underline = false)
        } else {
            val currentActive = _activeFormats.value.underline
            val newUnderline = !currentActive
            updateTypingFormat { it.copy(underline = newUnderline) }
            _activeFormats.value = _activeFormats.value.copy(underline = newUnderline)
        }
    }
    
    fun toggleStrikethrough() {
        val focus = _focus.value
        val hasSelection = when (focus) {
            is EditorFocus.Text -> textValueAt(focus.index).selection.length > 0
            is EditorFocus.ListItem -> listItemValueAt(focus.blockIndex, focus.itemIndex).selection.length > 0
            else -> false
        }
        
        if (hasSelection) {
            mutateFocused { SpanMutation.toggleStrikethrough(it) }
            _activeFormats.value = _activeFormats.value.copy(strikethrough = false)
            currentTypingFormat = currentTypingFormat.copy(strikethrough = false)
        } else {
            val currentActive = _activeFormats.value.strikethrough
            val newStrikethrough = !currentActive
            updateTypingFormat { it.copy(strikethrough = newStrikethrough) }
            _activeFormats.value = _activeFormats.value.copy(strikethrough = newStrikethrough)
        }
    }
    fun applyForeground(color: Color) {
        updateTypingFormat { it.copy(foregroundColor = color.toArgb().toUInt().toLong()) }
        mutateFocused { SpanMutation.applyForegroundColor(it, color) }
    }

    fun applyHighlight(color: Color) {
        updateTypingFormat { it.copy(highlightColor = color.toArgb().toUInt().toLong()) }
        mutateFocused { SpanMutation.applyHighlightColor(it, color) }
    }

    fun applyHeading1() {
        updateTypingFormat { it.copy(fontSize = 28, bold = true) }
        mutateFocused { SpanMutation.applyHeading1(it) }
    }
    
    fun applyHeading2() {
        updateTypingFormat { it.copy(fontSize = 22, bold = true) }
        mutateFocused { SpanMutation.applyHeading2(it) }
    }
    
    fun applyHeading3() {
        updateTypingFormat { it.copy(fontSize = 18, bold = true) }
        mutateFocused { SpanMutation.applyHeading3(it) }
    }
    
    fun applyBodyText() {
        updateTypingFormat { it.copy(fontSize = null, bold = false) }
        mutateFocused { SpanMutation.applyBodyText(it) }
    }
    
    private fun updateTypingFormat(update: (SpanMutation.FormattingState) -> SpanMutation.FormattingState) {
        currentTypingFormat = update(currentTypingFormat)
    }
    
    private fun updateActiveFormats() {
        val focus = _focus.value
        val activeFormat = when {
            hasActiveTypingFormat() -> currentTypingFormat
            focus is EditorFocus.Text -> {
                val block = getCurrentBlock(focus.index)
                val cursorPos = block?.value?.selection?.start ?: 0
                block?.value?.let { SpanMutation.getFormattingAtPosition(it, cursorPos) } ?: SpanMutation.FormattingState()
            }
            focus is EditorFocus.ListItem -> {
                val block = getCurrentListBlock(focus.blockIndex)
                val item = block?.items?.getOrNull(focus.itemIndex)
                val cursorPos = item?.value?.selection?.start ?: 0
                item?.value?.let { SpanMutation.getFormattingAtPosition(it, cursorPos) } ?: SpanMutation.FormattingState()
            }
            else -> SpanMutation.FormattingState()
        }
        _activeFormats.value = activeFormat
    }
    
    private fun getCurrentListBlock(index: Int): EditorBlock.List? {
        return _blocks.value.getOrNull(index) as? EditorBlock.List
    }

    private fun mutateFocused(transform: (TextFieldValue) -> TextFieldValue) {
        when (val f = _focus.value) {
            is EditorFocus.Text -> onTextBlockChange(f.index, transform(textValueAt(f.index)))
            is EditorFocus.ListItem -> onListItemChange(
                f.blockIndex,
                f.itemIndex,
                transform(listItemValueAt(f.blockIndex, f.itemIndex)),
            )
            null -> Unit
        }
    }

    private fun textValueAt(index: Int): TextFieldValue =
        (_blocks.value.getOrNull(index) as? EditorBlock.Text)?.value ?: TextFieldValue()

    private fun listItemValueAt(blockIndex: Int, itemIndex: Int): TextFieldValue {
        val block = _blocks.value.getOrNull(blockIndex) as? EditorBlock.List ?: return TextFieldValue()
        return block.items.getOrNull(itemIndex)?.value ?: TextFieldValue()
    }

    private fun scheduleSave() {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(800)
            persist()
        }
    }

    fun saveManually() {
        viewModelScope.launch {
            persist()
        }
    }

    private suspend fun persist() {
        if (!_hasUnsavedChangesFlag) return
        
        val json = DocumentJsonCodec.encode(EditorBlocksMapper.toDocument(_blocks.value))
        repository.upsertPost(
            PostEntity(
                id = postId,
                title = _title.value,
                contentJson = json,
                updatedAt = System.currentTimeMillis(),
            ),
        )
        _hasUnsavedChangesFlag = false
        _hasUnsavedChanges.value = false
    }

    override fun onCleared() {
        saveJob?.cancel()
        runBlocking { persist() }
        super.onCleared()
    }
}
