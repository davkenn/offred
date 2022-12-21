package com.example.renewed.Room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.renewed.models.CurrentFavoritesList
import com.example.renewed.models.RoomT3
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

@Dao
interface FavoritesDAO {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(elem: CurrentFavoritesList): Completable

    @Query("SELECT postId FROM CurrentFavoritesList order by foodId ")
    fun getPosts(): Observable<List<String>>

    @Query("DELETE FROM CurrentFavoritesList ")
    fun clearDb(): Completable

    @Query("DELETE FROM CurrentFavoritesList where postId in (:idList) ")
    fun deleteList(idList: List<String>): Completable

    //   getPostsByWeekPeriod(weeksAgo:Int):Flowable
}