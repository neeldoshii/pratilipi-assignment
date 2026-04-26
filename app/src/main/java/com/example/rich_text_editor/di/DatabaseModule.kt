package com.example.rich_text_editor.di

import android.content.Context
import com.example.rich_text_editor.data.files.ImageFileStore
import com.example.rich_text_editor.data.local.PostDao
import com.example.rich_text_editor.data.local.RichEditorDatabase
import com.example.rich_text_editor.data.repository.DefaultPostRepository
import com.example.rich_text_editor.data.repository.PostRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): RichEditorDatabase {
        return RichEditorDatabase.build(context)
    }

    @Provides
    fun providePostDao(database: RichEditorDatabase): PostDao {
        return database.postDao()
    }

    @Provides
    @Singleton
    fun providePostRepository(
        dao: PostDao,
        scope: CoroutineScope
    ): PostRepository {
        val repository = DefaultPostRepository(dao)
        scope.launch(Dispatchers.IO) {
            repository.seedIfEmpty()
        }
        return repository
    }

    @Provides
    @Singleton
    fun provideImageFileStore(@ApplicationContext context: Context): ImageFileStore {
        return ImageFileStore(context)
    }
}