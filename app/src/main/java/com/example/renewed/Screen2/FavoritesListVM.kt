package com.example.renewed.Screen2


 import androidx.lifecycle.ViewModel
 import com.example.renewed.VIEWPAGER_PAGES_TOTAL
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
        private val favsRepo: BaseFavoritesRepo
    ): ViewModel() {
        val deletePostsComplete: Observable<EffectType2>
        val addPostsComplete: Observable<EffectType2>
        val currentlyDisplayedPosts: Observable<List<String>>
        val currentPosition: Observable<Int>
        private val disposables: CompositeDisposable = CompositeDisposable()
        private val inputEvents: PublishRelay<MyFavsEvent> = PublishRelay.create()

        init {
            val newPostsObservable = favsRepo.observeSavedSubreddits()
                .flatMap { x -> Observable.just(Unit).repeat(10)
                                .map { x.shuffled().first()}
                         }
                .flatMap { favsRepo.getRandomPosts(it.displayName, 2) }
                .share()  //Do I need this share? seems to work ok without it.
            //TODO need to also save it to the db here


            newPostsObservable.take(VIEWPAGER_PAGES_TOTAL.toLong())
                              .flatMapCompletable { x -> favsRepo.insert(x.name) }
                              .startWith(favsRepo.clearPages()
                              .subscribeOn(Schedulers.io()))
                              .subscribe({ Timber.d("observ" ) },
                                          { Timber.e("error: ${it.localizedMessage}") })
                              .addTo(disposables)

            currentlyDisplayedPosts = favsRepo.observeCurrentPostList().replay(1)
                .autoConnect(1) { disposables.add(it) }

            currentPosition = inputEvents.publish { it.ofType(MyFavsEvent.UpdatePositionEvent::class.java) }
                .map { it.newPosition }

                .replay(1)

                .autoConnect(1) { disposables.add(it) }

            deletePostsComplete = inputEvents.publish {
                //   it.ofType(MyFavsEvent.DeleteSubredditEvent::class.java).deleteThenReturn()
                it.ofType(MyFavsEvent.DeleteSubredditEvent::class.java).deleteThenReturn()
            }

            addPostsComplete = inputEvents.publish {
                it.ofType(MyFavsEvent.AddSubredditsEvent::class.java).flatMap {
                    newPostsObservable.take(it.count.toLong())
                        .doOnNext { Timber.e("SUCCESS!!! ${it.name}") }
                        .doOnNext {

                            favsRepo.insert(it.name).subscribeOn(Schedulers.io())
                                .subscribe()
                        }
                        .map { EffectType2.LOAD }
                }
            }
        }
   //     val vs: Observable<FullViewStateScreen2> = inputEvents
     //       .doOnNext { Timber.d("---- Event is $it") }

          //  .doOnNext { Timber.d("---- Result is $it") }
            //.combineResults()
          //  .doOnNext { Timber.d("----Combined is $it") }
       //     .replay(1)
         //   .autoConnect(1){disposables.add(it)}
            private fun Observable<MyFavsEvent.DeleteSubredditEvent>.deleteThenReturn() : Observable<EffectType2> {
                return doOnNext {
                    favsRepo.deletePages(it.targets)
                        .subscribeOn(Schedulers.io())
                        .subscribe()
                }

                    .map {EffectType2.DELETE }
            }
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


            fun clearPages() = favsRepo.clearPages()


            override fun onCleared() {
                super.onCleared()
                Timber.d("oncleared in favslistvm")
                disposables.dispose()
            }

            fun processInput(name: MyFavsEvent) {
                inputEvents.accept(name)
            }
        }


