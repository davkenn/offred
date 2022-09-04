package com.example.renewed.Room

import androidx.room.*
import com.example.renewed.models.RoomT3
import com.example.renewed.models.RoomT5
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

@Dao
interface T3DAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(t3s: List<RoomT3>): Completable

    //TODO should this return maybe?
    @Query("SELECT * FROM RoomT3 WHERE RoomT3.name like :name")
    fun getPost(name: String): Single<RoomT3>

    @Query("SELECT * FROM RoomT3 WHERE RoomT3.subredditId like :name LIMIT 30")
    fun getPosts(name: String): Single<List<RoomT3>>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateT3(t5: RoomT5): Completable

    //FROM WHEN FLOWABLE ? or does it do this with single?
    // @Query("SELECT * FROM RoomT5 WHERE isSaved=0 ORDER BY RANDOM() LIMIT 50")

}
