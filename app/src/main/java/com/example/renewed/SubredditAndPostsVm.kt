package com.example.renewed

import androidx.lifecycle.ViewModel
import com.example.renewed.models.*
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
    private val repository: BaseSubredditsAndPostsRepo
): ViewModel() {


    private val disposables: CompositeDisposable = CompositeDisposable()
    private val inputEvents: PublishRelay<MyEvent> = PublishRelay.create()

    val vs: Observable<FullViewState> = inputEvents
        .doOnNext { Timber.d("---- Event is $it") }
        .eventToResult()
        .doOnNext { Timber.d("---- Result is $it") }
        .combineResults()
        .replay(1)
        .autoConnect(1)

        //.share()



    fun processInput(name: MyEvent) {
        inputEvents.accept(name)
    }

    fun prefetch(): Completable =

        repository.deleteUninterestingSubreddits()
            .andThen(repository.prefetchSubreddits()
                                .retry(0)
                                .onErrorResumeNext {repository.prefetchDefaultSubreddits() }
                                .doOnComplete { Timber.d("---- done fetching subreddits") }
                                .doOnError { Timber.e("----error getting subreddits ${it.stackTraceToString()}") })
            .andThen(repository.prefetchPosts())
            .retry(0)
            .onErrorResumeNext {repository.prefetchDefaultPosts() }
                                .doOnComplete { Timber.d("---- done fetching posts") }
                                .doOnError { Timber.e("----error getting posts") }

            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())


    private fun Observable<MyEvent>.eventToResult(): Observable<MyViewState> {

        return publish { o ->
            val a = Observable.fromArray(
                o.ofType(MyEvent.ScreenLoadEvent::class.java).onScreenLoad(),
                o.ofType(MyEvent.ClickOnT5ViewEvent::class.java).onClickT5(),
                o.ofType(MyEvent.ClickOnT3ViewEvent::class.java).onClickT3(),
                o.ofType(MyEvent.RemoveAllSubreddits::class.java).onRefreshList(),
                o.ofType(MyEvent.UpdateViewingState::class.java).updateViewingState() ,
                o.ofType(MyEvent.SaveOrDeleteEvent::class.java).onSaveOrDelete()
            )

            a.mergeAll()
        }
    }


    private fun Observable<MyViewState>.combineResults(): Observable<FullViewState> {

        return scan(FullViewState()) { state, event ->
            when (event) {
                is MyViewState.T5ListForRV -> state.copy(
                    t5ListForRV = event, latestEvent5 = null,
                    latestEvent3 = null, eventProcessed = false
                )
                is MyViewState.T3ListForRV -> state.copy(
                    t3ListForRV = event, latestEvent5 = null,
                    latestEvent3 = null, eventProcessed = false
                )
                is MyViewState.T5ForViewing -> state.copy(
                    latestEvent5 = event, latestEvent3 = null,
                    eventProcessed = false
                )
                is MyViewState.T3ForViewing -> state.copy(
                    latestEvent3 = event, latestEvent5 = null,
                    eventProcessed = false
                )
                is MyViewState.NavigateBack -> state.copy(
                    latestEvent3 = null, latestEvent5 = null,
                    eventProcessed = true
                )
            }
        }
    }

    private fun Observable<MyEvent.ScreenLoadEvent>.onScreenLoad(): Observable<MyViewState> {
//TODO i need to start if its null by clearing the displayed status

      return  Observable.merge(
            flatMapSingle {

                repository.getSubreddits()
                    .subscribeOn(Schedulers.io())
                    .map { list -> list.map { x->x.toViewState() } }
                    .map { MyViewState.T5ListForRV(it) }
            },
            flatMapSingle {

                repository.getPosts(it.name ?: "")
                    .subscribeOn(Schedulers.io())
                    .map { list -> list.map { x -> x.toViewState() } }
                    .map { x -> MyViewState.T3ListForRV(x) }
    })
    }

    //TODO bug where isDisplayed is true for some items not in the display list do I need to catch
    //deletes when process dies or just clear when start app?
    private fun Observable<MyEvent.RemoveAllSubreddits>.onRefreshList(): Observable<MyViewState> {

        return Observable.merge(
            flatMap{ Observable.just(MyViewState.T3ListForRV(null))},
           flatMap {
               //TODO isn't last good enough because I assume they are in order?
                repository.getSubreddits(it.srList.lastOrNull()).toObservable().subscribeOn(Schedulers.io())
                    .map { list -> list.map { it.toViewState() } }
                    .map { MyViewState.T5ListForRV(it) }
                    .startWith(
                        prefetch().subscribeOn(Schedulers.io()))})
            }


    private fun Observable<MyEvent.ClickOnT3ViewEvent>.onClickT3(): Observable<MyViewState> {

        return flatMapSingle {
            repository.getPost(it.name)
                .subscribeOn(Schedulers.io())
                .map { x -> MyViewState.T3ForViewing(x.toViewState()) }
        }
    }

    private fun Observable<MyEvent.UpdateViewingState>.updateViewingState(): Observable<MyViewState> {
        return Observable.merge(
            flatMap { Observable.just(MyViewState.T3ListForRV(null)) },
            flatMap {
                repository.updateSubreddits(srList=
                    if (it.name == null) listOf() else listOf(it.name),
                    isDisplayedInAdapter = false, shouldToggleDisplayedColumnInDb = true
                )
                .subscribeOn(Schedulers.io())

                .andThen(Observable.just(MyViewState.NavigateBack))
        })
    }

    private fun Observable<MyEvent.SaveOrDeleteEvent>.onSaveOrDelete(): Observable<MyViewState> {


        return flatMap{
                repository.deleteOrSaveSubreddit( it.targetedSubreddit, it.shouldDelete).subscribeOn(Schedulers.io())
                    .andThen {prefetch().subscribeOn(Schedulers.io())}
                    .andThen( Observable.just(MyViewState.T3ListForRV(null)) )}


                 //TODO really this has no place here bc im doing it elsewhere
         }


    private fun Observable<MyEvent.ClickOnT5ViewEvent>.onClickT5(): Observable<MyViewState> {

    return Observable.merge(
        flatMapSingle { clickOnT5Event ->
              repository.updateSubreddits(listOf( clickOnT5Event.name), isDisplayedInAdapter = false,
                                                            shouldToggleDisplayedColumnInDb = true)
                        .subscribeOn(Schedulers.io())
                        .andThen(repository.getPosts(clickOnT5Event.name)
                        .map { list -> list.map { x -> x.toViewState() }}
                        .map { x -> MyViewState.T3ListForRV(x) })},
        flatMapSingle {
              repository.getSubreddit(it.name)
                        .onErrorResumeWith(Single.just(RoomT5(name= "Oops! Somehow there's an error...",
                                                        description = "Either you have no internet connection"  +
                                                      "or the site you seek no longer exists", displayName="ll",
                                                    created_utc = Instant.now(),timeLastAccessed = Instant.now(),
                                                        thumbnail = "", banner_img = "", subscribers=5))
                        .retry(1))
                .subscribeOn(Schedulers.io())
                .map { x -> MyViewState.T5ForViewing(x.toViewState()) }})
        }


        override fun onCleared() {
            super.onCleared()
            disposables.dispose()
                }

}

