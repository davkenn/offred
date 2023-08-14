package com.example.renewed.moshiadapters

import com.example.renewed.models.*
import com.squareup.moshi.*

//media_metadata json key contains full images and not thumbnails. Need a special adapter
//because the key for the object inside media_metadata has a random key
class MediaList {
    @FromJson
    fun parse(reader: JsonReader, mediaAdapter: JsonAdapter<GalleryMedia>, ): List<GalleryMedia> {
        var ls = mutableListOf<GalleryMedia>()
        val jsonObj = reader.readJsonValue() as Map<String, Any>
        return ls.apply { jsonObj.keys.forEach { ls.add(mediaAdapter.fromJsonValue(jsonObj[it])!!)}}
    }
}

