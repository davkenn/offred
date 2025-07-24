package com.example.offred.moshiadapters


import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonQualifier
import com.squareup.moshi.ToJson


@Retention(AnnotationRetention.RUNTIME)
@JsonQualifier
annotation class HtmlDecoded

class HtmlDecodedAdapter {

    @ToJson
    fun toJson(@HtmlDecoded value: String): String = value

    @FromJson
    @HtmlDecoded
    fun fromJson(json: String): String {
        return json
            // Handle common HTML entities
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&nbsp;", " ")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&apos;", "'")
            // Handle numeric entities if needed
            .replace("&#x200B;", "") // Zero-width space
    }
}
