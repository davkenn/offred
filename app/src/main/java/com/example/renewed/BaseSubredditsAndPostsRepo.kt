package com.example.renewed

import com.example.renewed.models.RoomT3
import com.example.renewed.models.RoomT5
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

interface BaseSubredditsAndPostsRepo {

    fun prefetchSubreddits(): Completable//will prefetch posts for things not yet in db or having posts
    fun prefetchPosts(): Completable

    fun getSubreddit(name: String): Single<RoomT5>
    fun getSubreddits(): Observable<List<RoomT5>>

    fun getPost(name: String): Single<RoomT3>
    fun getPosts(name: String): Single<List<RoomT3>>

    fun deleteSubreddits(names: List<String>): Observable<Unit>
    fun deleteUninterestingSubreddits(): Completable

    fun updateSubreddits(srList: List<String>,isDisplayed: Boolean,shouldDelete:Boolean =false): Completable

//    fun setViewed(name: String,isDisplayed:Boolean): Completable


}
