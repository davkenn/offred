package com.example.renewed.repos

import com.example.renewed.models.RoomT3
import com.example.renewed.models.RoomT5
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

interface BaseFavoritesRepo{
    fun getNextSubreddits(i: Int): Single<List<RoomT5>>
    fun observeSavedSubreddits(): Observable<List<RoomT5>>
    fun getRandomPost(name: String): Single<RoomT3>
    fun insert(s: String): Completable
    fun observeCurrentPostList(): Observable<List<String>>
}