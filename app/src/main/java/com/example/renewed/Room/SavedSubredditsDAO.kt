package com.example.renewed.Room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.example.renewed.models.RoomT5
import com.example.renewed.models.RoomT5BASE
import io.reactivex.rxjava3.core.Completable

@Dao
interface SavedSubredditsDAO {


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun saveSubreddit(t5: RoomT5BASE): Completable

}