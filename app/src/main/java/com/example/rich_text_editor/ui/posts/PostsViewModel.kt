package com.example.rich_text_editor.ui.posts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rich_text_editor.data.local.PostEntity
import com.example.rich_text_editor.data.repository.PostRepository
import com.example.rich_text_editor.editor.DocumentJsonCodec
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class PostsViewModel @Inject constructor(
    private val repository: PostRepository,
) : ViewModel() {

    val posts: StateFlow<List<PostEntity>> =
        repository.observePosts()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _isCreateDialogVisible = MutableStateFlow(false)
    val isCreateDialogVisible: StateFlow<Boolean> = _isCreateDialogVisible.asStateFlow()

    fun showCreateDialog() {
        _isCreateDialogVisible.value = true
    }

    fun dismissCreateDialog() {
        _isCreateDialogVisible.value = false
    }

    suspend fun createDocument(title: String): String {
        val trimmed = title.trim()
        require(trimmed.isNotEmpty()) { "Title cannot be empty" }
        val id = repository.insertPost(trimmed, DocumentJsonCodec.emptyDocumentJson())
        _isCreateDialogVisible.value = false
        return id
    }

    suspend fun deletePost(postId: String) {
        repository.deletePost(postId)
    }
}
