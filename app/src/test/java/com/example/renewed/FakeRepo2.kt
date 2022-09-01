package com.example.renewed


import com.example.renewed.models.RoomT3
import com.example.renewed.models.RoomT5
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import okhttp3.mockwebserver.MockWebServer
import okio.buffer
import okio.source
import java.nio.charset.StandardCharsets
import javax.inject.Inject

class FakeRepo2  @Inject constructor() : BaseSubredditsAndPostsRepo {

    override fun prefetchSubreddits(): Completable {
     //   val a: MockWebServer = MockWebServer()
       // a.enqueueResponse("",200)

       // return Completable.complete()

        val inputStream = this.javaClass.classLoader!!.getResource("hundredcomments.json")
            .openStream()
        val source = inputStream?.let { inputStream.source().buffer() }
        var res = source?.let{it.readString(StandardCharsets.UTF_8)}

        return Completable.complete()
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

    override fun updateSubreddits(
        srList: List<String>,
        isDisplayed: Boolean,
        shouldDelete: Boolean,
        shouldUpdateDisplayed: Boolean
    ): Completable {
        TODO("Not yet implemented")
    }



 //   override fun setViewed(name: String, isDisplayed: Boolean): Completable {
   //     TODO("Not yet implemented")
   // }
}
