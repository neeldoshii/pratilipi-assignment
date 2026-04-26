package com.example.rich_text_editor.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [PostEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class RichEditorDatabase : RoomDatabase() {
    abstract fun postDao(): PostDao

    companion object {
        fun build(context: Context): RichEditorDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                RichEditorDatabase::class.java,
                "rich_editor.db",
            ).build()
    }
}
