package com.example.renewed.models

import com.example.renewed.moshiadapters.SubredditDescription
import com.squareup.moshi.JsonClass

import com.squareup.moshi.Json


//https://github.com/square/moshi/issues/813
@JsonClass(generateAdapter = true)
data class Holder(val data: RedditPostType, val kind: String)

sealed class RedditPostType {
    abstract val name:String
}

@JsonClass(generateAdapter = true)
data class RedditListing(val children: List<Holder>, val before: String?, val after: String?)

@JsonClass(generateAdapter = true)
data class Listing(  val data: RedditListing)





@JsonClass(generateAdapter = true)
data class T5 (

    override val name: String,
    val display_name: String,
    val icon_img: String?,
    val header_img: String?,
    val community_icon: String?,
    val banner_img: String?,
    val url: String,
    val subscribers: Int,
    val active_user_count: Int,
     @SubredditDescription
    val description: String,
    @SubredditDescription
    val public_description: String,
    val created_utc: Long

): RedditPostType()


@JsonClass(generateAdapter = true)
data class T3(

     override val name: String,
    val author: String,

    val ups: Int,
    val downs: Int,
    val num_comments: Int,
    val score: Int,
     //can be empty string
    val selftext: String,
    val subreddit: String,
    val subreddit_id: String,
    val subreddit_subscribers: Int,
    val title: String,
    @Json(name = "upvote_ratio")
    val upvoteRatio: Double,
     //can be image or vid link hot tell apart can also just be text post
    @Json(name = "url")
    val url: String,
    //if url is permalink then create a text, if not then either vid  or photo link
     val permalink: String,
        //image link for vids
     val thumbnail: String,
     val created_utc: Long,
     ) : RedditPostType()


@JsonClass(generateAdapter = true)
data class T1 (
    override val name: String,
    val link_id: String,
    val body: String
): RedditPostType()