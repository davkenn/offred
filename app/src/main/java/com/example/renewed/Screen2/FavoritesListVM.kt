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
        private val currentlyDisplayedRepo: BaseFavoritesRepo
    ): ViewModel() {
        val vs3: Observable<PartialViewState>
        val vs4: Observable<PartialViewState>
        val vs: Observable<List<String>>
        val vsPos: Observable<Int>
        private val disposables: CompositeDisposable = CompositeDisposable()
        private val inputEvents: PublishRelay<MyFavsEvent> = PublishRelay.create()

        init {
            val newPostsObservable = currentlyDisplayedRepo.observeSavedSubreddits()
                .flatMap { x -> Observable.just(1).repeat(10)
                                .map { x.shuffled().first()}
                         }
                .flatMap { currentlyDisplayedRepo.getRandomPosts(it.displayName, 2) }
                .share()  //Do I need this share? seems to work ok without it.
            //TODO need to also save it to the db here


            newPostsObservable.take(12)
                              .flatMapCompletable { x -> currentlyDisplayedRepo.insert(x.name) }
                              .startWith(currentlyDisplayedRepo.clearPages()
                              .subscribeOn(Schedulers.io()))
                              .subscribe({ Timber.d("observ" ) },
                                          { Timber.e("error: ${it.localizedMessage}") })
                              .addTo(disposables)

            vs = currentlyDisplayedRepo.observeCurrentPostList().replay(1)
                .autoConnect(1) { disposables.add(it) }

            vsPos = inputEvents.publish { it.ofType(MyFavsEvent.UpdatePositionEvent::class.java) }
                .map { it.newPosition }
                .replay(1)

                .autoConnect(1) { disposables.add(it) }

            vs3 = inputEvents.publish {
                it.ofType(MyFavsEvent.DeleteSubredditEvent::class.java)
                    .doOnNext {
                        currentlyDisplayedRepo.deletePages(it.targets)
                            .subscribeOn(Schedulers.io())
                            .subscribe()
                    }
                //     repository.insert(it.targets[it.targets.indices.random()])
            }
                .map { PartialViewState.SnackbarEffect }

            vs4 = inputEvents.publish {
                it.ofType(MyFavsEvent.AddSubredditsEvent::class.java).flatMap {
                    newPostsObservable.take(it.count.toLong())
                        .doOnNext { Timber.e("SUCCESS!!! ${it.name}") }
                        .doOnNext {

                            currentlyDisplayedRepo.insert(it.name).subscribeOn(Schedulers.io())
                                .subscribe()
                        }
                        .map { PartialViewState.T3ForViewing(it.toViewState()) }
                }
            }
        }
         //   private fun Observable<MyFavsEvent.DeleteSubredditEvent>.updateViewingState(): Observable<RoomT3> {
           //     return repository.observeSavedSubreddits()
                    //get exactly 10 posts, even if loading fails for some
             //       .flatMap { x ->
               //         Observable.just(
                 //           1, 1, 1, 1, 1,
                   //         1, 1, 1, 1, 1
                     //   )
                       //     .map { x.shuffled().take(1) }
                  //  }
                   // .flatMapIterable { it }
                  //  .flatMap { repository.getRandomPosts(it.displayName, 2) }


            fun clearPages() = currentlyDisplayedRepo.clearPages()


            override fun onCleared() {
                super.onCleared()
                Timber.d("oncleared in favslistvm")
                disposables.dispose()
            }

            fun processInput(name: MyFavsEvent) {
                inputEvents.accept(name)
            }
        }