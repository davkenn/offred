package com.example.renewed

import com.example.renewed.DefaultDBContents.t3SampleList
import com.example.renewed.models.RoomT3
import com.example.renewed.models.RoomT5
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject
import com.example.renewed.DefaultDBContents.t5SampleList
import com.example.renewed.repos.BaseSubredditsAndPostsRepo


class FakeRepo @Inject constructor() : BaseSubredditsAndPostsRepo {
    private  val res:List<RoomT5> = t5SampleList
    private val res2 : List<RoomT3> = t3SampleList


    override fun prefetchSubreddits(): Completable {

        return   return Completable.complete()
    }

    override fun prefetchPosts(): Completable {

           return Completable.complete()

    }




    override fun getSubreddit(name: String): Single<RoomT5> {
        return Single.just(res[0])
    }

    override fun getSubreddits(startFeedAfterThis: String?): Single<List<RoomT5>> {
        return Single.just(res.toList())
    }


    override fun getPost(name: String): Single<RoomT3> {
        return Single.just(res2[0])
    }

    override fun getPosts(name: String): Single<List<RoomT3>> {
        return Single.just(res2.toList())
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