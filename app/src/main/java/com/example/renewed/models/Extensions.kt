package com.example.renewed.models

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun T5.toDbModel(): RoomT5 {
    val fullDescription = combineDescriptions(this.description, this.public_description)
    val thumb1 = icon_img ?: ""
    val thumb2 = header_img ?: ""
    val thumb3 = community_icon ?: ""
    val thumbnail = thumb1.ifBlank { thumb3.substringBeforeLast("?") }.ifBlank { thumb2 }
    return RoomT5(
        name = name, displayName = display_name, description = fullDescription,
        thumbnail = thumbnail, banner_img = banner_img ?: "", isSaved = false,
        created_utc = Instant.ofEpochSecond(created_utc), totalViews = 0,
        timeLastAccessed = Instant.now(), subscribers = subscribers
    )
}

fun T3.toDbModel(): RoomT3 {
    var thumb: String?=null
    var address: String?=null
    //TODO need to fix this and get the actual url from the other field
    if (url.startsWith("https://v.redd.it")){
        //TODO whyd I go away from dash originally and how can I fix this so it works for all vids? another field? maybe load both?
                                                address=
                                                    media?.reddit_video?.dash_url?:
                                                    media?.reddit_video?.fallback_url


                                                                ?: (url + "/DASH_720.mp4?source=fallback url")
    }
  //  if (media_metadata!=null && (thumb!=null) && ("gallery" !in url)) {
    //    thumb = media_metadata.p?.last()?.u?.replace("amp;", "")
      //  }
    return RoomT3(
        name = name, subredditId = subreddit_id, selftext = selftext, url = address?: url,
        created_utc = Instant.ofEpochSecond(created_utc), permalink = permalink,
        timeLastAccessed = Instant.now(), title = title, thumbnail = thumb?:thumbnail,
        gallery_urls = media_metadata?.joinToString(separator = " ") {
                                                it.p?.last()?.u?.
                                                replace("amp;","")?:""})
}



fun RoomT5.toViewState(): ViewStateT5 =
    ViewStateT5(
        name = name, displayName = displayName, description = description,
        thumbnail = thumbnail, bannerImg = banner_img, subscribers = subscribers,
        created = created_utc.instantToDateString(), timeLastAccessed = timeLastAccessed
    )

fun RoomT3.toViewState(): ViewStateT3 =
    ViewStateT3(
        name = this.name, displayName = this.title, subredditId = this.subredditId,
        selftext = this.selftext, thumbnail = this.thumbnail, url = this.url,
        created = this.created_utc.instantToDateString().replaceFirst(" ", "\n"),
        galleryUrls = gallery_urls?.split(" "))

private fun Instant.instantToDateString() =
    this.atZone(ZoneId.of("America/Los_Angeles"))
        .toLocalDateTime()
        .format(DateTimeFormatter.ofPattern("MM/dd/yyyy h:mm a"))
        .toString()

private fun combineDescriptions(description: String = "", publicDescription: String = ""): String =
    buildString {
        if (!description.contains(publicDescription)) {
            append("$publicDescription\n\n")
        }
        append("$description\n\n")
    }
