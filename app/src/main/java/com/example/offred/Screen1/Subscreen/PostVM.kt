package com.example.offred.Screen1.Subscreen

import androidx.lifecycle.ViewModel
import com.example.offred.repos.BaseSubredditsAndPostsRepo
import com.example.offred.models.ViewStateT3
import com.example.offred.models.toViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

@HiltViewModel
class PostVM @Inject constructor(
private val repository: BaseSubredditsAndPostsRepo
): ViewModel() {
    var pos: Int=0
    var name: String? = null
        private set


    fun setPost(n: String) : Single<ViewStateT3> =
        repository.getPost(n)
                  .doOnEvent{x,_->name =  x?.subredditId} //feels bad
                  .map { it.toViewState() }
}
