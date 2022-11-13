package com.example.renewed.di

import android.content.Context
import androidx.room.Room
import com.example.renewed.Room.FavoritesDAO
import com.example.renewed.Room.RedditDatabase
import com.example.renewed.Room.T3DAO
import com.example.renewed.Room.T5DAO
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
    fun provideDB(@ApplicationContext ctxt: Context) : RedditDatabase {
        return Room.databaseBuilder(
            ctxt,
            RedditDatabase::class.java,
            "RedditDBProd"
        )//.createFromAsset("RedditDBProduction")
            .build()
    }

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