package com.example.renewed.di

import android.content.Context
import androidx.room.Room
import com.example.renewed.API
import com.example.renewed.BaseSubredditsAndPostsRepo
import com.example.renewed.Room.RedditDatabase
import com.example.renewed.Room.T3DAO
import com.example.renewed.Room.T5DAO
import com.example.renewed.SubredditsAndPostsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

    @Module
    @TestInstallIn(
        components = [SingletonComponent::class],
        replaces = [DbModule::class]
    )

    class TestRepoModule  {


            @Provides
            @Singleton
            fun provideDB(@ApplicationContext ctxt: Context) : RedditDatabase {
                return Room.databaseBuilder(
                    ctxt,
                    RedditDatabase::class.java,
                    "RedditDBTest"
                ).createFromAsset("RedditDB")
                    .build()
            }

            @Provides
            @Singleton
            fun provideT5DAO(db: RedditDatabase) : T5DAO = db.subredditDao()

            @Provides
            @Singleton
            fun provideT3DAO(db: RedditDatabase) : T3DAO = db.postsDao()

            /**
    object TestTasksRepositoryModule {

        @Singleton
        @Provides
        fun providePostsRepository(api: API, t5Dao: T5DAO, t3Dao: T3DAO) :
                BaseSubredditsAndPostsRepo = SubredditsAndPostsRepository(api, t5Dao, t3Dao)

    }**/
}


