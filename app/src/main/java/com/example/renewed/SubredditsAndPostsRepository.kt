package com.example.renewed

import androidx.room.rxjava3.EmptyResultSetException
import com.example.renewed.Room.T3DAO
import com.example.renewed.Room.T5DAO
import com.example.renewed.models.*
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import timber.log.Timber
import java.lang.Integer.max
import java.lang.Integer.min
import java.time.Instant




class SubredditsAndPostsRepository(private val api : API,
                                   val t5Dao: T5DAO,
                                   val t3Dao: T3DAO
): BaseSubredditsAndPostsRepo {



    override fun prefetchPosts(): Completable {


        return t5Dao.getSubredditIDsNeedingPosts()
            .flattenAsObservable { it }
            .flatMap( { api.getPostsInDateRange(it).toObservable() }, 6)
            .map { list -> list.data.children.map {(it.data as T3).toDbModel()} }
            .flatMapCompletable { roomT3s ->
                t3Dao.insertAll(roomT3s)
            }
    }

    override fun prefetchSubreddits() : Completable =
        t5Dao.howManySubredditsInDb()
             .toObservable()
             .flatMapCompletable {  n-> loadSubredditsDb(
                                                      max(0,
                                                          min(80, 80-n.toInt())
                                                                                      )
                                                                                        )}


    //https://stackoverflow.com/questions/42161293/rxjava-flatmap-how-to-skip-errors
    private fun loadSubredditsDb(needed: Int): Completable =
        Observable.fromIterable(List(needed){0})
            .flatMap ( {  api.getRandomSubreddit().toObservable()} , 6)
            .map { (it as T5).toDbModel() }
            .flatMapCompletable { roomT5 -> t5Dao.insertT5(roomT5)}

/**
    override fun getSubreddit(name: String): Single<RoomT5> =
        try{t5Dao.getSubreddit("")}catch (e: EmptyResultSetException){ Single.just(  RoomT5(
            "ERROR",
            "ERROR",
            "${e.message}",
            "",
            "",
            Instant.now(),
            1,
            Instant.now(),
            false) )}
**/
    override fun getSubreddit(name: String): Single<RoomT5> =
        t5Dao.getSubreddit(name)/**.onErrorResumeWith(Single.just(
            RoomT5(
                "ERROR",
                "ERROR",
                "",
                "",
                "", Instant.now(),0, Instant.now())**/



    override fun getSubreddits() : Single<List<RoomT5>> =
        t5Dao.getSubredditsFromTable()


    override fun getPost(name:String) : Single<RoomT3> {
        return  t3Dao.getPost(name)
    }


    override fun getPosts(name:String) : Single<List<RoomT3>> {
      return  t3Dao.getPosts(name)
    }

    //TODO if this is seven it will repeat the ones with the lowest vals, fix the alg or leave it at thre
    override fun deleteUninterestingSubreddits(): Completable{

        return  t5Dao.deleteUnwanted(3)
    }

    override fun deleteOrSaveSubreddit(name: String?, shouldDelete: Boolean): Completable {
        return if (name.isNullOrBlank()) Completable.complete()
        else t5Dao.getSubreddit(name).concatMapCompletable {
                t5Dao.updateT5(it.copy(isDisplayed = 0,
                isSaved = !shouldDelete, totalViews = if (shouldDelete)  40000 else it.totalViews)) }
    }



    //TODO if im going to delete i have to remove from back stack
    override fun deleteSubreddits(names:List<String>): Observable<Unit> {

        return Observable.fromIterable(names)
                         .flatMapCompletable {  t5Dao.delete(it) }
                         .toObservable()
    }



//TODO break this back up into setviewed and delete

    override fun updateSubreddits(srList: List<String>, isDisplayedFlagSet:Boolean,
                                shouldUpdateDisplayed:Boolean): Completable {


    return Observable.fromIterable(srList)
                    .flatMapSingle {t5Dao.getSubreddit(it)}
                    .doOnError { Timber.e("----error fetching subreddit for update" +
                            "${it.stackTraceToString()} ") }

                    .concatMapCompletable {
                        t5Dao.updateT5(it.copy(timeLastAccessed = Instant.now(),
                            //so as not to double count a view, its sense of how many times its
                            //been viewed is only updated when its sent back to the system
                            totalViews= if (shouldUpdateDisplayed) it.totalViews else it.totalViews+1,
                                isDisplayed =  if (shouldUpdateDisplayed) isDisplayedFlagSet.compareTo(false)
                                                                        else it.isDisplayed))

    }




}}
