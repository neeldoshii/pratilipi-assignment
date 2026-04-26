package com.example.rich_text_editor.ui.posts

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.rich_text_editor.R

@Composable
fun CreateDocumentTitleDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var title by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { onConfirm(title) },
                enabled = title.trim().isNotEmpty()
            ) {
                Text(text = stringResource(R.string.create_document_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.create_document_cancel))
            }
        },
        title = { Text(text = stringResource(R.string.create_document_title)) },
        text = {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                singleLine = true,
                label = { Text(stringResource(R.string.create_document_field_label)) },
                placeholder = { Text("Enter note title...") },
                isError = title.isNotEmpty() && title.trim().isEmpty(),
                supportingText = if (title.isNotEmpty() && title.trim().isEmpty()) {
                    { Text("Title cannot be empty") }
                } else null
            )
        },
        modifier = modifier,
    )
}
