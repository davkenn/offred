package com.example.renewed.Screen1

import androidx.lifecycle.ViewModel
import com.example.renewed.models.*
import com.example.renewed.repos.BaseSubredditsAndPostsRepo
import com.jakewharton.rxrelay3.PublishRelay
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.mergeAll
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber
import java.time.Instant
import javax.inject.Inject


@HiltViewModel
class SubredditsAndPostsVM @Inject constructor(
    private val repo: BaseSubredditsAndPostsRepo
): ViewModel() {

    private val disposables: CompositeDisposable = CompositeDisposable()
    private val inputEvents: PublishRelay<MyEvent> = PublishRelay.create()

    init {
        repo.login()
        disposables.add(repo.clearDisplayed().subscribe())
    }

    fun processInput(name: MyEvent) {
        inputEvents.accept(name)
    }
    val vs: Observable<FullViewStateScreen1> = inputEvents
        .doOnNext { Timber.d("---- Event is $it") }
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

    private fun Observable<MyEvent>.eventToResult(): Observable<PartialViewState> {
        return publish {
            val a = Observable.fromArray(
                it.ofType(MyEvent.ScreenLoadEvent::class.java).onScreenLoad(),
                it.ofType(MyEvent.ClickOnT5ViewEvent::class.java).onClickT5(),
                it.ofType(MyEvent.ClickOnT3ViewEvent::class.java).onClickT3(),
                it.ofType(MyEvent.RemoveAllSubreddits::class.java).onRefreshList(),
                it.ofType(MyEvent.UpdateViewingState::class.java).updateViewingState() ,
                it.ofType(MyEvent.SaveOrDeleteEvent::class.java).onSaveOrDelete(),
                it.ofType(MyEvent.ClearEffectEvent::class.java).onClear(),
                it.ofType(MyEvent.MakeSnackBarEffect::class.java).onSnackbar()
            )
            a.mergeAll()
        }
    }

    private fun Observable<PartialViewState>.combineResults(): Observable<FullViewStateScreen1> {

        return scan(FullViewStateScreen1()) { state, event ->
            when (event) {
                is PartialViewState.T5ListForRV -> state.copy(
                                                        t5ListForRV = event, latestEvent5 = null,
                                                        latestEvent3 = null, effect = null)
                is PartialViewState.T3ListForRV -> state.copy(
                                                        t3ListForRV = event, latestEvent5 = null,
                                                        latestEvent3 = null, effect = null)
                is PartialViewState.T5ForViewing -> state.copy(
                                                        latestEvent5 = event, latestEvent3 = null,
                                                        effect = null)
                is PartialViewState.T3ForViewing -> state.copy(
                                                        latestEvent3 = event, latestEvent5 = null,
                                                        effect = null)
                is PartialViewState.NavigateBackEffect -> state.copy(
                                                        latestEvent3= null, latestEvent5 = null,
                                                        effect = EffectType.DELETE_OR_SAVE)
                is PartialViewState.ClearEffectEffect -> state.copy(
                                                                            effect = null)
                is PartialViewState.SnackbarEffect -> state.copy(
                                                        effect=EffectType.SNACKBAR,
                                                            latestEvent3 = null,latestEvent5=null)
            }
        }.skip(1)
    }

    private fun Observable<MyEvent.ScreenLoadEvent>.onScreenLoad(): Observable<PartialViewState> {
        return  Observable.merge(
                                flatMapSingle {
                                                repo.getSubreddits()
                                                    .subscribeOn(Schedulers.io())
                                                    .map { list -> list.map { x->x.toViewState() } }
                                                    .map { PartialViewState.T5ListForRV(it) }
                                              },
                                flatMapSingle {
                                                repo.getPosts(it.name ?: "")
                                                    .subscribeOn(Schedulers.io())
                                                    .map { list -> list.map { x -> x.toViewState() } }
                                                    .map { x -> PartialViewState.T3ListForRV(x) }
                                               })
    }

    private fun Observable<MyEvent.RemoveAllSubreddits>.onRefreshList(): Observable<PartialViewState> {
        return Observable.merge(
            flatMap{ Observable.just(PartialViewState.T3ListForRV(null))},
            flatMap {
               repo.getSubreddits(it.srList.lastOrNull()).toObservable()

                   .map { list -> list.map { it.toViewState() } }
                   .map { PartialViewState.T5ListForRV(it) }
                   .startWith(prefetch()).subscribeOn(Schedulers.io())
           })
    }

    private fun Observable<MyEvent.ClickOnT3ViewEvent>.onClickT3(): Observable<PartialViewState> {
        return flatMapSingle {
            repo.getPost(it.name)
                .subscribeOn(Schedulers.io())
                .map { x -> PartialViewState.T3ForViewing(x.toViewState()) }
        }
    }

    private fun Observable<MyEvent.UpdateViewingState>.updateViewingState(): Observable<PartialViewState> {
        return Observable.merge(
            flatMap { Observable.just(PartialViewState.T3ListForRV(null)) },
            flatMap {
                repo.updateSubreddits(srList=
                    if (it.name == null) listOf() else listOf(it.name),
                    isDisplayedInAdapter = false, shouldToggleDisplayedColumnInDb = true)
                    .subscribeOn(Schedulers.io())
                    .andThen(Observable.just(PartialViewState.NavigateBackEffect))
        })
    }

    private fun Observable<MyEvent.SaveOrDeleteEvent>.onSaveOrDelete(): Observable<PartialViewState> {
        return flatMap {
                    Observable.just(PartialViewState.T5ListForRV(
                                     it.previousState.filter { x->x.name != it.targetedSubreddit }))
                              .startWith(repo.deleteOrSaveSubreddit(it.targetedSubreddit, it.shouldDelete)
                             .subscribeOn(Schedulers.io()))
                        }
    }

    private fun Observable<MyEvent.ClickOnT5ViewEvent>.onClickT5(): Observable<PartialViewState> {
        return Observable.merge(
            flatMapSingle { clickOnT5Event ->
                repo.updateSubreddits(listOf( clickOnT5Event.name), isDisplayedInAdapter = false,
                                                         shouldToggleDisplayedColumnInDb = true)
                    .subscribeOn(Schedulers.io())
                    .andThen(repo.getPosts(clickOnT5Event.name)
                    .map { list -> list.map { x -> x.toViewState() }}
                    .map { x -> PartialViewState.T3ListForRV(x) })
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
                    .map { x -> PartialViewState.T5ForViewing(x.toViewState()) }
            })
    }

    fun prefetch(): Completable =
        repo.deleteUninterestingSubreddits()
            .andThen(repo.prefetchSubreddits()
                .doOnError { Timber.e("----error getting subreddits ${it.stackTraceToString()}") }
                .onErrorComplete()
                .doOnComplete { Timber.d("---- done fetching subreddits") })
            .andThen(repo.prefetchPosts()
                .doOnError { Timber.e("----error getting posts ${it.stackTraceToString()}") }
                .onErrorComplete()
                .doOnComplete { Timber.d("---- done fetching posts") })

    private fun Observable<MyEvent.MakeSnackBarEffect>.onSnackbar(): Observable<PartialViewState> {
        return flatMap{Observable.just(PartialViewState.SnackbarEffect)}
    }

    private fun Observable<MyEvent.ClearEffectEvent>.onClear(): Observable<PartialViewState> {
        return flatMap{Observable.just(PartialViewState.ClearEffectEffect)}
    }
}
