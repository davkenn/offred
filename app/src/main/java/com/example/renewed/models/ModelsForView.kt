package com.example.renewed.models

import io.reactivex.rxjava3.core.Observable
import java.time.Instant

data class FullViewStateScreen2(

    val currentlyDisplayedList: List<String>,
    val position : Int,
    val effect: EffectType?=null)


data class FullViewStateScreen1(
    val t3ListForRV: PartialViewState.T3ListForRV?=null,
    val t5ListForRV: PartialViewState.T5ListForRV?=null,
    val latestEvent5: PartialViewState.T5ForViewing?=null,
    val latestEvent3: PartialViewState.T3ForViewing?=null,
    val effect: EffectType?=null)

sealed class PartialViewState(val name: String?){
    data class T5ListForRV(val vsT5: List<ViewStateT5>?): PartialViewState("T3List")
    data class T3ListForRV(val vsT3: List<ViewStateT3>?): PartialViewState("T5List")
    data class T3ForViewing(val t3 : ViewStateT3): PartialViewState(t3.name)
    data class T5ForViewing(val t5 : ViewStateT5): PartialViewState(t5.name)
    object NavigateBackEffect:PartialViewState("NavigateEffect")
    object ClearEffectEffect:PartialViewState("ClearState")
    object SnackbarEffect:PartialViewState("SnackbarEffect")
}

//used on Screen2
enum class EffectType2{
    DELETE,LOAD
}
//Used on Screen1
enum class EffectType{
    DELETE_OR_SAVE,SNACKBAR
}

data class ViewStateT5(val name: String, val displayName: String, val description: String,
                       val thumbnail: String, val bannerImg: String, val subscribers: Int,
                       val created: String, val timeLastAccessed: Instant) {

        override fun toString(): String = "ViewStateT5($name) "

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
//todo this is better but doesnt capture the text posts that no need url
//   && !isImagePost(t3ViewState) && !isVideoPost(t3ViewState)

//TODO IMGUR can also be an mp4 or a webp (videopost)
fun ViewStateT3.isImagePost() =  "i.redd.it" in url || "imgur" in url

fun ViewStateT3.isVideoPost() =  "v.redd.it" in url

fun ViewStateT3.hasNoThumbnail() =  thumbnail.isBlank() || thumbnail == "self" ||
                             isGalleryPost() || isImagePost() || isVideoPost() ||
                             thumbnail == "default"||  thumbnail == "spoiler" //|| thumbnail == nsfw

