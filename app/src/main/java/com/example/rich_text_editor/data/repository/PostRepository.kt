package com.example.rich_text_editor.data.repository

import com.example.rich_text_editor.data.local.PostEntity
import kotlinx.coroutines.flow.Flow

interface PostRepository {
    fun observePosts(): Flow<List<PostEntity>>
    suspend fun getPost(id: String): PostEntity?
    suspend fun insertPost(title: String, contentJson: String): String
    suspend fun upsertPost(post: PostEntity)
    suspend fun deletePost(id: String)
    suspend fun seedIfEmpty()
}
