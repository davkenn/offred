package com.example.renewed.Screen2


 import androidx.lifecycle.ViewModel
 import com.example.renewed.Room.FavoritesDAO
 import com.example.renewed.repos.BaseFavoritesRepo
 import com.example.renewed.models.MyFavsEvent
 import com.example.renewed.models.RoomT5
 import com.jakewharton.rxrelay3.PublishRelay
 import dagger.hilt.android.lifecycle.HiltViewModel
 import io.reactivex.rxjava3.core.Observable
 import io.reactivex.rxjava3.disposables.CompositeDisposable
 import io.reactivex.rxjava3.kotlin.addTo
 import io.reactivex.rxjava3.kotlin.mergeAll
 import timber.log.Timber
 import java.util.concurrent.TimeUnit

 import javax.inject.Inject

    @HiltViewModel
    class FavoritesListVM @Inject constructor(
        private val repository: BaseFavoritesRepo
    ): ViewModel() {
        val vs: Observable<List<String>>
        private val disposables: CompositeDisposable = CompositeDisposable()
        private val inputEvents: PublishRelay<MyFavsEvent> = PublishRelay.create()
        init {
     //       prefetch1().subscribe({
       //         Timber.d("HERE", it.toString())
         //   },
      //          { Timber.e("FAVLISTERROR", it.stackTrace) }).addTo(disposables)


            //TODO i need a delete button to make this really worthwhile
            repository.observeSavedSubreddits()
                //have to delete in here before make sublist
                .map { it.shuffled().take(4) }.flatMapIterable { it }
                .flatMapSingle { repository.getRandomPost(it.displayName) }
                    //TODO need to also save it to the db here

                .doOnNext {
                    repository.insert(it.name).subscribe({},
                        { Timber.e("dberror: ${it.localizedMessage}") })
                }


                .subscribe({
                    Timber.d("observ" + it.url)
                },
                    { Timber.e("observeerror: ${it.localizedMessage}") }).addTo(disposables)

            vs = repository.observeCurrentPostList()        .replay(1)
                .autoConnect(1){disposables.add(it)}



    //        repository.observeCurrentPostList()
 //               .subscribe { Timber.d("you $it") }.addTo(disposables)
            //have to delete in here before make sublist

        }

        override fun onCleared() {
            super.onCleared()
            disposables.dispose()
        }







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