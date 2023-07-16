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

    @Query("SELECT * FROM RoomT3 WHERE RoomT3.subredditId like :name LIMIT 10")
    fun getPosts(name: String): Single<List<RoomT3>>

    @Query("SELECT * FROM RoomT3 LIMIT 10")
    fun getPosts2(): Single<List<RoomT3>>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateT3(t5: RoomT5): Completable

    //FOR UI TESTING
    @Query("SELECT * FROM RoomT3")
    fun getAllRows(): List<RoomT3>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun fillDb(t5s: List<RoomT3>): Unit

}
