package com.example.renewed.models

import com.example.renewed.models.MyViewState

//data class ViewStateT5(val displayName:String, val timeLastAccessed: String,val created_utc:String)

data class FullViewState(
    val t3ListForRV: MyViewState.T3ListForRV?=null,
    val t5ListForRV: MyViewState.T5ListForRV?=null,
    val latestEvent5: MyViewState.T5ForViewing?=null,
    val latestEvent3: MyViewState.T3ForViewing?=null,
    val eventProcessed: Boolean = false

)

data class ViewStateT5(
    val name: String,
    val displayName: String,
    val description: String,
    val thumbnail: String,
    val bannerImg: String,
    val subscribers: Int,
    val created: String)

data class ViewStateT3(
    val name: String,
    val displayName: String,
    val selftext: String,
    val url: String,
    val thumbnail: String,
    val created: String
)