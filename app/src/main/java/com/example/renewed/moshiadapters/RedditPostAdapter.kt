package com.example.renewed.moshiadapters

import com.example.renewed.models.*
import com.example.renewed.models.*
import com.squareup.moshi.*

//TODO exact same adapter to strip this
class RedditPostAdapter {

    @FromJson
    fun parse(
        reader: JsonReader,
        t1Adapter: JsonAdapter<T1>,
        t5Adapter: JsonAdapter<T5>,
        t3Adapter: JsonAdapter<T3>
    ): Holder {
        val jsonObj = reader.readJsonValue() as Map<String, Any>
        val type = jsonObj["kind"]
        val data = when (type) {
            "t5" -> t5Adapter.fromJsonValue(jsonObj["data"])!!
            "t3" -> t3Adapter.fromJsonValue(jsonObj["data"])!!
            "t1" -> t1Adapter.fromJsonValue(jsonObj["data"])!!
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


/**class RedditListingAdapter {
    @FromJson fun RedditListingFromListing(listing: Listing): RedditListing = listing.data
    @ToJson fun ListingFromRedditListing(event: RedditListing): Listing = Listing(event)
}**/
