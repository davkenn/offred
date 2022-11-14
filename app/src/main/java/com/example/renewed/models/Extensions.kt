package com.example.renewed.models

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun T5.toDbModel() : RoomT5 {


    val fullDescription = combineDescriptions(this.description,this.public_description)
    val thumb1 = icon_img?: ""
    val thumb2 = header_img?:""
    val thumb3 = community_icon?:""
    val thumbnail = thumb1.ifBlank { thumb3.substringBeforeLast("?")}.ifBlank { thumb2 }

    return RoomT5(
                name = name,
                displayName = display_name,
                description =fullDescription,
                thumbnail =thumbnail,
                banner_img =banner_img?:"",
                created_utc = Instant.ofEpochSecond(created_utc),
                timeLastAccessed = Instant.now(),
        subscribers = subscribers,
                isSaved = false,
                totalViews = 0)
}

//TODO selftext is optional here
fun T3.toDbModel() : RoomT3 {
    if (url.startsWith("https://v.redd.it")) url+= "/DASH_720.mp4?source=fallback"
    return RoomT3(

        name = name,
        subredditId = subreddit_id,
        created_utc = Instant.ofEpochSecond(created_utc),
        timeLastAccessed = Instant.now(),
        title = title,
        selftext = selftext,
        url = url,
        permalink = permalink,
        thumbnail = thumbnail
    )

}

    fun RoomT5.toViewState(): ViewStateT5 =
        ViewStateT5(
            name = name,
            displayName = displayName,
            description=description,
            thumbnail =thumbnail,
            bannerImg=banner_img,
            subscribers = subscribers,
           created= created_utc.instantToDateString(),
        timeLastAccessed=timeLastAccessed
        )


fun RoomT3.toViewState(): ViewStateT3 =

    ViewStateT3(
        name = this.name,
        displayName = this.title,
        subredditId = this.subredditId,
        selftext = this.selftext,
        thumbnail = this.thumbnail,
        url = this.url,
        created= this.created_utc
            .instantToDateString().replaceFirst(" ","\n")
     )


    private fun Instant.instantToDateString() =
        this.atZone(ZoneId.of("America/Los_Angeles"))
        .toLocalDateTime()
        .format(DateTimeFormatter.ofPattern("MM/dd/yyyy h:mm a"))
        .toString()


    private fun combineDescriptions(description : String="", publicDescription : String="") : String {
        return buildString {
                        if (publicDescription.isNotBlank())  append("$publicDescription\n\n")
                           if (description.isNotBlank()) append("$description\n\n")
        }
    }