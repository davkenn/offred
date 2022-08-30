package com.example.renewed

import androidx.lifecycle.ViewModel
import com.example.renewed.models.MyEvent
import com.example.renewed.models.MyViewState
import com.example.renewed.models.toViewState
import com.jakewharton.rxrelay3.PublishRelay
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

@HiltViewModel
class PostVM @Inject constructor(
private val repository: BaseSubredditsAndPostsRepo
): ViewModel() {

    fun setFullname(n: String) : Single<MyViewState.T3ForViewing> =
        repository.getPost(n).map { MyViewState.T3ForViewing(it.toViewState() )}



}
