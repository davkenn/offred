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

class FavoritesRepo(private val t5: T5DAO, private val t3: T3DAO,
                    private val favs:FavoritesDAO,private val api: API): BaseFavoritesRepo {


    override fun insert(s: String): Completable {
         //TODO bug where if you add list again all things in twice
        return favs.getPosts1(s).andThen(favs.insert(CurrentFavoritesList(s)))

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
   //         .flatMapSingle {  api.getHotComments(name)}
                .map{ x -> extractT3Field(x).toDbModel()}
                .doOnNext { t3.insertAll(listOf(it)).subscribe() }



//            .flatMapSingle {  api.getPostWithLotsOfComments()}
                /**This call makes this error in moshi adapter. now will fix it and see if it goes away
                 * turn this into an autmated test
                              .flatMapSingle {  api.getPostWithLotsOfComments()}
                2022-12-13 13:51:41.479 31104-31135/com.example.offred E/FavoritesListVM: observeerror:
                 * java.lang.IllegalStateException: unexpected type: more at $[1].data.children[8]
                **/
                      //    .map{ extractT3Field(it)}.
    //  take(number)
                   //       .doOnNext { t3.insertAll(it).subscribe() }
                //TODO this seems to be crashing sometimes too
    }

    private fun extractT3Field(it: List<Listing>): T3 = it[0].data.children[0].data as T3

    private fun extractT3Field(it: Listing): T3 = it.data.children[0].data as T3


}


