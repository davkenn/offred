package com.example.offred.moshiadapters


import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonQualifier
import com.squareup.moshi.ToJson

/**
 * Adapter for subreddit description to be displayed for a subreddit on the subscreen.
 */
@Retention(AnnotationRetention.RUNTIME)
@JsonQualifier
annotation class SubredditDescription
class DescriptionAdapter {


    @ToJson fun toJson(@SubredditDescription description:  String): String = description

    @FromJson @SubredditDescription
    fun fromJson(descriptionFromJson: String): String =
        descriptionFromJson
                             .replace("&gt;", "")
                             .replace("&lt;", "")
                             .replace("&amp;", "")
                             .replace("nbsp;", "")
                            .replace("""[\[\]()*#]""".toRegex()," ")


    }

