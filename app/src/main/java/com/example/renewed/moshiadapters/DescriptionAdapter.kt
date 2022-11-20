package com.example.renewed.moshiadapters


import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonQualifier
import com.squareup.moshi.ToJson



@Retention(AnnotationRetention.RUNTIME)
@JsonQualifier
annotation class SubredditDescription
class DescriptionAdapter {

    private val regex :Regex = """https?://\S+\s""".toRegex()

    @ToJson fun toJson(@SubredditDescription description:  String): String = description

    @FromJson @SubredditDescription
    fun fromJson(descriptionFromJson: String): String =
        descriptionFromJson
                             .replace("&gt;", "")
                            .replace("&lt;", "")
                            .replace("""[\[\]()*#]""".toRegex(),"")
                            .replace(regex) {"\n${it.value}\n"}

    }

