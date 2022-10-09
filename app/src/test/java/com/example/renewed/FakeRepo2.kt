package com.example.renewed


import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import okio.buffer
import okio.source
import java.nio.charset.StandardCharsets


import com.example.renewed.models.*

import java.time.Instant


class FakeRepo2(val apiService: API) : BaseSubredditsAndPostsRepo {
    private var res : String? = null


    override fun prefetchSubreddits(): Completable {

         val inputStream = this.javaClass.classLoader!!.getResource("Berserk.json")
        .openStream()
        val source = inputStream?.let { inputStream.source().buffer() }
        res = source?.let{it.readString(StandardCharsets.UTF_8)}

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


    fun getSubreddits(): Single<List<RoomT5>> {
        return apiService.getRandomSubreddit()
            .map{(it as T5).toDbModel()}
            .map{ var b = mutableListOf<RoomT5>(it);b}

    }

    override fun getPost(name: String): Single<RoomT3> {
        return Single.error { Exception() }
    }

    override fun getPosts(name: String): Single<List<RoomT3>> {
        return Single.just(listOf(RoomT3("aaa","aaa", Instant.now(),Instant.now(),"aaa",
            "aaa","aaa","per", "aaa")))
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



}