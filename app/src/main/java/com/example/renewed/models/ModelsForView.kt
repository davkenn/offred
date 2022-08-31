package com.example.renewed.models

import com.example.renewed.models.MyViewState
import java.time.Instant

//data class ViewStateT5(val displayName:String, val timeLastAccessed: String,val created_utc:String)

data class FullViewState(
    val t3ListForRV: MyViewState.T3ListForRV?=null,
    val t5ListForRV: MyViewState.T5ListForRV?=null,
    val latestEvent5: MyViewState.T5ForViewing?=null,
    val latestEvent3: MyViewState.T3ForViewing?=null,
    val eventProcessed: Boolean = false

)

sealed class MyViewState{
    //TODO make it on a superclass and consolidate?

    data class T5ListForRV(val vsT5: List<ViewStateT5>): MyViewState()
    data class T3ListForRV(val vsT3: List<ViewStateT3>?): MyViewState()
    data class T3ForViewing(val t3 : ViewStateT3): MyViewState()
    data class T5ForViewing(val t5 : ViewStateT5): MyViewState()
    object NavigateBack:MyViewState()

}
data class ViewStateT5(
    val name: String,
    val displayName: String,
    val description: String,
    val thumbnail: String,
    val bannerImg: String,
    val subscribers: Int,
    val created: String,
    val timeLastAccessed: Instant) {
    override fun toString(): String = "ViewStateT5($name) "
}

data class ViewStateT3(
    val name: String,
    val displayName: String,
    val selftext: String,
    val url: String,
    val thumbnail: String,
    val created: String
){
    override fun toString(): String = "ViewStateT3($name) "
}