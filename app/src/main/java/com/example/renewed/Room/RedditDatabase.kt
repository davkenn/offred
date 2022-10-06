package com.example.renewed.Room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.renewed.models.RoomT3
import com.example.renewed.models.RoomT5
import com.example.renewed.models.RoomT5BASE


//TODO im using the subclass here
    @Database(entities =[RoomT5::class, RoomT3::class, RoomT5BASE::class], version = 1,exportSchema = false)
    @TypeConverters(Converters::class)
    abstract class RedditDatabase : RoomDatabase() {

        abstract fun subredditDao(): T5DAO
        abstract fun savedSubredditsDao(): SavedSubredditsDAO
        abstract fun postsDao(): T3DAO

    }




