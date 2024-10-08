package com.example.renewed.repos

import com.example.renewed.API
import com.example.renewed.AuthAPI
import com.example.renewed.Room.FavoritesDAO
import com.example.renewed.Room.T3DAO
import com.example.renewed.Room.T5DAO
import com.example.renewed.models.*
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.kotlin.subscribeBy
import retrofit2.http.Path

class FavoritesRepo(private val t5: T5DAO,
    private val t3: T3DAO,
    private val favs:FavoritesDAO,
    private val api: API
    ): BaseFavoritesRepo {

    override fun insert(s: String): Completable {
        return favs.insert(CurrentFavoritesList(s))

    }
    override fun observeSavedSubreddits(): Observable<List<RoomT5>>{
        return t5.observeSavedSubreddits()
    }

    override fun observeCurrentPostList(): Observable<List<String>>{
        return favs.getPosts()
    }

    override fun deletePages(s:List<String>): Completable {
        return favs.deleteList(s)
    }

    override fun clearPages(): Completable {
        return favs.clearDb().startWith(t5.deleteUnsavedPosts())
    }

    override fun getRandomPosts(name:String,number:Int): Observable<RoomT3> {
        return  Observable.just(name).repeat(number.toLong())
                          .flatMapSingle {  api.getRandomPost(name)}
                .map{ x -> extractT3Field(x).toDbModel()}
                .doOnNext { t3.insertAll(listOf(it)).subscribe() }
    }

    private fun extractT3Field(it: List<Listing>): T3 = it[0].data.children[0].data as T3

}


