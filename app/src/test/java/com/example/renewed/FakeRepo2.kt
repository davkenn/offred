package com.example.renewed


import com.example.renewed.models.RoomT3
import com.example.renewed.models.RoomT5
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import okhttp3.mockwebserver.MockWebServer
import javax.inject.Inject

class FakeRepo2  @Inject constructor() : BaseSubredditsAndPostsRepo {

    override fun prefetchSubreddits(): Completable {
        val a: MockWebServer = MockWebServer()
        a.enqueueResponse("",200)

        return Completable.complete()

    }

    override fun prefetchPosts(): Completable {
        TODO("Not yet implemented")
    }

    override fun getSubreddit(name: String): Single<RoomT5> {
        TODO("Not yet implemented")
    }

    override fun getSubreddits(): Observable<List<RoomT5>> {
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

    override fun updateSubreddits(srList: List<String>): Completable {
        TODO("Not yet implemented")
    }

    override fun setViewed(name: String, isDisplayed: Boolean): Completable {
        TODO("Not yet implemented")
    }
}
