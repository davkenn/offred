package com.example.renewed.Screen2


 import android.annotation.SuppressLint
 import androidx.lifecycle.ViewModel
 import com.example.renewed.Room.T3DAO
 import com.example.renewed.VIEWPAGER_PAGES_TOTAL
 import com.example.renewed.VP_PAGES_PER_LOAD
 import com.example.renewed.models.*
 import com.example.renewed.repos.BaseFavoritesRepo
 import com.jakewharton.rxrelay3.PublishRelay
 import dagger.hilt.android.lifecycle.HiltViewModel
 import io.reactivex.rxjava3.core.Completable
 import io.reactivex.rxjava3.core.Observable
 import io.reactivex.rxjava3.disposables.CompositeDisposable
 import io.reactivex.rxjava3.kotlin.addTo
 import io.reactivex.rxjava3.kotlin.mergeAll
 import io.reactivex.rxjava3.kotlin.withLatestFrom
 import io.reactivex.rxjava3.schedulers.Schedulers
 import timber.log.Timber


 import javax.inject.Inject

@SuppressLint("CheckResult")
@HiltViewModel
class FavoritesListVM @Inject constructor(private val favsRepo: BaseFavoritesRepo): ViewModel() {
    private var currentWindowLength: Observable<Int>
    private val currentlyDisplayedPosts: Observable<List<String>>?
    private val newPostsObservable: Observable<RoomT3>
    private val disposables: CompositeDisposable = CompositeDisposable()
    private val inputEvents: PublishRelay<Screen2Event> = PublishRelay.create()

    val vs: Observable<FullViewStateScreen2> = inputEvents
        .doOnNext { Timber.d("---- Event is $it") }
        .eventToResult()
        .doOnNext { Timber.d("---- Result is $it") }
        .combineResults()
        .doOnNext { Timber.d("----Combined is $it") }
        .replay(1)
        .autoConnect(1) { disposables.add(it) }

    private fun Observable<PartialViewStateScreen2>.combineResults(): Observable<FullViewStateScreen2> {
        return scan(FullViewStateScreen2()) { state, event ->
            when (event) {

                is PartialViewStateScreen2.LoadCompleteEffect -> state.copy(effect = Screen2Effect.LOAD)
                is PartialViewStateScreen2.DeleteCompleteEffect -> state.copy(effect = Screen2Effect.DELETE)
                is PartialViewStateScreen2.Posts -> state.copy(
                    currentlyDisplayedList = event,
                    effect = null
                )

                is PartialViewStateScreen2.Position -> state.copy(position = event, effect = null)
                is PartialViewStateScreen2.ClearEffectEffect -> state.copy(effect = null)
            }
        }.skip(1)
    }

    private fun Observable<Screen2Event>.eventToResult(): Observable<PartialViewStateScreen2> {
        return publish {
            val a = Observable.fromArray(
                it.ofType(Screen2Event.DeleteSubredditEvent::class.java).deleteThenReturn(),
                it.ofType(Screen2Event.AddSubredditsEvent::class.java)
                    .loadThenReturn(newPostsObservable),
                it.ofType(Screen2Event.ClearEffectEvent::class.java).clearEffect(),
                it.ofType(Screen2Event.UpdatePositionEvent::class.java).returnPosition(),
                it.ofType(Screen2Event.UpdateViewedPosts::class.java).returnPosts(),
                it.ofType(Screen2Event.LoadMoreEvent::class.java).handleLoadMore(newPostsObservable)
            )
            a.mergeAll()
        }
    }

    init {

        currentWindowLength = favsRepo.currentLength().replay(1).autoConnect(1){disposables.add(it)}
        currentlyDisplayedPosts = favsRepo.observeCurrentPostList().replay(1)
            .autoConnect(1) { disposables.add(it) }



        currentlyDisplayedPosts.subscribe { processInput(Screen2Event.UpdateViewedPosts(it)) }

        newPostsObservable = favsRepo.observeSavedSubreddits()
            .flatMap { Observable.fromIterable(it) }.flatMap {
                favsRepo.getPostsFromSavedSubreddit(it)
            }
            .share()


        newPostsObservable
                .take(VIEWPAGER_PAGES_TOTAL.toLong()-currentWindowLength.blockingFirst())
                .flatMapCompletable { x -> favsRepo.insert(x.name) }.subscribe()
    }

    private fun Observable<Screen2Event.UpdateViewedPosts>.returnPosts()
                                    : Observable<PartialViewStateScreen2> =
            map { PartialViewStateScreen2.Posts(it.newPosts) }

    private fun Observable<Screen2Event.ClearEffectEvent>.clearEffect()
                                    : Observable<PartialViewStateScreen2> =
            map { PartialViewStateScreen2.ClearEffectEffect }


    private fun Observable<Screen2Event.DeleteSubredditEvent>.deleteThenReturn()
                                    : Observable<PartialViewStateScreen2> {
        return flatMap { favsRepo.deletePages(it.targets)
                .subscribeOn(Schedulers.io())
            .andThen(
                Observable.just(PartialViewStateScreen2.DeleteCompleteEffect))
        }
    }

    private fun Observable<Screen2Event.UpdatePositionEvent>.returnPosition()
                                    : Observable<PartialViewStateScreen2> =
            map{PartialViewStateScreen2.Position(it.newPosition)}

    private fun Observable<Screen2Event.AddSubredditsEvent>.loadThenReturn(arg:Observable<RoomT3>)
                                    : Observable<PartialViewStateScreen2> {
                     return flatMap { arg.take(it.count.toLong())
                                         .flatMapCompletable { favsRepo.insert(it.name)
                                             .subscribeOn(Schedulers.io()) }
                         .andThen(Observable.just(PartialViewStateScreen2.LoadCompleteEffect))
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

    private fun Observable<Screen2Event.LoadMoreEvent>.handleLoadMore(
        newPostsObservable: Observable<RoomT3>
    ): Observable<PartialViewStateScreen2> {
        return switchMap { event -> // 'event' is the LoadMoreEvent
            // Start the chain by deleting the pages passed in the event.
            favsRepo.deletePages(event.targets)
                // AFTER deletion is complete, proceed with the next step.
                .andThen(
                    // We need to get a snapshot of the list *after* deletion.
                    currentlyDisplayedPosts!!.take(1)
                        .flatMap { currentList ->
                            // Filter the endless stream of new posts to find ones not already in our list.
                            newPostsObservable.filter { newPost ->
                                newPost.name !in currentList
                            }
                        }
                        // Take the required number of new, unique posts.
                        .distinct { it.name }
                        .take(VP_PAGES_PER_LOAD.toLong())
                        // Now, insert each of these new posts into the database.
                        .flatMapCompletable { newPost ->
                            favsRepo.insert(newPost.name)
                        }
                ).andThen(

                    Observable.just(

                        PartialViewStateScreen2.LoadCompleteEffect
                    )
                )
                // Ensure all database operations run on a background thread.
                .subscribeOn(Schedulers.io())
        }
    }}



