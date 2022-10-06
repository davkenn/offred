package com.example.renewed.Room

import androidx.room.*
import com.example.renewed.models.RoomT5
import io.reactivex.rxjava3.core.*
import java.time.Instant


@Dao
interface T5DAO {

        @Query("SELECT COUNT(*) FROM RoomT5 WHERE isSaved=0 and isDisplayed=0")
        fun howManySubredditsInDb(): Single<Long>

        @Query("DELETE FROM RoomT5 WHERE name = :name")
        fun delete(name: String): Completable

        @Query("DELETE FROM RoomT5 WHERE isSaved=0 AND isDisplayed=0 AND totalViews >= :maxViews")
        fun deleteUnwanted(maxViews: Int): Completable

        //TODO is this good or bad to just centralize it like this
        @Query("SELECT * FROM RoomT5 WHERE isSaved=0 and totalViews < 3 "+
                "ORDER by displayName < :startReturningAfter,  displayName LIMIT 20")
        fun getSubredditsFromTable(startReturningAfter:String): Single<List<RoomT5>>

        @Query("SELECT * FROM RoomT5 WHERE RoomT5.name like :name")
        fun getSubreddit(name: String) : Single<RoomT5>

        //get subreddits yet to have any posts loaded for it
        @Query("SELECT roomT5.displayName FROM roomT5  LEFT JOIN roomT3  ON roomT5.name = subredditId  " +
                "WHERE subredditId IS NULL LIMIT 40")   //this num must be same as num loaded
        fun getSubredditIDsNeedingPosts() : Single<List<String>>

        @Insert(onConflict = OnConflictStrategy.IGNORE)
        fun insertT5(t5: RoomT5): Completable


        @Update(onConflict = OnConflictStrategy.REPLACE)
        fun updateT5(t5: RoomT5): Completable



}