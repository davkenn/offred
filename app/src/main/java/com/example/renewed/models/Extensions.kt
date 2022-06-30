package com.example.renewed.models

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun T5.toDbModel() : RoomT5 {

    //add let in here somewhere
    var fullDescription = combineDescriptions(this.description,this.public_description)

    var thumb1 = icon_img?: ""
    var thumb2 = header_img?:""
    var thumb3 = community_icon?:""

    val thumbnail = thumb1.ifBlank { thumb3.substringBeforeLast("?")}.ifBlank { thumb2 }

    return RoomT5(
                name = name,
                displayName = display_name,
                description=fullDescription,
                thumbnail=thumbnail,
                banner_img=banner_img?:"",
                created_utc = Instant.ofEpochSecond(created_utc),
                timeLastAccessed = Instant.now(),
                subscribers= subscribers,
                isSaved = 0,
                totalViews = 0)

}


//fun RoomT5.toViewState(): ViewStateT5 =
  //  ViewStateT5(
    //        displayName = this.displayName,
      //      timeLastAccessed= DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneId.systemDefault()).format(timeLastAccessed),
        //    created_utc = DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneId.systemDefault()).format(created_utc))

//TODO selftext is optional here
fun T3.toDbModel() : RoomT3 =
    RoomT3(
        name = name, subredditId = subreddit_id, created_utc =Instant.ofEpochSecond(created_utc),
        title =title, selftext =selftext, url =url, permalink =permalink, thumbnail =thumbnail)



/**fun RoomT5.toViewState(): ViewStateT5 =
    ViewStateT5(
        displayName = this.displayName,
        timeLastAccessed= timeLastAccessed.atZone(ZoneId.systemDefault())
                        .toLocalDateTime()
                        .format(DateTimeFormatter.ofPattern("EE MMM dd, yy h:mm a"))
                        .toString(),
        created_utc= created_utc.atZone(ZoneId.systemDefault())
                    .toLocalDateTime()
                    .format(DateTimeFormatter.ofPattern("EE MMMM dd, yyyy hh:mm a"))
                    .toString()
        )
**/
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


fun combineDescriptions(description : String="", publicDescription : String="") : String {

        return buildString {
                                    append(description)
                                        append("\n\n")
                                            append(publicDescription)
                                                append("\n\n")}

}