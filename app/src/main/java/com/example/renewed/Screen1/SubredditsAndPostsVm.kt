package com.example.renewed.Screen1

import androidx.lifecycle.ViewModel
import com.example.renewed.models.*
import com.example.renewed.repos.BaseSubredditsAndPostsRepo
import com.example.renewed.test.CountingIdleResource
import com.jakewharton.rxrelay3.PublishRelay
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.mergeAll
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber
import java.time.Instant
import javax.inject.Inject


@HiltViewModel
class SubredditsAndPostsVM @Inject constructor(
    private val repo: BaseSubredditsAndPostsRepo
): ViewModel() {

    private val disposables: CompositeDisposable = CompositeDisposable()
    private val inputEvents: PublishRelay<Screen1Event> = PublishRelay.create()

    init {
        Timber.d("oncleared in subsandpostsvm")
        disposables.add(repo.clearDisplayed().andThen(prefetch()).subscribeOn(Schedulers.io())
            .subscribeBy{processInput(Screen1Event.ScreenLoadEvent(""))})
    }

    fun processInput(name: Screen1Event) {
        inputEvents.accept(name)


    //    if (name is Screen1Event.ClickOnT5ViewEvent) CountingIdleResource.increment()

    }
    val vs: Observable<FullViewStateScreen1> = inputEvents
        .doOnNext { Timber.d("---- Event is $it") }
        .doOnNext {CountingIdleResource.increment() }
        .eventToResult()
        .doOnNext { Timber.d("---- Result is $it") }
        .combineResults()
        .doOnNext { Timber.d("----Combined is $it") }
        .replay(1)
        .autoConnect(1){disposables.add(it)}

    override fun onCleared() {
        super.onCleared()
        disposables.dispose()
    }

    private fun Observable<Screen1Event>.eventToResult(): Observable<PartialViewStateScreen1> {
        return publish {
            val a = Observable.fromArray(
                it.ofType(Screen1Event.ScreenLoadEvent::class.java).onScreenLoad(),
                it.ofType(Screen1Event.ClickOnT5ViewEvent::class.java).onClickT5(),
                it.ofType(Screen1Event.ClickOnT3ViewEvent::class.java).onClickT3(),
                it.ofType(Screen1Event.RemoveAllSubreddits::class.java).onRefreshList(),
                it.ofType(Screen1Event.UpdateViewingState::class.java).updateViewingState() ,
                it.ofType(Screen1Event.SaveEvent::class.java).onSave(),
                it.ofType(Screen1Event.ClearEffectEvent::class.java).onClear(),
                it.ofType(Screen1Event.MakeSnackBarEffect::class.java).onSnackbar()
            )
            a.mergeAll()
        }
    }

    private fun Observable<PartialViewStateScreen1>.combineResults(): Observable<FullViewStateScreen1> {

        return scan(FullViewStateScreen1()) { state, event ->
            when (event) {
                is PartialViewStateScreen1.T5ListForRV -> state.copy(
                                                        t5ListForRV = event, latestEvent5 = null,
                                                        latestEvent3 = null, effect = null)
                is PartialViewStateScreen1.T3ListForRV -> state.copy(
                                                        t3ListForRV = event, latestEvent5 = null,
                                                        latestEvent3 = null, effect = null)
                is PartialViewStateScreen1.T5ForViewing -> state.copy(
                                                        latestEvent5 = event, latestEvent3 = null,
                                                        effect = null)
                is PartialViewStateScreen1.T3ForViewing -> state.copy(
                                                        latestEvent3 = event, latestEvent5 = null,
                                                        effect = null)
                is PartialViewStateScreen1.NavigateBackEffect -> state.copy(
                                                        latestEvent3= null, latestEvent5 = null,
                                                        effect = Screen1Effect.DELETE_OR_SAVE)
                is PartialViewStateScreen1.ClearEffectEffect -> state.copy(effect = null)
                is PartialViewStateScreen1.SnackbarEffect -> state.copy(
                                                        effect=Screen1Effect.SNACKBAR,
                                                        latestEvent3 = null,latestEvent5=null)
            }
        }.skip(1)
    }

    private fun Observable<Screen1Event.ScreenLoadEvent>.onScreenLoad(): Observable<PartialViewStateScreen1> {
        return flatMapSingle { getSubredditList() }
    }

    private fun Observable<Screen1Event.RemoveAllSubreddits>.onRefreshList(): Observable<PartialViewStateScreen1> {
        return Observable.merge(
            flatMap{ Observable.just(PartialViewStateScreen1.T5ListForRV(null),PartialViewStateScreen1.T3ListForRV(null))},
            flatMap { getSubredditList(it.srList.lastOrNull()).toObservable()
                                                .startWith(prefetch()).subscribeOn(Schedulers.io()) })
    }

    private fun Observable<Screen1Event.ClickOnT3ViewEvent>.onClickT3(): Observable<PartialViewStateScreen1> {
        return flatMapSingle {
            repo.getPost(it.name)
                .subscribeOn(Schedulers.io())
                .map { x -> PartialViewStateScreen1.T3ForViewing(x.toViewState()) }
        }
    }

    private fun Observable<Screen1Event.UpdateViewingState>.updateViewingState(): Observable<PartialViewStateScreen1> {
        return Observable.merge(
            flatMap { Observable.just(PartialViewStateScreen1.T3ListForRV(null)) },
            flatMap {
                repo.updateSubreddits(srList= if (it.name == null) listOf() else listOf(it.name),
                          isDisplayedInAdapter = false, shouldToggleDisplayedColumnInDb = true)
                    .subscribeOn(Schedulers.io())
                    .andThen(Observable.just(PartialViewStateScreen1.NavigateBackEffect))
        })
    }

    private fun Observable<Screen1Event.SaveEvent>.onSave(): Observable<PartialViewStateScreen1> {
        return flatMap {
                    Observable.just(PartialViewStateScreen1.T5ListForRV(
                                     it.previousState.filter { x->x.name != it.targetedSubreddit }))
                              .startWith(repo.saveSubreddit(it.targetedSubreddit)
                             .subscribeOn(Schedulers.io()))
                        }
    }

    private fun Observable<Screen1Event.ClickOnT5ViewEvent>.onClickT5(): Observable<PartialViewStateScreen1> {
        return Observable.merge(
            flatMapSingle { clickOnT5Event ->
                repo.updateSubreddits(listOf( clickOnT5Event.name), isDisplayedInAdapter = false,
                                                         shouldToggleDisplayedColumnInDb = true)
                    .subscribeOn(Schedulers.io())
                    .andThen(repo.getPosts(clickOnT5Event.name)
                    .map { list -> list.map { x -> x.toViewState() }}
                    .map { x -> PartialViewStateScreen1.T3ListForRV(x) })
            },
            flatMapSingle {
                repo.getSubreddit(it.name)
                    .onErrorResumeWith(Single.just(RoomT5(name= "Oops! Somehow there's an error...",
                        description = "Either you have no internet connection"  +
                                       "or the site you seek no longer exists", displayName="ll",
                        created_utc = Instant.now(),timeLastAccessed = Instant.now(),
                                                    thumbnail = "", banner_img = "", subscribers=5))
                    .retry(1))
                    .subscribeOn(Schedulers.io())
                    .map { x -> PartialViewStateScreen1.T5ForViewing(x.toViewState()) }
            })
    }

    private fun Observable<Screen1Event.MakeSnackBarEffect>.onSnackbar()
                                : Observable<PartialViewStateScreen1> =
                                flatMap{Observable.just(PartialViewStateScreen1.SnackbarEffect)}


    private fun Observable<Screen1Event.ClearEffectEvent>.onClear()
                                        : Observable<PartialViewStateScreen1> =
                             flatMap{Observable.just(PartialViewStateScreen1.ClearEffectEffect)}

    private fun prefetch(): Completable =
        repo.deleteUninterestingSubreddits()
            .andThen(repo.prefetchSubreddits()
                .doOnError { Timber.e("----error getting subreddits ${it.stackTraceToString()}") }
                .onErrorComplete()
                .doOnComplete { Timber.d("---- done fetching subreddits") })
            .andThen(repo.prefetchPosts()
                .doOnError { Timber.e("----error getting posts ${it.stackTraceToString()}") }
                .onErrorComplete()
                .doOnComplete { Timber.d("---- done fetching posts") })

    private fun getSubredditList(lastOnPreviousPage:String?=null) = repo.getSubreddits(lastOnPreviousPage)
        .subscribeOn(Schedulers.io())
        .map { list -> list.map { x -> x.toViewState() } }
        .map { PartialViewStateScreen1.T5ListForRV(it) }

}
