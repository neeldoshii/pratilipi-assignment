package com.example.rich_text_editor.ui.posts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rich_text_editor.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostListScreen(
    viewModel: PostsViewModel,
    onOpenPost: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val posts by viewModel.posts.collectAsStateWithLifecycle()
    val showDialog by viewModel.isCreateDialogVisible.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    
    var postToDelete by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.post_list_title)) },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.showCreateDialog() },
                icon = { Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.create_document_fab_content_description)) },
                text = { Text(stringResource(R.string.create_document_fab_label)) },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            items(posts, key = { it.id }) { post ->
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = { dismissValue ->
                        when (dismissValue) {
                            SwipeToDismissBoxValue.EndToStart -> {
                                postToDelete = post.id
                                false // Don't dismiss yet, wait for confirmation
                            }
                            SwipeToDismissBoxValue.StartToEnd -> {
                                onOpenPost(post.id)
                                false
                            }
                            else -> false
                        }
                    }
                )

                SwipeToDismissBox(
                    state = dismissState,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    backgroundContent = {
                        val color = when (dismissState.targetValue) {
                            SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.error
                            SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.surface
                        }
                        val icon = when (dismissState.targetValue) {
                            SwipeToDismissBoxValue.EndToStart -> Icons.Default.Delete
                            SwipeToDismissBoxValue.StartToEnd -> Icons.Default.Edit
                            else -> null
                        }
                        val alignment = when (dismissState.targetValue) {
                            SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                            SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                            else -> Alignment.Center
                        }
                        
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(color)
                                .padding(horizontal = 20.dp),
                            contentAlignment = alignment
                        ) {
                            icon?.let {
                                Icon(
                                    imageVector = it,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    },
                    content = {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenPost(post.id) },
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(
                                    text = post.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                DocumentRichPreview(
                                    contentJson = post.contentJson,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 6.dp),
                                )
                            }
                        }
                    }
                )
            }
        }
    }

    if (showDialog) {
        CreateDocumentTitleDialog(
            onDismiss = { viewModel.dismissCreateDialog() },
            onConfirm = { title ->
                scope.launch {
                    val id = viewModel.createDocument(title)
                    onOpenPost(id)
                }
            },
        )
    }

    postToDelete?.let { postId ->
        val postTitle = posts.find { it.id == postId }?.title ?: "Note"
        AlertDialog(
            onDismissRequest = { postToDelete = null },
            title = { Text("Delete \"$postTitle\"") },
            text = { 
                Text("Are you sure you want to permanently delete this note? This action cannot be undone and all your content will be lost forever.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            viewModel.deletePost(postId)
                            postToDelete = null
                        }
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { postToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
