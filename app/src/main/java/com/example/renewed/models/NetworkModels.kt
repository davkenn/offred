package com.example.renewed.models


import com.example.renewed.moshiadapters.SubredditDescription
import com.squareup.moshi.JsonClass

import com.squareup.moshi.Json

/**
 * The Reddit API defines different entity types, including T5 for subreddits, T3 for posts in those
 * subreddits, and T1 for comments replying to those posts. For every kind of entity that is defined
 * in the reddit json endpoints we have three different model classes: the network models, the
 * database models, and the view models. This way, what is parsed from network responses,  what is
 * stored in the database, and what is displayed in the UI can all vary independently. These models
 * represent what is parsed from the network.
 */
@JsonClass(generateAdapter = true)
data class Holder(val data: RedditPostType, val kind: String)

@JsonClass(generateAdapter = true)
data class RedditListing(val children: List<Holder>, val before: String?, val after: String?)

@JsonClass(generateAdapter = true)
data class PostAndComments(val data: List<RedditListing>)

@JsonClass(generateAdapter = true)
data class Listing(val data: RedditListing)

sealed class RedditPostType {
    abstract val name:String
}

@JsonClass(generateAdapter = true)
data class T5 (override val name: String, val display_name: String, val icon_img: String?,
               val header_img: String?, val community_icon: String?, val banner_img: String?,
               val url: String, val subscribers: Int, val active_user_count: Int, val created_utc: Long,
               @SubredditDescription val description: String,
               @SubredditDescription val public_description: String): RedditPostType()

@JsonClass(generateAdapter = true)
data class T3(override val name: String, val author: String, val ups: Int, val downs: Int,
              val score: Int, val num_comments: Int, @SubredditDescription val selftext: String,
              val subreddit: String, val subreddit_id: String, val subreddit_subscribers: Int,
              @SubredditDescription val title: String, var url: String, val spoiler:Boolean,
              @Json(name = "upvote_ratio") val upvoteRatio: Double,
            //if json permalink is a url create a text post, if not then either vid  or photo post
              val permalink: String, val thumbnail: String, val created_utc: Long,
              val videoMedia:VideoMedia?, val secure_Video_media:VideoMedia?,
              val media_metadata:List<GalleryMedia>?) : RedditPostType()

@JsonClass(generateAdapter = true)
data class PicsAndDimens (val y:Int,val x:Int, val u:String)

@JsonClass(generateAdapter = true)
data class GalleryMedia(val e:String?, val m:String?, val p: List<PicsAndDimens>?)

@JsonClass(generateAdapter = true)
data class VideoMedia  (val reddit_video:Video?)

@JsonClass(generateAdapter = true)
data class Video  (val fallback_url:String?,val dash_url:String?)

@JsonClass(generateAdapter = true)
data class T1 (override val name: String, val link_id: String, val body: String): RedditPostType()

//if you fetch from a post a list of its replies and limit it to some number of posts n,
//you will also get a more datatype at the end giving you links to more replies
//Right now my moshi parser is failing posts with tons of replies and thus with a more
@JsonClass(generateAdapter = true)
data class More (override val name: String, val children: List<String>): RedditPostType()
