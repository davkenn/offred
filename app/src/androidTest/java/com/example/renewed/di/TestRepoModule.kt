package com.example.renewed.di

import com.example.renewed.API
import com.example.renewed.BaseSubredditsAndPostsRepo
import com.example.renewed.Room.SavedSubredditsDAO
import com.example.renewed.Room.T3DAO
import com.example.renewed.Room.T5DAO
import com.example.renewed.SubredditsAndPostsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

abstract class TestRepoModule {
    @Module
    @TestInstallIn(
        components = [SingletonComponent::class],
        replaces = [RepoModule::class]
    )
    object TestTasksRepositoryModule {

        @Singleton
        @Provides
        fun providePostsRepository(api: API, t5Dao: T5DAO, t3Dao: T3DAO,savedDao:SavedSubredditsDAO) :
                BaseSubredditsAndPostsRepo = SubredditsAndPostsRepository(api, t5Dao, t3Dao)

    }
}


