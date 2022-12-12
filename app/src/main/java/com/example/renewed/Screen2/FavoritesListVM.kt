package com.example.renewed.Screen2


 import androidx.lifecycle.ViewModel
 import androidx.test.core.app.canTakeScreenshot
 import com.example.renewed.Room.FavoritesDAO
 import com.example.renewed.models.FullViewState
 import com.example.renewed.models.MyEvent
 import com.example.renewed.repos.BaseFavoritesRepo
 import com.example.renewed.models.MyFavsEvent
 import com.example.renewed.models.RoomT5
 import com.jakewharton.rxrelay3.PublishRelay
 import dagger.hilt.android.lifecycle.HiltViewModel
 import io.reactivex.rxjava3.core.Completable
 import io.reactivex.rxjava3.core.Observable
 import io.reactivex.rxjava3.core.ObservableOnSubscribe
 import io.reactivex.rxjava3.core.Single
 import io.reactivex.rxjava3.disposables.CompositeDisposable
 import io.reactivex.rxjava3.kotlin.addTo
 import io.reactivex.rxjava3.kotlin.mergeAll
 import io.reactivex.rxjava3.schedulers.Schedulers
 import timber.log.Timber
 import java.util.concurrent.TimeUnit

 import javax.inject.Inject

    @HiltViewModel
    class FavoritesListVM @Inject constructor(
        private val repository: BaseFavoritesRepo
    ): ViewModel() {
        val vs: Observable<List<String>>
        val vsPos: Observable<Int>
        private val disposables: CompositeDisposable = CompositeDisposable()
        private val inputEvents: PublishRelay<MyFavsEvent> = PublishRelay.create()
        init {

            //TODO i need a delete button to make this really worthwhile
            repository.observeSavedSubreddits()

                    //here i have 10 so 5 could be the number to delete and add

                .flatMap {   x->      Observable.just(1,1,1,1,1)
                    .map {  x.shuffled().take(1) }
                }
                .flatMapIterable { it }
                .flatMap{ repository.getRandomPosts(it.displayName,2) }
                    //TODO need to also save it to the db here
                .doOnNext {
                    repository.insert(it.name).subscribe({},
                        { Timber.e("dberror: ${it.localizedMessage}") }).addTo(disposables)
                }.startWith( repository.clearPages().subscribeOn(Schedulers.io())
                )

                .subscribe({
                    Timber.d("observ" + it.url)
                },
                    { Timber.e("observeerror: ${it.localizedMessage}") }).addTo(disposables)

            vs = repository.observeCurrentPostList()   .replay(1)
                .autoConnect(1){disposables.add(it)}

            vsPos= inputEvents.publish{
                it.ofType(MyFavsEvent.UpdatePositionEvent::class.java)}.map { it.newPosition}
                .replay(1)
                .autoConnect(1){disposables.add(it)}

            inputEvents.publish{
                it.ofType(MyFavsEvent.DeleteSubredditEvent::class.java)}
                  .flatMapCompletable{ repository.deletePages(it.targets)
                                                 .subscribeOn(Schedulers.io())}
                                            .subscribe()


        }

        override fun onCleared() {
            super.onCleared()
            Timber.d("oncleared in favslistvm")
            disposables.dispose()
        }


        fun processInput(name: MyFavsEvent) {

            inputEvents.accept(name)
        }
    }