package com.example.renewed


 import androidx.lifecycle.ViewModel
 import com.example.renewed.models.MyEvent
 import com.example.renewed.models.MyFavsEvent
 import com.example.renewed.models.PartialViewState
 import com.example.renewed.models.RoomT5
 import com.jakewharton.rxrelay3.PublishRelay
 import dagger.hilt.android.lifecycle.HiltViewModel
 import io.reactivex.rxjava3.core.Flowable
 import io.reactivex.rxjava3.core.Observable
 import io.reactivex.rxjava3.disposables.CompositeDisposable
 import io.reactivex.rxjava3.kotlin.mergeAll
 import io.reactivex.rxjava3.schedulers.Schedulers
 import timber.log.Timber
 import java.util.concurrent.TimeUnit

 import javax.inject.Inject

    @HiltViewModel
    class FavoritesListVM @Inject constructor(
        private val repository: BaseFavoritesRepo
    ): ViewModel() {

        override fun onCleared() {
            super.onCleared()
            disposables.dispose()
        }
        private val disposables: CompositeDisposable = CompositeDisposable()
        private val inputEvents: PublishRelay<MyFavsEvent> = PublishRelay.create()
        val vs: Observable<List<RoomT5>> = inputEvents
        .doOnNext { Timber.d("---- Event is $it") }
        .eventToResult()
        .doOnNext { Timber.d("---- Result is $it") }

        .replay(1)
        .autoConnect(2){disposables.add(it)}


        val subs = prefetch1().subscribe{
            Timber.d("HERE",it.toString())
            processInput(MyFavsEvent.UpdateCurrentSubreddits)}




private fun Observable<MyFavsEvent>.eventToResult(): Observable<List<RoomT5>> {
            return publish { o ->
                val a = Observable.fromArray(
                    o.ofType(MyFavsEvent.UpdateCurrentSubreddits::class.java).onUpdateSubs(),
                    o.ofType(MyFavsEvent.UpdateDateRangeEvent::class.java).flatMap { Observable.just(listOf()) }
                )
                a.mergeAll()
            }
        }

        private fun Observable<MyFavsEvent.UpdateCurrentSubreddits>.onUpdateSubs(): Observable<List<RoomT5>> {
            return flatMap{repository.getNextSubreddits(5).toObservable()}
        }



        fun prefetch1(): Observable<Long> =
            Observable.interval(5, TimeUnit.SECONDS)




        fun processInput(name: MyFavsEvent) {

            inputEvents.accept(name)
        }
    }