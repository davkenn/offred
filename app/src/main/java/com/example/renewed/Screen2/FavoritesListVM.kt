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

        val eventCompleteEvent: Observable<Screen2Effect>
        val newPostsObservable:Observable<RoomT3>
        val currentlyDisplayedPosts: Observable<List<String>>
        val currentPosition: Observable<Int>
        private val disposables: CompositeDisposable = CompositeDisposable()
        private val inputEvents: PublishRelay<Screen2Event> = PublishRelay.create()


        val vs: Observable<PartialViewStateScreen2> = inputEvents.eventToResult()   .replay(1)
            .autoConnect(1){disposables.add(it)}

        private fun Observable<Screen2Event>.eventToResult(): Observable<PartialViewStateScreen2> {
            return publish {
                val a = Observable.fromArray(
                    it.ofType(Screen2Event.DeleteSubredditEvent::class.java).deleteThenReturn(),
                    it.ofType(Screen2Event.AddSubredditsEvent::class.java).loadThenReturn(newPostsObservable),
      //              it.ofType(Screen2Event.UpdatePositionEvent::class.java).returnPosition()
                )
                a.mergeAll()
            }
        }
        init {
            newPostsObservable = favsRepo.observeSavedSubreddits()
                .flatMap { x -> Observable.just(Unit).repeat(10)
                                .map { x.shuffled().first()}
                         }
                .flatMap { favsRepo.getRandomPosts(it.displayName, 2) }
                .share()//Do I need this share? seems to work ok without it.

//I/okhttp.OkHttpClient: <-- HTTP FAILED: java.io.IOException: Canceled is caused by this
// take operation which ends the stream early
            newPostsObservable.take(VIEWPAGER_PAGES_TOTAL.toLong())
                              .flatMapCompletable { x -> favsRepo.insert(x.name) }
                              .startWith(favsRepo.clearPages()
                              .subscribeOn(Schedulers.io()))
                              .subscribe({ Timber.d("observ" ) },
                                          { Timber.e("error: ${it.localizedMessage}") })
                              .addTo(disposables)

            currentlyDisplayedPosts = favsRepo.observeCurrentPostList().replay(1)
                .autoConnect(1) { disposables.add(it) }

            currentPosition = inputEvents.publish { it.ofType(Screen2Event.UpdatePositionEvent::class.java) }
                .map { it.newPosition }
                .replay(1)
                .autoConnect(1) { disposables.add(it) }

            eventCompleteEvent = inputEvents.publish {
                val a = Observable.fromArray(
                    it.ofType(Screen2Event.DeleteSubredditEvent::class.java).deleteThenReturn2(),
                    it.ofType(Screen2Event.AddSubredditsEvent::class.java).loadThenReturn2(newPostsObservable))
                a.mergeAll()
                }
            }

            private fun Observable<Screen2Event.DeleteSubredditEvent>.deleteThenReturn()
                                                      : Observable<PartialViewStateScreen2> {
                return flatMap {
                    favsRepo.deletePages(it.targets)
                        .subscribeOn(Schedulers.io())
                        .andThen(Observable.just(PartialViewStateScreen2.DeleteCompleteEffect))
                }
            }


        private fun Observable<Screen2Event.UpdatePositionEvent>.returnPosition()
                : Observable<PartialViewStateScreen2> =
            map{PartialViewStateScreen2.Position(it.newPosition)}




        private fun Observable<Screen2Event.DeleteSubredditEvent>.deleteThenReturn2() : Observable<Screen2Effect> {
            return flatMap {
                favsRepo.deletePages(it.targets)
                    .subscribeOn(Schedulers.io())
                    .andThen(Observable.just(Screen2Effect.DELETE))
            }
        }

            private fun Observable<Screen2Event.AddSubredditsEvent>.loadThenReturn(arg:Observable<RoomT3>)
                                                    : Observable<PartialViewStateScreen2> {
                     return flatMap { arg.take(it.count.toLong())
                                         .flatMapCompletable { favsRepo.insert(it.name)
                                                                       .subscribeOn(Schedulers.io())
                                          }
                         .andThen(Observable.just(PartialViewStateScreen2.DeleteCompleteEffect))
                                     }
            }
        private fun Observable<Screen2Event.AddSubredditsEvent>.loadThenReturn2(arg:Observable<RoomT3>) : Observable<Screen2Effect> {
            return flatMap { arg.take(it.count.toLong())
                .flatMapCompletable { favsRepo.insert(it.name)
                    .subscribeOn(Schedulers.io())
                }
                .andThen(Observable.just(Screen2Effect.LOAD))
            }
        }


        override fun onCleared() {
                super.onCleared()
                Timber.d("oncleared in favslistvm")
                disposables.dispose()
            }

            fun processInput(name: Screen2Event) {
                inputEvents.accept(name)
            }
        }


