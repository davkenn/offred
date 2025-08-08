package com.example.offred.Room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.offred.models.CurrentFavoritesList
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable

@Dao
interface FavoritesDAO {


   @Query("SELECT COUNT(*) FROM CurrentFavoritesList")
   fun currentSize(): Observable<Int>
   @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(elements: List<CurrentFavoritesList>): Completable
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(elem: CurrentFavoritesList): Completable

    @Query("SELECT postId FROM CurrentFavoritesList order by displayOrder ASC")
    fun getPosts(): Observable<List<String>>

    @Query("DELETE FROM CurrentFavoritesList ")
    fun clearDb(): Completable

    @Query("DELETE FROM CurrentFavoritesList where postId in (:idList) ")
    fun deleteList(idList: List<String>): Completable
}
