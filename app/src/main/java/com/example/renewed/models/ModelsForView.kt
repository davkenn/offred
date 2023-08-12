package com.example.renewed.models

import java.time.Instant



data class FullViewStateScreen1(
    val t3ListForRV: PartialViewStateScreen1.T3ListForRV?=null,
    val t5ListForRV: PartialViewStateScreen1.T5ListForRV?=null,
    val latestEvent5: PartialViewStateScreen1.T5ForViewing?=null,
    val latestEvent3: PartialViewStateScreen1.T3ForViewing?=null,
    val effect: Screen1Effect?=null)

sealed class PartialViewStateScreen1(val name: String?){
    data class T5ListForRV(val vsT5: List<ViewStateT5>?): PartialViewStateScreen1("T3List")
    data class T3ListForRV(val vsT3: List<ViewStateT3>?): PartialViewStateScreen1("T5List")
    data class T3ForViewing(val t3 : ViewStateT3): PartialViewStateScreen1(t3.name)
    data class T5ForViewing(val t5 : ViewStateT5): PartialViewStateScreen1(t5.name)
    object NavigateBackEffect:PartialViewStateScreen1("NavigateEffect")
    object ClearEffectEffect:PartialViewStateScreen1("ClearState")
    object SnackbarEffect:PartialViewStateScreen1("SnackbarEffect")
}

data class FullViewStateScreen2(
    val currentlyDisplayedList: PartialViewStateScreen2.Posts?=null,
    val position : PartialViewStateScreen2.Position?=null,
    val effect: Screen2Effect?=null)

sealed class PartialViewStateScreen2(val name: String?){
    object DeleteCompleteEffect: PartialViewStateScreen2("DeleteCompleteEffect")
    object LoadCompleteEffect: PartialViewStateScreen2("LoadCompleteEffect")
    data class Position(val position:Int):PartialViewStateScreen2("Position")
    data class Posts(val posts:List<String>):PartialViewStateScreen2("Position")
}

enum class Screen2Effect{
    DELETE,LOAD
}

enum class Screen1Effect{
    DELETE_OR_SAVE,SNACKBAR
}

data class ViewStateT5(val name: String, val displayName: String, val description: String,
                       val thumbnail: String, val bannerImg: String, val subscribers: Int,
                       val created: String, val timeLastAccessed: Instant) {

        override fun toString(): String = "ViewStateT5($name) "

        //override equals and hash code so equality only depends on displayName property
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as ViewStateT5
            return displayName == other.displayName
        }
        override fun hashCode(): Int {
            var result = name.hashCode()
            result = 31 * result + displayName.hashCode()
            result = 31 * result + subscribers
            result = 31 * result + created.hashCode()
            return result
    }
}

data class ViewStateT3(
    val name: String, val displayName: String, val subredditId: String,
    val selftext: String, val url: String, val thumbnail: String,
    val created: String, val galleryUrls: List<String>?
) {
                        override fun toString(): String = "ViewStateT3($name) " }

fun ViewStateT3.isGalleryPost() =   ("reddit" in url) && ("gallery" in url)

fun ViewStateT3.isUrlPost() =
    url.startsWith("http")// && "com" in x.t3.url
            && ("reddit" !in url  && "redd.it" !in url  && "imgur" !in url)

//TODO IMGUR can also be an mp4 or webp
fun ViewStateT3.isImagePost() =  "i.redd.it" in url || "imgur" in url

fun ViewStateT3.isVideoPost() =  "v.redd.it" in url

fun ViewStateT3.hasNoThumbnail() =  thumbnail.isBlank() || thumbnail == "self" ||
                             isGalleryPost() || isImagePost() || isVideoPost() ||
                             thumbnail == "default"||  thumbnail == "spoiler" //|| thumbnail == nsfw

