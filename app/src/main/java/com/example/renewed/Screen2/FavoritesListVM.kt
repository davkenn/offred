package com.example.renewed.Screen2


 import androidx.lifecycle.ViewModel
 import androidx.test.core.app.canTakeScreenshot
 import com.example.renewed.Room.FavoritesDAO
 import com.example.renewed.models.*
 import com.example.renewed.repos.BaseFavoritesRepo
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
        val vs3: Observable<PartialViewState>
        val vs: Observable<List<String>>
        val vsPos: Observable<Int>
        private val disposables: CompositeDisposable = CompositeDisposable()
        private val inputEvents: PublishRelay<MyFavsEvent> = PublishRelay.create()

        init {

            var a = repository.observeSavedSubreddits()
                //get exactly 10 posts, even if loading fails for some
                .flatMap { x ->
                    Observable.just(
                        1, 1, 1, 1, 1,
                        1, 1, 1, 1, 1
                    )
                        .map { x.shuffled().take(1) }
                }
                .flatMapIterable { it }
                .flatMap { repository.getRandomPosts(it.displayName, 2) }.take(10)
                .share()
            //TODO need to also save it to the db here
            a.doOnNext {
                repository.insert(it.name)
                    .subscribe({}, { Timber.e("dberr:${it.localizedMessage}") })
                    .addTo(disposables)
            }.startWith(repository.clearPages().subscribeOn(Schedulers.io()))
                .subscribe({ Timber.d("observ" + it.url) },
                    { Timber.e("observeerror: ${it.localizedMessage}") })
                .addTo(disposables)

            //   a.flatMapCompletable {  }

            vs = repository.observeCurrentPostList().replay(1)
                .autoConnect(1) { disposables.add(it) }

            vsPos = inputEvents.publish {
                it.ofType(MyFavsEvent.UpdatePositionEvent::class.java)
            }.map { it.newPosition }
                .replay(1)
                .autoConnect(1) { disposables.add(it) }
            vs3 =
                inputEvents.publish {
                    it.ofType(MyFavsEvent.DeleteSubredditEvent::class.java).doOnNext {
                        repository.deletePages(it.targets).subscribeOn(Schedulers.io()).subscribe()
                    }
                        //     repository.insert(it.targets[it.targets.indices.random()])
                        .subscribeOn(Schedulers.io())
                }.map { PartialViewState.SnackbarEffect }

            var b= a.flatMap { repository.getRandomPosts(it.name,2) }
            inputEvents.publish {
                it.ofType(MyFavsEvent.AddSubredditsEvent::class.java).flatMap {

                    repository.observeSavedSubreddits()
                        //get exactly 10 posts, even if loading fails for some
                        .flatMap { x ->
                            Observable.just(
                                1, 1, 1, 1, 1,
                                1, 1, 1, 1, 1
                            )
                                .map { x.shuffled().take(1) }
                        }
                        .flatMapIterable { it }
                        .flatMap {
                            repository.getRandomPosts(it.displayName, 2)

                                .take(4).doOnNext {
                                    repository.insert(it.name).subscribeOn(Schedulers.io())
                                        .subscribe()
                                }

                            //    .map{PartialViewState.T3ForViewing(it.toViewState())} }
                            // }.doOnNextrepository.insert(it.targets[it.targets.indices.random()])
                            //   .subscribeOn(Schedulers.io())
                            //     }.map { PartialViewState.SnackbarEffect }

                            //.doOnNext
                            //     repository.deletePages(it.targets).subscribeOn(Schedulers.io()).subscribe()}

                            //         .flatMapIterable { it.targets }.flatMap { repository.getRandomPosts(it,2).subscribeOn(Schedulers.io())}
                            //                                                            .map{PartialViewState.T3ForViewing(it.toViewState())}}
                            //      .take(4)
                            //        .flatMap{ repository.getRandomPosts(it,2) }
                            //          . take(4).doOnNext(repository.insert())
                        }


                    //} //.compose( a.take(4)  )
                    //    .subscribe({}, { Timber.e("dberr:${it.localizedMessage}")})
                    //  .addTo(disposables)


                    //  . subscribe()
                }}}

                    private fun Observable<MyFavsEvent.DeleteSubredditEvent>.updateViewingState(): Observable<PartialViewState.T3ForViewing> {
                        return flatMapIterable { it.targets }
                            .flatMap { repository.getRandomPosts(it, 2) }
                            .take(4).map { PartialViewState.T3ForViewing(it.toViewState()) }


                        // }

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