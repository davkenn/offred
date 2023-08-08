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
 import io.reactivex.rxjava3.kotlin.mergeAll
 import io.reactivex.rxjava3.schedulers.Schedulers
 import timber.log.Timber

 import javax.inject.Inject

    @HiltViewModel
    class FavoritesListVM @Inject constructor(
        private val favsRepo: BaseFavoritesRepo
    ): ViewModel() {
        val eventCompleteEvent: Observable<EffectType2>
        val newPostsObservable:Observable<RoomT3>
        val currentlyDisplayedPosts: Observable<List<String>>
        val currentPosition: Observable<Int>
        private val disposables: CompositeDisposable = CompositeDisposable()
        private val inputEvents: PublishRelay<MyFavsEvent> = PublishRelay.create()

        init {

            //TODO load fewer posts at a time
            //TODO fix I/okhttp.OkHttpClient: <-- HTTP FAILED: java.io.IOException: Canceled
            //maybe it has something to do with subscribing wrongly
            newPostsObservable = favsRepo.observeSavedSubreddits()
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

            eventCompleteEvent = inputEvents.publish {
                val a = Observable.fromArray(
                    it.ofType(MyFavsEvent.DeleteSubredditEvent::class.java).deleteThenReturn(),
                    it.ofType(MyFavsEvent.AddSubredditsEvent::class.java).loadThenReturn(newPostsObservable))
                a.mergeAll()
                }
            }

            private fun Observable<MyFavsEvent.DeleteSubredditEvent>.deleteThenReturn() : Observable<EffectType2> {
                return flatMap {
                    favsRepo.deletePages(it.targets)
                        .subscribeOn(Schedulers.io())
                        .andThen(Observable.just(EffectType2.DELETE))
                }
            }


            private fun Observable<MyFavsEvent.AddSubredditsEvent>.loadThenReturn(arg:Observable<RoomT3>) : Observable<EffectType2> {
                     return flatMap { arg.take(it.count.toLong())
                                         .flatMapCompletable { favsRepo.insert(it.name)
                                                                       .subscribeOn(Schedulers.io())
                                          }
                                          .andThen(Observable.just(EffectType2.LOAD))
                                     }
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


