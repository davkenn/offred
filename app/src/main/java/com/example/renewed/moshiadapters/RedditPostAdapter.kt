package com.example.renewed.moshiadapters

import com.example.renewed.models.*
import com.squareup.moshi.*


class RedditPostAdapter {
    @FromJson
    fun parse(
        reader: JsonReader,
        t1Adapter: JsonAdapter<T1>,
        t3Adapter: JsonAdapter<T3>,
        t5Adapter: JsonAdapter<T5>,
        moreAdapter: JsonAdapter<More>
    ): Holder {
        val jsonObj = reader.readJsonValue() as Map<String, Any>
        val data = when (val type = jsonObj["kind"]) {
            "t5" -> t5Adapter.fromJsonValue(jsonObj["data"])!!
            "t3" -> t3Adapter.fromJsonValue(jsonObj["data"])!!
            "t1" -> t1Adapter.fromJsonValue(jsonObj["data"])!!
            "more" -> moreAdapter.fromJsonValue(jsonObj["data"])!!
            else -> throw IllegalStateException("unexpected type: $type")
        }
        return Holder(data, jsonObj["kind"] as String)
    }
}

class RedditHolderAdapter {
    @FromJson fun RedditPostFromHolder(holder: Holder): RedditPostType = holder.data
    @ToJson fun HolderFromRedditPost(event: RedditPostType): Holder =
        Holder(event, event.name.takeWhile { it != '_' })
}



