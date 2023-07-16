package com.example.renewed.repos

import android.util.Base64
import android.util.Log
import com.example.renewed.API
import com.example.renewed.AuthAPI
import com.example.renewed.Room.T3DAO
import com.example.renewed.Room.T5DAO
import com.example.renewed.SCREEN1_DB_SIZE
import com.example.renewed.models.*
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import java.lang.Integer.max
import java.lang.Integer.min
import java.time.Instant

class SubredditsAndPostsRepo(private val t5Dao: T5DAO, private val t3Dao: T3DAO,
                             private val api: API ): BaseSubredditsAndPostsRepo {

    override fun prefetchPosts(): Completable =
        t5Dao.getSubredditIDsNeedingPosts()
             .flattenAsObservable { it }
             .flatMap( { api.getPostsInDateRange(it).toObservable() }, 10)
             .map { list -> list.data.children.map {(it.data as T3).toDbModel()} }
             .flatMapCompletable { roomT3s -> t3Dao.insertAll(roomT3s) }

    override fun prefetchSubreddits() : Completable =
        t5Dao.howManySubredditsInDb()
             .toObservable()
             .flatMapCompletable {  n->
                                        loadSubredditsDb(max(0,
                                            min(SCREEN1_DB_SIZE, SCREEN1_DB_SIZE-n.toInt())))
                                 }

    //TODO theres a bug still i think when you start after an hour probably need to handle invalid token
    private fun loadSubredditsDb(needed: Int): Completable =
        Observable.fromIterable(List(needed){0})
            .flatMap ( {  api.getRandomSubreddit().toObservable()} , 10)
            .map { (it as T5).toDbModel() }
            .flatMapCompletable { roomT5 -> t5Dao.insertT5(roomT5)}

    override fun getSubreddit(name: String): Single<RoomT5> =
        t5Dao.getSubreddit(name)

    override fun getSubreddits(startFeedAfterThis: String?) : Single<List<RoomT5>> =
        t5Dao.getSubredditsFromTable(if (startFeedAfterThis.isNullOrEmpty()) ""
                                    else startFeedAfterThis)
            .flatMap { it ->
                updateSubreddits(it.map { it.name }, isDisplayedInAdapter = true,
                                          shouldToggleDisplayedColumnInDb = false)
                .andThen(Single.just(it))
            }.subscribeOn(Schedulers.io())

    override fun getPost(name:String) : Single<RoomT3> = t3Dao.getPost(name)
    override fun getPosts(name:String) : Single<List<RoomT3>> = t3Dao.getPosts(name).subscribeOn(Schedulers.io())

    override fun deleteUninterestingSubreddits(): Completable= t5Dao.deleteUnwanted()
    override fun deleteOrSaveSubreddit(name: String?, shouldDelete: Boolean): Completable =
         Observable.fromIterable(listOf(name)).flatMapSingle{t5Dao.getSubreddit(name!!)}
                   .concatMapCompletable{ callUpdate(it, shouldDelete) }.subscribeOn(Schedulers.io())

    private fun callUpdate(l: RoomT5, shouldDelete: Boolean)  :Completable =
          if (!shouldDelete)  t5Dao.saveSubreddit(l.name)
          else                t5Dao.delete(l.name)

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
