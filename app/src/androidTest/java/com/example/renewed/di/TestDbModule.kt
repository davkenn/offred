package com.example.renewed.di

import android.content.Context
import androidx.room.Room
import com.example.renewed.Room.RedditDatabase
import com.example.renewed.Room.T3DAO
import com.example.renewed.Room.T5DAO
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton
/**
    @Module
    @TestInstallIn(
        components = [SingletonComponent::class],
        replaces = [DbModule::class]
    )

    class TestRepoModule  {



        //TODO this DB is fucked because it was using old code to parse make new db
            @Provides
            @Singleton
            fun provideDB(@ApplicationContext ctxt: Context) : RedditDatabase {
              //  var db = ctxt.getDatabasePath("RedditDB1")
                //if (db.exists()) db.delete()
                return Room.databaseBuilder(
                    ctxt,
                    RedditDatabase::class.java,
                    "RedditDB1"
                ).createFromAsset("RedditDBTest")
                    .build()
            }

            @Provides
            @Singleton
            fun provideT5DAO(db: RedditDatabase) : T5DAO = db.subredditDao()

            @Provides
            @Singleton
            fun provideT3DAO(db: RedditDatabase) : T3DAO = db.postsDao()


    }

**/
