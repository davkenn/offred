package com.example.renewed.Room

import androidx.room.*
import com.example.renewed.models.RoomT5
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single


@Dao
interface T5DAO {

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        fun insertAll(t5s: List<RoomT5>): Completable

        @Query("SELECT COUNT(*) FROM RoomT5 WHERE isSaved=0 AND isDisplayed=0")
        fun howManySubredditsInDb(): Single<Long>

        @Query("DELETE FROM RoomT5 WHERE name = :name")
        fun delete(name: String): Completable

        @Query("DELETE FROM RoomT5 WHERE isSaved=0 AND isDisplayed=0 AND totalViews >= :maxViews")
        fun deleteUnwanted(maxViews: Int): Completable

        //TODO here I am cutting off before deleting, maybe should just remove totalviews
        @Query("SELECT * FROM RoomT5 WHERE isSaved= 0 AND totalViews < 3 "+
                "ORDER BY displayName <= :startReturningAfter,  displayName LIMIT 20")
        fun getSubredditsFromTable(startReturningAfter:String): Single<List<RoomT5>>

        @Query("SELECT * FROM RoomT5 WHERE isSaved= 1 AND totalViews < 3 "+
                "ORDER BY totalViews LIMIT :returnCount")
        fun getNextFavoriteSubreddits(returnCount:Int): Single<List<RoomT5>>


        @Query("SELECT * FROM RoomT5 WHERE RoomT5.name LIKE :name")
        fun getSubreddit(name: String) : Single<RoomT5>

        //get subreddits yet to have any posts loaded for it
        @Query("SELECT roomT5.displayName FROM roomT5  LEFT JOIN roomT3  ON roomT5.name = subredditId  " +
                "WHERE subredditId IS NULL LIMIT 80")   //this num must be same as num loaded
        fun getSubredditIDsNeedingPosts() : Single<List<String>>

        @Insert(onConflict = OnConflictStrategy.IGNORE)
        fun insertT5(t5: RoomT5): Completable

        @Update(onConflict = OnConflictStrategy.REPLACE)
        fun updateT5(t5: RoomT5): Completable

        @Query("UPDATE RoomT5 SET isSaved=1 WHERE RoomT5.name LIKE :name")
        fun saveSubreddit(name: String): Completable

        @Query("UPDATE RoomT5 SET isDisplayed=0")
        fun clearDisplayed() : Completable


}