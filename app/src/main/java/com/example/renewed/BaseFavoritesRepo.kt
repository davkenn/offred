package com.example.renewed

import io.reactivex.rxjava3.core.Flowable

interface BaseFavoritesRepo{
    fun getCurrentState(): Flowable<Int>
}