package com.example.offred.repos

import com.example.offred.models.RoomT3
import com.example.offred.models.RoomT5
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable

interface BaseFavoritesRepo{
    fun observeSavedSubreddits(): Observable<List<RoomT5>>
    fun getRandomPosts(name: String,number:Int): Observable<RoomT3>
    fun insert(s: String): Completable
    fun observeCurrentPostList(): Observable<List<String>>
    fun clearPages(): Completable
    fun deletePages(s: List<String>): Completable
    fun insertAll(posts: List<String>): Completable

    fun getPostsFromSavedSubreddit(subreddit: RoomT5): Observable<RoomT3>
    fun currentLength(): Observable<Int>
}