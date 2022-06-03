package com.example.renewed

import androidx.lifecycle.ViewModel
import com.example.renewed.models.MyViewState
import com.example.renewed.models.toViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

@HiltViewModel
class PostVM @Inject constructor(
private val repository: BaseSubredditsAndPostsRepo
): ViewModel() {
    //TODO fix this to make it more stateful maybe
    //TODO by doing this maybe you can still delete the posts right away
    //TODO but maybe this would make your update logic more complicated bc you may save a deleted post
    //TODO it actually makes sense to purge all viewed but not saved posts at startup, so add a viewed field
    fun setFullname(n: String) : Single<MyViewState.T3ForViewing> =
        repository.getPost(n).map { MyViewState.T3ForViewing(it.toViewState() )}



}