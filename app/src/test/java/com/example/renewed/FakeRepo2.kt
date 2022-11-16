package com.example.renewed


import com.example.renewed.models.RoomT3
import com.example.renewed.models.RoomT5
import com.example.renewed.models.T5
import com.example.renewed.models.toDbModel
import com.example.renewed.repos.BaseSubredditsAndPostsRepo
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import okio.buffer
import okio.source
import java.nio.charset.StandardCharsets


class FakeRepo2(private val apiService: API) : BaseSubredditsAndPostsRepo {
    private var res : String? = null


    override fun prefetchSubreddits(): Completable {

         val inputStream = this.javaClass.classLoader!!.getResource("Berserk.json")
        .openStream()
        val source = inputStream?.let { inputStream.source().buffer() }
        res = source?.readString(StandardCharsets.UTF_8)

        return Completable.complete()
    }

    override fun prefetchPosts(): Completable {
        return Completable.complete()
    }

    override fun getSubreddit(name: String): Single<RoomT5> {
        return Single.error(Exception())
    }

    override fun getSubreddits(startFeedAfterThis: String?): Single<List<RoomT5>> =
        getSubreddits()


    private fun getSubreddits(): Single<List<RoomT5>> {
        return apiService.getRandomSubreddit()
            .map{(it as T5).toDbModel()}
            .map{ val b = mutableListOf<RoomT5>(it);b}

    }

    override fun getPost(name: String): Single<RoomT3> {
        return Single.error { Exception() }
    }

    override fun getPosts(name: String): Single<List<RoomT3>> {
   //     return Single.just(listOf(RoomT3("aaa","aaa", Instant.now(),Instant.now(),"aaa",
     //       "aaa","aaa","per", "aaa")))
        return Single.just(listOf())
    }


    override fun deleteUninterestingSubreddits(): Completable {
        return Completable.complete()
    }

    override fun deleteOrSaveSubreddit(name: String?, shouldDelete: Boolean): Completable {
        return Completable.complete()
    }

    override fun updateSubreddits(
        srList: List<String>,
        isDisplayedInAdapter: Boolean,
        shouldToggleDisplayedColumnInDb: Boolean
    ): Completable {
        return Completable.complete()
    }

    override fun clearDisplayed(): Completable {
        return Completable.complete()
    }


}