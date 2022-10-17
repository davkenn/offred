package com.example.renewed


 import androidx.lifecycle.ViewModel
 import com.example.renewed.models.MyEvent
 import com.jakewharton.rxrelay3.PublishRelay
 import dagger.hilt.android.lifecycle.HiltViewModel
 import io.reactivex.rxjava3.core.Flowable
 import io.reactivex.rxjava3.disposables.CompositeDisposable

 import javax.inject.Inject

    @HiltViewModel
    class FavoritesListVM @Inject constructor(
        private val repository: BaseFavoritesRepo
    ): ViewModel()
    {


        private val disposables: CompositeDisposable = CompositeDisposable()
        private val inputEvents: PublishRelay<MyEvent> = PublishRelay.create()
        val vs: Flowable<Int> =  repository.getCurrentState().share()
        /**
        val vs: Observable<FullViewState> = inputEvents.flatMap { it-> it }
        .doOnNext { Timber.d("---- Event is $it") }
        .eventToResult()
        .doOnNext { Timber.d("---- Result is $it") }
        .combineResults()
        .doOnNext { Timber.d("----Combined is $it") }
        .replay(1)
        .autoConnect(1){disposables.add(it)}
         **/
    }
