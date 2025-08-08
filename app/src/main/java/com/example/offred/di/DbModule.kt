package com.example.offred.di

import android.content.Context
import androidx.room.Room
import com.example.offred.Room.FavoritesDAO
import com.example.offred.Room.RedditDatabase
import com.example.offred.Room.T3DAO
import com.example.offred.Room.T5DAO
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DbModule {

    @Provides
    @Singleton
    fun provideDB(@ApplicationContext ctxt: Context) : RedditDatabase =
        Room.databaseBuilder(ctxt, RedditDatabase::class.java, "RedditDB2")
            .fallbackToDestructiveMigration()
            .build()


    @Provides
    @Singleton
    fun provideT5DAO(db: RedditDatabase) : T5DAO = db.subredditDao()

    @Provides
    @Singleton
    fun provideT3DAO(db: RedditDatabase) : T3DAO = db.postsDao()

    @Provides
    @Singleton
    fun provideFavoritesDAO(db: RedditDatabase) : FavoritesDAO = db.favoritesDao()
}