package com.example.renewed.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.Instant

@Entity
data class RoomT5(
    @PrimaryKey
    val name: String,
    val displayName: String,
    val description: String,
    val thumbnail: String,
    val banner_img: String,
    val timeLastAccessed: Instant,
    val subscribers: Int,
    val created_utc: Instant,
    val isSaved: Int = 0,
    val totalViews: Int = 0,
    //this will keep posts from being deleted if still active,
    //but not be counted in reloads of db. can mess with stuff if on current screen
    val isDisplayed: Int = 0
)

@Entity(foreignKeys = [ForeignKey(entity = RoomT3::class,
    parentColumns = arrayOf("name"),
    childColumns = arrayOf("link_id"),
    onDelete = ForeignKey.CASCADE,onUpdate = ForeignKey.CASCADE)])
data class RoomT1(
    @PrimaryKey
    val name: String,
    @ColumnInfo(index = true)
    val link_id: String,
    val body: String

    )

@Entity(foreignKeys = [ForeignKey(entity = RoomT5::class,
    parentColumns = arrayOf("name"),
    childColumns = arrayOf("subredditId"),
    onDelete = ForeignKey.CASCADE,onUpdate = ForeignKey.CASCADE)])
data class RoomT3(

    @PrimaryKey
    val name: String,
    @ColumnInfo(index = true)
    val subredditId: String,
    val created_utc: Instant,//todo fix
    val timeLastAccessed: Instant,
    val title: String,
    val selftext: String,
    val url: String,
    val permalink:String,
    val thumbnail: String)


   /** @Json(name = "author")

    val author: String,

    val author_fullname: String,

    val created: Int,


    val downs: Int,

    override val name: String,
    val num_cmments: Int,

    val over_18: Boolean,
    @Json(name = "permalink")
    val permalink: String,
    @Json(name = "score")
    val score: Int,
    @Json(name = "selftext")
    val selftext: String,
    @Json(name = "subreddit")
    val subreddit: String,
    @Json(name = "subreddit_id")
    val subredditId: String,
    @Json(name = "subreddit_name_prefixed")
    val subredditNamePrefixed: String,
    @Json(name = "subreddit_subscribers")
    val subredditSubscribers: Int,
    @Json(name = "subreddit_type")
    val subredditType: String,
    @Json(name = "suggested_sort")
    val suggestedSort: String,
    @Json(name = "thumbnail")
    val thumbnail: String,
    @Json(name = "title")
    val title: String,
    @Json(name = "total_awards_received")
    val totalAwardsReceived: Int,
    @Json(name = "ups")
    val ups: Int,
    @Json(name = "upvote_ratio")
    val upvoteRatio: Double,
    @Json(name = "url")
    val url: String,
    @Json(name = "url_overridden_by_dest")
    val urlOverriddenByDest: String

    @PrimaryKey
    val name: String,
    val displayName: String,
    val description: String,
    val thumbnail: String,
    val timeLastAccessed: Instant,
    val subscribers: Int,
    val created_utc: Instant,
    val isSaved: Int = 0

)
**/
