package com.example.renewed

import com.example.renewed.Room.T3DAO
import com.example.renewed.Room.T5DAO
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.FlowableEmitter
import java.util.concurrent.Flow

class FavoritesRepository(private val t5: T5DAO, private val t3: T3DAO):BaseFavoritesRepo {
    override fun getCurrentState(): Flowable<Int> {
       return Flowable.fromArray(1,2,3)
    }
}