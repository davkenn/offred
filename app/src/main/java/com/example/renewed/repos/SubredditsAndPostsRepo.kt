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

class SubredditsAndPostsRepo(
    private val api: API, private val auth: AuthAPI, private val t5Dao: T5DAO, private val t3Dao: T3DAO
): BaseSubredditsAndPostsRepo {
    var a=""
//var a = "eyJhbGciOiJSUzI1NiIsImtpZCI6IlNIQTI1NjpzS3dsMnlsV0VtMjVmcXhwTU40cWY4MXE2OWFFdWFyMnpLMUdhVGxjdWNZIiwidHlwIjoiSldUIn0.eyJzdWIiOiJsb2lkIiwiZXhwIjoxNjg5Mjk1NTM2LjA0NTkxNCwiaWF0IjoxNjg5MjA5MTM2LjA0NTkxNCwianRpIjoiVnc2dUZ3TkxRYk5JWEhlLV85LVFrU2duSkhNd0l3IiwiY2lkIjoidTNNYU1haDBkT2UxSUEiLCJsaWQiOiJ0Ml9mY2pmeDdld2UiLCJsY2EiOjE2ODkyMDkxMzYwNDQsInNjcCI6ImVKeUtWdEpTaWdVRUFBRF9fd056QVNjIiwiZmxvIjo2fQ.gPm2G7g20KLFCQ-hNeLcHPflEJJBWtynv1esI8ej3jPUeobeDwK7omPOlYR3WOkg-JVlfK2CX5QlfpV1E6D624at7cQxvd8mJLx3JA7mAA7iKX7AK-OWsU0BDVh65cotuAKKytk2qvUyQCJMcy7QnRWnTxRp276jP77ZMAtgjMtgiSmbxcm96OR8Wv-6UPo_FshvHmSLa8e1K1UE9bt0JDL6owdCYTyT-rb06IRZKNGKYgd7xMYPFcp-T6iJcG-_kd06NsGeFiJXRfCP8Yd1zo3Ja1ey7tzhTdWx3dubOpifcu9WR0zndCcCuAoc4bhOFaLVJ9rT92rARqcLRSOt9A"

    init{


    }
    override fun login():Single<String>{

                  auth.installedClient(        "https://oauth.reddit.com/grants/installed_client",
            "DO_NOT_TRACK_THIS_DEViCE").subscribeBy { a=it.getOrDefault("access_token","") }
        return Single.just("AAAA")
   }

    override fun prefetchPosts(): Completable =
        t5Dao.getSubredditIDsNeedingPosts()
             .flattenAsObservable { it }
             .flatMap( { api.getPostsInDateRange(it).toObservable() }, 6)
             .map { list -> list.data.children.map {(it.data as T3).toDbModel()} }
             .flatMapCompletable { roomT3s -> t3Dao.insertAll(roomT3s) }

    override fun prefetchSubreddits() : Completable =
        t5Dao.howManySubredditsInDb()
             .toObservable()
             .flatMapCompletable {  n-> loadSubredditsDb(
                                                      max(0,
                                                          min(SCREEN1_DB_SIZE, SCREEN1_DB_SIZE-n.toInt())))
                                 }

    private fun loadSubredditsDb(needed: Int): Completable =
        Observable.fromIterable(List(needed){0})
            .flatMap ( {  api.getRandomSubreddit("Bearer $a").toObservable()} , 1)
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
