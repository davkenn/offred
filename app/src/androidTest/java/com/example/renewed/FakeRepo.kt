package com.example.renewed

import com.example.renewed.models.RoomT3
import com.example.renewed.models.RoomT5
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject


class FakeRepo @Inject constructor() : BaseSubredditsAndPostsRepo {
    override fun prefetchSubreddits(): Completable {
        TODO("Not yet implemented")
    }

    override fun prefetchPosts(): Completable {
        TODO("Not yet implemented")
    }

    override fun getSubreddit(name: String): Single<RoomT5> {
        TODO("Not yet implemented")
    }

    override fun getSubreddits(): Single<List<RoomT5>> {
        TODO("Not yet implemented")
    }

    override fun getPost(name: String): Single<RoomT3> {
        TODO("Not yet implemented")
    }

    override fun getPosts(name: String): Single<List<RoomT3>> {
        TODO("Not yet implemented")
    }

    override fun deleteSubreddits(names: List<String>): Observable<Unit> {
        TODO("Not yet implemented")
    }

    override fun deleteUninterestingSubreddits(): Completable {
        TODO("Not yet implemented")
    }

    override fun updateSubreddits(srList: List<String>,isDisplayed: Boolean,shouldDelete:Boolean): Completable {
        TODO("Not yet implemented")
    }

}