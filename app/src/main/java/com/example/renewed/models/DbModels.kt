package com.example.renewed.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * The Reddit API defines different entity types, including T5 for subreddits, T3 for posts in those
 * subreddits, and T1 for comments replying to those posts. For every kind of entity that is defined
 * in the reddit json endpoints we have three different model classes: the network models, the
 * database models, and the view models. This way, what is parsed from network responses,  what is
 * stored in the database, and what is displayed in the UI can all vary independently. These models
 * represent what is stored in the database.
 */
@Entity
data class RoomT5(@PrimaryKey val name: String, val displayName: String, val description: String,
                  val thumbnail: String, val banner_img: String, val timeLastAccessed: Instant,
                  val subscribers: Int, val created_utc: Instant, val isSaved: Boolean = false,
                  val totalViews: Int = 0, val isDisplayed: Int = 0)

@Entity(foreignKeys = [ForeignKey(entity = RoomT3::class, parentColumns = arrayOf("name"),
                                  childColumns = arrayOf("link_id"), onDelete = ForeignKey.CASCADE,
                                                                    onUpdate = ForeignKey.CASCADE)])
data class RoomT1(@PrimaryKey val name: String, @ColumnInfo(index = true) val link_id: String,
                                                                                val body: String)

@Entity(foreignKeys = [ForeignKey(entity = RoomT5::class, parentColumns = arrayOf("name"),
                              childColumns = arrayOf("subredditId"), onDelete = ForeignKey.CASCADE,
                                                                    onUpdate = ForeignKey.CASCADE)])
data class RoomT3(@PrimaryKey val name: String, @ColumnInfo(index = true) val subredditId: String,
                  val created_utc: Instant, val timeLastAccessed: Instant, val title: String,
                  val selftext: String, val url: String, val permalink:String, val thumbnail: String,
                  val gallery_urls: String?,val isSaved:Boolean=false)

@Entity
data class CurrentFavoritesList( val postId: String){
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}