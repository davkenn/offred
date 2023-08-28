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
class FavoritesListVM @Inject constructor(private val favsRepo: BaseFavoritesRepo): ViewModel() {
    private val currentlyDisplayedPosts: Observable<List<String>>?
    private val newPostsObservable:Observable<RoomT3>
    private val disposables: CompositeDisposable = CompositeDisposable()
    private val inputEvents: PublishRelay<Screen2Event> = PublishRelay.create()

    val vs: Observable<FullViewStateScreen2> = inputEvents
        .doOnNext { Timber.d("---- Event is $it") }
        .eventToResult()
        .doOnNext { Timber.d("---- Result is $it") }
        .combineResults()
        .doOnNext { Timber.d("----Combined is $it") }
        .replay(1)
        .autoConnect(1){disposables.add(it)}

    private fun Observable<PartialViewStateScreen2>.combineResults(): Observable<FullViewStateScreen2> {
        return scan(FullViewStateScreen2()) { state, event ->
            when (event) {
                is PartialViewStateScreen2.LoadCompleteEffect -> state.copy(effect = Screen2Effect.LOAD)
                is PartialViewStateScreen2.DeleteCompleteEffect -> state.copy(effect = Screen2Effect.DELETE)
                is PartialViewStateScreen2.Posts -> state.copy(currentlyDisplayedList = event,effect=null)
                is PartialViewStateScreen2.Position -> state.copy(position = event,effect=null)
                is PartialViewStateScreen2.ClearEffectEffect -> state.copy(effect=null)
            }
        }.skip(1)
    }

    private fun Observable<Screen2Event>.eventToResult(): Observable<PartialViewStateScreen2> {
        return publish { val a = Observable.fromArray(
            it.ofType(Screen2Event.DeleteSubredditEvent::class.java).deleteThenReturn(),
            it.ofType(Screen2Event.AddSubredditsEvent::class.java).loadThenReturn(newPostsObservable),
            it.ofType(Screen2Event.ClearEffectEvent::class.java).clearEffect(),
            it.ofType(Screen2Event.UpdatePositionEvent::class.java).returnPosition(),
            it.ofType(Screen2Event.UpdateViewedPosts::class.java).returnPosts())
            a.mergeAll()
            }
        }

    init {
        newPostsObservable = favsRepo.observeSavedSubreddits()
            .flatMap { x ->
                Observable.just(Unit).repeat(10)
                    .map { x.shuffled().first() }
            }
            .flatMap { favsRepo.getRandomPosts(it.displayName, 2) }
            .share()
        //okhttp.OkHttpClient: <-- HTTP FAILED: java.io.IOException: Canceled
        //get this error on loads because take ends the stream early. But it works.
        newPostsObservable.take(VIEWPAGER_PAGES_TOTAL.toLong())
            .flatMapCompletable { x -> favsRepo.insert(x.name) }
            .startWith(
                favsRepo.clearPages().subscribeOn(Schedulers.io())
            )
            .subscribe(
                { Timber.d("observ") },
                { Timber.e("error: ${it.localizedMessage}") })
            .addTo(disposables)

        currentlyDisplayedPosts = favsRepo.observeCurrentPostList().replay(1)
            .autoConnect(1) { disposables.add(it) }

        currentlyDisplayedPosts.subscribe { processInput(Screen2Event.UpdateViewedPosts(it)) }
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
}


