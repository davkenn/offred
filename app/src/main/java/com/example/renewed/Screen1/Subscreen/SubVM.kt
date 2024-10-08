package com.example.renewed.Screen1.Subscreen

import androidx.lifecycle.ViewModel
import com.example.renewed.repos.BaseSubredditsAndPostsRepo
import com.example.renewed.models.PartialViewStateScreen1
import com.example.renewed.models.RoomT5
import com.example.renewed.models.toViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Single
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class SubVM @Inject constructor(
    private val repository: BaseSubredditsAndPostsRepo
): ViewModel() {
    lateinit var name: String
        private set

    fun setSub(n: String) : Single<PartialViewStateScreen1.T5ForViewing> =
        repository.getSubreddit(n)
                  .onErrorResumeWith(Single.just(RoomT5("ERROR", "ERROR", "",
                      "", "", Instant.now(),0, Instant.now())).retry(10))
                  .map { PartialViewStateScreen1.T5ForViewing(it.toViewState() )}
                  .also { name = n }
}