package com.example.renewed.models

import java.time.Instant



/**
 * The partial and full view state classes are used by the view model to represent the latest state
 * of the UI to the Fragment that is observing the view model. After events (button clicks,
 * page loads etc.) are sent to the viewmodel, the view model responds
 * to these events by updating the view state that its' fragment is observing. Events are processed
 * and then turned into pieces of partial view state. The view model uses this partial view state
 * to update the relevant part of the full view state that was last sent to the UI. Once the full
 * view state is updated with the latest partial view state, that full view state is observed by the
 * fragment.
 *
 */
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
    object ClearEffectEffect:PartialViewStateScreen2("ClearState")
    data class Position(val position:Int):PartialViewStateScreen2("Position")
    data class Posts(val posts:List<String>):PartialViewStateScreen2("Position")
}

enum class Screen2Effect{
    DELETE,LOAD
}

enum class Screen1Effect{
    DELETE_OR_SAVE,SNACKBAR
}

/**
 * The Reddit API defines different entity types, including T5 for subreddits, T3 for posts in those
 * subreddits, and T1 for comments replying to those posts. For every kind of entity that is defined
 * in the reddit json endpoints we have three different model classes: the network models, the
 * database models, and the view models. This way, what is parsed from network responses,  what is
 * stored in the database, and what is displayed in the UI can all vary independently. These models
 * represent what is presented in the UI.
 */
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

