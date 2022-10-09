package com.example.renewed

import com.example.renewed.models.RoomT3
import com.example.renewed.models.RoomT5
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

interface BaseSubredditsAndPostsRepo {

    fun prefetchSubreddits(): Completable
    fun prefetchPosts(): Completable

    //used to fil db with data if no internet
    fun prefetchDefaultSubreddits(): Completable
    fun prefetchDefaultPosts(): Completable

    fun getSubreddit(name: String): Single<RoomT5>
    fun getSubreddits(startFeedAfterThis: String?=null): Single<List<RoomT5>>

    fun getPost(name: String): Single<RoomT3>
    fun getPosts(name: String): Single<List<RoomT3>>

    fun deleteUninterestingSubreddits(): Completable
    fun deleteOrSaveSubreddit(name:String?,shouldDelete:Boolean):Completable

    fun updateSubreddits(
        srList: List<String>,
        isDisplayedInAdapter: Boolean,
        shouldToggleDisplayedColumnInDb: Boolean
    ): Completable


}
