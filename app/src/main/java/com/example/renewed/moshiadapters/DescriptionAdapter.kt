package com.example.renewed.moshiadapters


import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonQualifier
import com.squareup.moshi.ToJson


//TODO where do I put this annotation?
@Retention(AnnotationRetention.RUNTIME)
@JsonQualifier
annotation class SubredditDescription

//TODO maybe just move this into db model
//TODO whats with the descriptions that just go off end of object?
//is this a string length limit or just a truncation on the log screen
class DescriptionAdapter {
    //TODO should these be in companion object
    private val regex :Regex = """https?://\S+\s""".toRegex()

    @ToJson fun toJson(@SubredditDescription description:  String): String = description

    //TODO make sure order right I think it is
    @FromJson @SubredditDescription
    fun fromJson(descriptionFromJson: String): String {
        return descriptionFromJson

            .replace("&gt;", "")
            .replace("&lt;", "")
            .replace("&amp;", " and ")

            .replace("""[\[\]()*]""".toRegex(),"")

     //       .replace("\\s+", " ")

            .replace(regex) {"\n${it.value}\n"}
    }

}