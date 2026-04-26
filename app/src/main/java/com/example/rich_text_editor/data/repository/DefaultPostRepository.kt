package com.example.rich_text_editor.data.repository

import com.example.rich_text_editor.data.local.PostDao
import com.example.rich_text_editor.data.local.PostEntity
import com.example.rich_text_editor.editor.SampleDocuments
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class DefaultPostRepository(
    private val dao: PostDao,
) : PostRepository {

    override fun observePosts(): Flow<List<PostEntity>> = dao.observePosts()

    override suspend fun getPost(id: String): PostEntity? = dao.getPost(id)

    override suspend fun insertPost(title: String, contentJson: String): String {
        val id = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        dao.upsert(PostEntity(id = id, title = title, contentJson = contentJson, updatedAt = now))
        return id
    }

    override suspend fun upsertPost(post: PostEntity) {
        dao.upsert(post.copy(updatedAt = System.currentTimeMillis()))
    }

    override suspend fun deletePost(id: String) {
        dao.delete(id)
    }

    override suspend fun seedIfEmpty() {
        if (dao.count() > 0) return
        val id = UUID.randomUUID().toString()
        dao.upsert(
            PostEntity(
                id = id,
                title = SampleDocuments.SEED_POST_TITLE,
                contentJson = SampleDocuments.SAMPLE_CONTENT_JSON,
                updatedAt = System.currentTimeMillis(),
            ),
        )
    }
}
