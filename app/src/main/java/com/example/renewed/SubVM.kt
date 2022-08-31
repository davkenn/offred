package com.example.renewed

import androidx.lifecycle.ViewModel
import com.example.renewed.models.MyViewState
import com.example.renewed.models.toViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

@HiltViewModel
class SubVM @Inject constructor(
    private val repository: BaseSubredditsAndPostsRepo
): ViewModel() {
    lateinit var name: String
        private set


    fun setSub(n: String) : Single<MyViewState.T5ForViewing> =
        repository.getSubreddit(n).map { MyViewState.T5ForViewing(it.toViewState() )}
            .also { name = n }




}