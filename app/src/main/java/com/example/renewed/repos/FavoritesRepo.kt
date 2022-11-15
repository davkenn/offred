package com.example.renewed.repos

import com.example.renewed.Room.T3DAO
import com.example.renewed.Room.T5DAO
import com.example.renewed.models.RoomT5
import com.example.renewed.repos.BaseFavoritesRepo
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import java.util.concurrent.TimeUnit

class FavoritesRepo(private val t5: T5DAO, private val t3: T3DAO): BaseFavoritesRepo {
    override fun getCurrentState(): Flowable<Int> {
        return Flowable.fromArray(1, 2, 3)
    }

    override fun startPollingPosts(): Observable<Long> {
        return Observable.interval(1, TimeUnit.SECONDS).replay(10).take(10)
    }

    override fun getNextSubreddits(i: Int): Single<List<RoomT5>> {
        return  t5.getNextFavoriteSubreddits(5)

    }}


