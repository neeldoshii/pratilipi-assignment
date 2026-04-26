package com.example.rich_text_editor.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey val id: String,
    val title: String,
    val contentJson: String,
    val updatedAt: Long,
)
