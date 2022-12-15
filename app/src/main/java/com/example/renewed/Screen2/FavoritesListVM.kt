package com.example.renewed.Screen2


 import androidx.lifecycle.ViewModel
 import com.example.renewed.models.*
 import com.example.renewed.repos.BaseFavoritesRepo
 import com.jakewharton.rxrelay3.PublishRelay
 import dagger.hilt.android.lifecycle.HiltViewModel
 import io.reactivex.rxjava3.core.Observable
 import io.reactivex.rxjava3.disposables.CompositeDisposable
 import io.reactivex.rxjava3.kotlin.addTo
 import io.reactivex.rxjava3.schedulers.Schedulers
 import timber.log.Timber

 import javax.inject.Inject

    @HiltViewModel
    class FavoritesListVM @Inject constructor(
        private val repository: BaseFavoritesRepo
    ): ViewModel() {
        val vs3: Observable<PartialViewState>
        val vs4: Observable<PartialViewState>
        val vs: Observable<List<String>>
        val vsPos: Observable<Int>
        private val disposables: CompositeDisposable = CompositeDisposable()
        private val inputEvents: PublishRelay<MyFavsEvent> = PublishRelay.create()

        init {

            val newPostsObservable = repository.observeSavedSubreddits()
                //get exactly 10 posts, even if loading fails for some
                .flatMap { x ->
                    Observable.just(
                        1, 1, 1, 1, 1,
                        1, 1, 1, 1, 1
                    )
                        .map { x.shuffled().take(1) }
                }
                .flatMapIterable { it }
                .flatMap { repository.getRandomPosts(it.displayName, 2) }
                .share()  //Do I need this share? seems to work ok without it.
            //TODO need to also save it to the db here
            newPostsObservable . take(12)
                .doOnNext {
                repository.insert(it.name)
                    .subscribe({}, { Timber.e("dberr:${it.localizedMessage}") })
                    .addTo(disposables)
            }.startWith(repository.clearPages().subscribeOn(Schedulers.io()))
                .subscribe({ Timber.d("observ" + it.url) },
                    { Timber.e("observeerror: ${it.localizedMessage}") })
                .addTo(disposables)

            vs = repository.observeCurrentPostList().replay(1)
                .autoConnect(1) { disposables.add(it) }

            vsPos = inputEvents.publish {  it.ofType(MyFavsEvent.UpdatePositionEvent::class.java) }
                               .map { it.newPosition }
                               .replay(1)
                               .autoConnect(1) { disposables.add(it) }

            vs3 = inputEvents.publish { it.ofType(MyFavsEvent.DeleteSubredditEvent::class.java)
                                           .doOnNext {
                                                            repository.deletePages(it.targets)
                                                                      .subscribeOn(Schedulers.io())
                                                                      .subscribe() }
                        //     repository.insert(it.targets[it.targets.indices.random()])
                              }
                             .map { PartialViewState.SnackbarEffect }

            vs4= inputEvents.publish {
                it.ofType(MyFavsEvent.AddSubredditsEvent::class.java).flatMap {
                        newPostsObservable.take(6)
                        .doOnNext{Timber.e("SUCCESS!!! ${it.name}")}
                        .doOnNext {
                            repository.insert(it.name).subscribeOn(Schedulers.io())
                                .subscribe()
                        }
                        .map{PartialViewState.T3ForViewing(it.toViewState())}
                }}}



                    private fun Observable<MyFavsEvent.DeleteSubredditEvent>.updateViewingState(): Observable<RoomT3> {
                        return repository.observeSavedSubreddits()
                            //get exactly 10 posts, even if loading fails for some
                            .flatMap { x ->
                                Observable.just(
                                    1, 1, 1, 1, 1,
                                    1, 1, 1, 1, 1
                                )
                                    .map { x.shuffled().take(1) }
                            }
                            .flatMapIterable { it }
                            .flatMap { repository.getRandomPosts(it.displayName, 2) }

                          //TODO theres a latent bug here if the db gets less than four emissions

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