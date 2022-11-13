package com.example.renewed

import com.example.renewed.models.RoomT5
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

interface BaseFavoritesRepo{
    fun getCurrentState(): Flowable<Int>
    fun startPollingPosts(): Observable<Long>
    abstract fun getNextSubreddits(i: Int): Single<List<RoomT5>>
}