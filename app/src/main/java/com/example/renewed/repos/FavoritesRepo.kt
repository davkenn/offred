package com.example.renewed.repos

import com.example.renewed.API
import com.example.renewed.Room.T3DAO
import com.example.renewed.Room.T5DAO
import com.example.renewed.models.RoomT3
import com.example.renewed.models.RoomT5
import com.example.renewed.models.T3
import com.example.renewed.models.toDbModel
import com.example.renewed.repos.BaseFavoritesRepo
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import timber.log.Timber
import java.util.concurrent.TimeUnit

class FavoritesRepo(private val t5: T5DAO, private val t3: T3DAO,private val api: API): BaseFavoritesRepo {

    override fun getNextSubreddits(i: Int): Single<List<RoomT5>> {
        return  t5.getNextFavoriteSubreddits(5)

    }
    override fun observeSavedSubreddits(): Observable<List<RoomT5>>{
        return t5.observeSavedSubreddits()
    }

    override fun getRandomPost(name:String): Single<RoomT3>{

        return  api.getRandomPost(name)

                //TODO fix this its way too hacky
            .map{(it[0].data.children[0].data as T3).toDbModel()}
    }



}


