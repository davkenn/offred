package com.example.renewed.Screen1.Subscreen

import androidx.lifecycle.ViewModel
import com.example.renewed.repos.BaseSubredditsAndPostsRepo
import com.example.renewed.models.PartialViewState
import com.example.renewed.models.toViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

@HiltViewModel
class PostVM @Inject constructor(
private val repository: BaseSubredditsAndPostsRepo
): ViewModel() {
    lateinit var name: String
        private set

    fun setPost(n: String) : Single<PartialViewState.T3ForViewing> =
        repository.getPost(n)
            .doOnEvent{x,y->name =  x.subredditId}
            .map { PartialViewState.T3ForViewing(it.toViewState() )}
}
