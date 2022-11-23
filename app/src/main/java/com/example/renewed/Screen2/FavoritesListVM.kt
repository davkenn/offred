package com.example.renewed.Screen2


 import androidx.lifecycle.ViewModel
 import com.example.renewed.Room.FavoritesDAO
 import com.example.renewed.repos.BaseFavoritesRepo
 import com.example.renewed.models.MyFavsEvent
 import com.example.renewed.models.RoomT5
 import com.jakewharton.rxrelay3.PublishRelay
 import dagger.hilt.android.lifecycle.HiltViewModel
 import io.reactivex.rxjava3.core.Observable
 import io.reactivex.rxjava3.disposables.CompositeDisposable
 import io.reactivex.rxjava3.kotlin.addTo
 import io.reactivex.rxjava3.kotlin.mergeAll
 import timber.log.Timber
 import java.util.concurrent.TimeUnit

 import javax.inject.Inject

    @HiltViewModel
    class FavoritesListVM @Inject constructor(
        private val repository: BaseFavoritesRepo
    ): ViewModel() {
        val vs: Observable<List<String>>
        private val disposables: CompositeDisposable = CompositeDisposable()
        private val inputEvents: PublishRelay<MyFavsEvent> = PublishRelay.create()
        init {


            //TODO i need a delete button to make this really worthwhile
            repository.observeSavedSubreddits()
                //have to delete in here before make sublist
                .map { it.shuffled().take(4) }.flatMapIterable { it }
                .flatMap{ repository.getRandomPosts(it.displayName,4) }
                    //TODO need to also save it to the db here

                .doOnNext {
                    repository.insert(it.name).subscribe({},
                        { Timber.e("dberror: ${it.localizedMessage}") }).addTo(disposables)
                }

                .subscribe({
                    Timber.d("observ" + it.url)
                },
                    { Timber.e("observeerror: ${it.localizedMessage}") }).addTo(disposables)

            vs = repository.observeCurrentPostList()        .replay(1)
                .autoConnect(1){disposables.add(it)}


        }

        override fun onCleared() {
            super.onCleared()
            disposables.dispose()
        }




        fun processInput(name: MyFavsEvent) {

            inputEvents.accept(name)
        }
    }