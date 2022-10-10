package com.example.renewed

import com.example.renewed.DefaultDBContents.t3SampleList
import com.example.renewed.DefaultDBContents.t5SampleList
import com.example.renewed.Room.SavedSubredditsDAO
import com.example.renewed.Room.T3DAO
import com.example.renewed.Room.T5DAO
import com.example.renewed.models.*
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import java.lang.Integer.max
import java.lang.Integer.min
import java.time.Instant




class SubredditsAndPostsRepository(
    private val api: API, val t5Dao: T5DAO, val t3Dao: T3DAO
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
//TODO here you need to check if db is empty first otherwise this wont work
    override fun prefetchDefaultSubreddits():Completable =
                    Single.just(t5SampleList)
                          .flatMapCompletable{ x-> t5Dao.insertAll(x)}


    override fun prefetchDefaultPosts(): Completable =
        Single.just(t3SampleList)
            .flatMapCompletable{ x-> t3Dao.insertAll(x)}



    override fun prefetchSubreddits() : Completable =
        t5Dao.howManySubredditsInDb()
             .toObservable()
             .flatMapCompletable {  n-> loadSubredditsDb(
                                                      max(0,
                                                          min(80, 80-n.toInt())
                                                                                      )
                                                                                        )}

    private fun loadSubredditsDb(needed: Int): Completable =
        Observable.fromIterable(List(needed){0})
            .flatMap ( {  api.getRandomSubreddit().toObservable()} , 6)
            .map { (it as T5).toDbModel() }
            .flatMapCompletable { roomT5 -> t5Dao.insertT5(roomT5)}


    override fun getSubreddit(name: String): Single<RoomT5> =
        t5Dao.getSubreddit(name)


    override fun getSubreddits(startFeedAfterThis: String?) : Single<List<RoomT5>> =
        t5Dao.getSubredditsFromTable(startFeedAfterThis?:"")
             .flatMap { it -> updateSubreddits(it.map{it.name}, isDisplayedInAdapter = true,
                                                    shouldToggleDisplayedColumnInDb = false)
                             .andThen(Single.just(it))}


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

        return Observable.fromIterable(listOf(name)).flatMapSingle{t5Dao.getSubreddit(name!!)}
            .concatMapCompletable{

            callUpdate(it, shouldDelete)
        }
    }

    private fun callUpdate(l: RoomT5, shouldDelete: Boolean)  :Completable {
          return if (!shouldDelete)    t5Dao.saveSubreddit(l.name)
                 else                  t5Dao.delete(l.name)
    }


    override fun updateSubreddits(srList: List<String>, isDisplayedInAdapter: Boolean,
                                    shouldToggleDisplayedColumnInDb: Boolean): Completable {

        return Observable.fromIterable(srList)
            //TODO im just swallowing the error here, change back from maybe to see prob
            .flatMapMaybe {t5Dao.getSubreddit(it).onErrorComplete()}
            .concatMapCompletable {
                t5Dao.updateT5(it.copy(timeLastAccessed = Instant.now(),
                    //so as not to double count a view, views only updated when sent into adapter
                                totalViews= if (isDisplayedInAdapter) it.totalViews+1  else it.totalViews,
                                isDisplayed =  if (shouldToggleDisplayedColumnInDb) (it.isDisplayed+1) % 2
                                                                            else it.isDisplayed))

    }
}}
