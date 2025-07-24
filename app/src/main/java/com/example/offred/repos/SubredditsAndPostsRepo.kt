package com.example.offred.repos

import com.example.offred.API
import com.example.offred.Room.T3DAO
import com.example.offred.Room.T5DAO
import com.example.offred.SCREEN1_DB_SIZE
import com.example.offred.models.*
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber
import java.time.Instant

class SubredditsAndPostsRepo(
    private val t5Dao: T5DAO,
    private val t3Dao: T3DAO,
    private val api: API
    ): BaseSubredditsAndPostsRepo {

    private var currentAfterToken: String? = null
    override fun prefetchPosts(): Completable =
        t5Dao.getSubredditIDsNeedingPosts()
             .flattenAsObservable { it }
             .flatMap( { api.getPostsInDateRange(it).toObservable() }, 10)
             .map { list -> list.data.children.map {(it.data as T3).toDbModel()} }
             .flatMapCompletable { roomT3s -> t3Dao.insertAll(roomT3s) }

    override fun prefetchSubreddits() : Completable =
        t5Dao.howManySubredditsInDb().
                doOnSuccess{ Timber.e("IN DB: $it")}
             .flatMapCompletable {   loadSubredditsDb(SCREEN1_DB_SIZE-it.toInt())}


    private fun loadSubredditsDb(needed: Int): Completable =
                 api.getPostsFromAll(needed,currentAfterToken).doOnSuccess{currentAfterToken= it.data.after}
                     .flattenAsObservable { it.data.children }
                     .map{(it.data as T3).subreddit}
                     .distinct()
                     .take(needed.toLong())
                     .flatMapSingle{api.getSubredditDetails(it)}

                     .flatMapCompletable{t5Dao.insertT5((it as T5).toDbModel())}



    override fun getSubreddit(name: String): Single<RoomT5> =
        t5Dao.getSubreddit(name)

    override fun getSubreddits(startFeedAfterThis: String?) : Single<List<RoomT5>> =
        t5Dao.getSubredditsFromTable(if (startFeedAfterThis.isNullOrEmpty()) ""
                                    else startFeedAfterThis)
            .flatMap {
                updateSubreddits(it.map { x -> x.name }, isDisplayedInAdapter = true,
                                          shouldToggleDisplayedColumnInDb = false)
                .andThen(Single.just(it))
            }.subscribeOn(Schedulers.io())

    override fun getPost(name:String) : Single<RoomT3> = t3Dao.getPost(name)
    override fun getPosts(name:String) : Single<List<RoomT3>> = t3Dao.getPosts(name).subscribeOn(Schedulers.io())

    override fun deleteUninterestingSubreddits(): Completable= t5Dao.deleteUnwanted()

    override fun saveSubreddit(name: String?): Completable {
        if (name == null) {
            return Completable.complete()
        }

    // Now that we know 'name' is not null, we can safely start the chain.
    // t5Dao.getSubreddit(name) already returns a Single, so we can start there.
    return t5Dao.getSubreddit(name)
    .flatMapCompletable { roomT5 ->
        // Use the result from getSubreddit to perform the save
        t5Dao.saveSubreddit(roomT5.name)
    }
    .subscribeOn(Schedulers.io())
}




    override fun updateSubreddits(srList: List<String>, isDisplayedInAdapter: Boolean,
                                    shouldToggleDisplayedColumnInDb: Boolean): Completable =
        Observable.fromIterable(srList)
            //TODO im just swallowing the error here, change back from maybe to see prob
            .flatMapMaybe {t5Dao.getSubreddit(it).onErrorComplete()}
            .concatMapCompletable {
                t5Dao.updateT5(it.copy(timeLastAccessed = Instant.now(),
                    //so as not to double count a view, views only updated when sent into adapter
                                totalViews= if (isDisplayedInAdapter) it.totalViews+1  else it.totalViews,
                                isDisplayed =  if (shouldToggleDisplayedColumnInDb) (it.isDisplayed+1) % 2
                                                                            else it.isDisplayed))
                                    }.subscribeOn(Schedulers.io())

    override fun clearDisplayed(): Completable = t5Dao.clearDisplayed().subscribeOn(Schedulers.io())
}
