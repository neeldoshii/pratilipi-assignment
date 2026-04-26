package com.example.rich_text_editor.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDao {
    @Query("SELECT * FROM posts ORDER BY updatedAt DESC")
    fun observePosts(): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE id = :id LIMIT 1")
    suspend fun getPost(id: String): PostEntity?

    @Query("SELECT COUNT(*) FROM posts")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(post: PostEntity)

    @Query("DELETE FROM posts WHERE id = :id")
    suspend fun delete(id: String)
}
