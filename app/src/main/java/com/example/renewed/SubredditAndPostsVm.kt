package com.example.renewed

import androidx.lifecycle.ViewModel
import com.example.renewed.models.*
import com.example.renewed.models.FullViewState
import com.example.renewed.models.MyEvent
import com.example.renewed.models.MyViewState
import com.jakewharton.rxrelay3.PublishRelay
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable

import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.mergeAll
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import timber.log.Timber
import javax.inject.Inject


@HiltViewModel
class SubredditsAndPostsVM @Inject constructor(
    private val repository: BaseSubredditsAndPostsRepo
): ViewModel() {



    private val disposables: CompositeDisposable = CompositeDisposable()
    private val inputEvents: PublishRelay<MyEvent> = PublishRelay.create()

    val vs: Observable<FullViewState> = inputEvents
        .doOnNext { Timber.d("---- Event is $it")}
        .eventToResult()
        //     .share()
        .doOnNext { Timber.d("---- Result is $it")}
        .combineResults()
        .replay(1)
        .autoConnect(1)
        {upstream -> upstream.addTo(disposables) }

    fun prefetch(): Completable =


        repository.deleteUninterestingSubreddits()
            .andThen(repository.prefetchSubreddits()
                .doOnComplete { Timber.d("---- done fetching subreddits") }
                .doOnError { Timber.e("----error getting subreddits ${it.stackTraceToString()}") })
            .andThen(repository.prefetchPosts())
                .doOnError { Timber.e("----error getting posts") }
                .doOnComplete { Timber.d("---- done fetching posts") }

            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())


    fun processInput(name: MyEvent) {
        inputEvents.accept(name)
    }





    private fun Observable<MyEvent>.eventToResult(): Observable<MyViewState> {

        return publish { o ->
            var a = Observable.fromArray(
                o.ofType(MyEvent.ScreenLoadEvent::class.java).onScreenLoad(),
                o.ofType(MyEvent.ClickOnT5ViewEvent::class.java).onClickT5(),
                o.ofType(MyEvent.ClickOnT3ViewEvent::class.java).onClickT3(),
                o.ofType(MyEvent.RemoveAllSubreddits::class.java).onRefreshList(),
                o.ofType(MyEvent.BackOrDeletePressedEvent::class.java).onBackDeletePressed())
            a.mergeAll()
        } }



    private fun Observable<MyViewState>.combineResults(): Observable<FullViewState> {

        return scan(FullViewState()) { state, event ->
            when (event) {
                is MyViewState.T5ListForRV -> state.copy(t5ListForRV = event,latestEvent5 = null,
                                                    latestEvent3 = null, eventProcessed = false)
                is MyViewState.T3ListForRV -> state.copy(t3ListForRV = event,latestEvent5 = null,
                                                    latestEvent3 = null, eventProcessed = false)
                is MyViewState.T5ForViewing -> state.copy(latestEvent5= event,latestEvent3 = null,
                                                                            eventProcessed = false)
                is MyViewState.T3ForViewing -> state.copy(latestEvent3 = event,latestEvent5 = null,
                                                                            eventProcessed = false)
                is MyViewState.NavigateBack -> state.copy(latestEvent3 = null, latestEvent5 = null,
                                                                            eventProcessed = true )
            }
//this makes it so cant click same view twice, but still can click on another then this one
        }//.distinctUntilChanged()
    }

    private fun Observable<MyEvent.ScreenLoadEvent>.onScreenLoad(): Observable<MyViewState> {

         return        Observable.merge(
                    flatMapSingle {
                       repository.getSubreddits()
                           .subscribeOn(Schedulers.io())
                           .map { list -> list.map { it.toViewState() } }
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

            flatMap{ _ -> Observable.just(MyViewState.T3ListForRV(null))},
//TODO i had got this down to one subscribeon now its back up to 2 when added to observable and changed rx method
            flatMap{
                    repository.getSubreddits().toObservable().subscribeOn(Schedulers.io())
                        .map { list -> list.map { it.toViewState() } }
                        .map { MyViewState.T5ListForRV(it)  }//.subscribeOn(Schedulers.io())
                        .startWith(repository.updateSubreddits(it.srList,false)
                                              .andThen(prefetch()).subscribeOn(Schedulers.io()))
                     }
        )


    }

    private fun Observable<MyEvent.ClickOnT3ViewEvent>.onClickT3(): Observable<MyViewState> {

        return flatMapSingle{repository.getPost(it.name)
            .subscribeOn(Schedulers.io())
            .map {  x -> MyViewState.T3ForViewing(x.toViewState())}   }
    }


    private fun Observable<MyEvent.BackOrDeletePressedEvent>.onBackDeletePressed(): Observable<MyViewState> {

        return flatMap {
            repository.updateSubreddits(if (it.name==null) listOf() else listOf(it.name),
                                                                    true, it.shouldDelete)
                .subscribeOn(Schedulers.io())
                .andThen(Observable.just(MyViewState.NavigateBack))}

        }


    private fun Observable<MyEvent.ClickOnT5ViewEvent>.onClickT5(): Observable<MyViewState> {
    return Observable.merge(
                flatMapSingle {
                    repository.updateSubreddits(listOf(it.name),true)
                        .subscribeOn(Schedulers.io())
                        .andThen(repository.getPosts(it.name)
                              .map { list -> list.map { x -> x.toViewState() }}
                              .map { x -> MyViewState.T3ListForRV(x) })},
                flatMapSingle {
                    repository.getSubreddit(it.name).subscribeOn(Schedulers.io())
                              .map { x -> MyViewState.T5ForViewing(x.toViewState()) }
            })
        }




        override fun onCleared() {
            super.onCleared()
            disposables.dispose()
        }
}

