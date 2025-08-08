package com.example.offred.Screen2


 import android.annotation.SuppressLint
 import androidx.lifecycle.ViewModel
 import com.example.offred.VIEWPAGER_PAGES_TOTAL
 import com.example.offred.VP_PAGES_PER_LOAD
 import com.example.offred.models.*
 import com.example.offred.repos.BaseFavoritesRepo
 import com.jakewharton.rxrelay3.PublishRelay
 import dagger.hilt.android.lifecycle.HiltViewModel
 import io.reactivex.rxjava3.core.Observable
 import io.reactivex.rxjava3.disposables.CompositeDisposable
 import io.reactivex.rxjava3.kotlin.mergeAll
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


                is PartialViewStateScreen2.LoadStartedEffect -> state.copy(effect = Screen2Effect.LOAD)
                is PartialViewStateScreen2.Posts -> state.copy(currentlyDisplayedList = event)
//is this a bug with order of show loading and hide loading? should I keep effect when position updated? position when effect is updated?
                is PartialViewStateScreen2.Position -> state.copy(position = event)

            }
        }.skip(1)
    }

    private fun Observable<Screen2Event>.eventToResult(): Observable<PartialViewStateScreen2> {
        return publish {
            val a = Observable.fromArray(
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


    private fun Observable<Screen2Event.UpdatePositionEvent>.returnPosition()
                                    : Observable<PartialViewStateScreen2> =
            map{PartialViewStateScreen2.Position(it.newPosition)}



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
                ).startWith(Observable.just(PartialViewStateScreen2.LoadStartedEffect))
                .subscribeOn(Schedulers.io())}

    }}



