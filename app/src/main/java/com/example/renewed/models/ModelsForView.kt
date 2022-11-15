package com.example.renewed.models

import java.time.Instant

data class FullViewState(
    val t3ListForRV: PartialViewState.T3ListForRV?=null,
    val t5ListForRV: PartialViewState.T5ListForRV?=null,
    val latestEvent5: PartialViewState.T5ForViewing?=null,
    val latestEvent3: PartialViewState.T3ForViewing?=null,
    val effect: EffectType?=null)

sealed class PartialViewState(val name: String?){
    data class T5ListForRV(val vsT5: List<ViewStateT5>): PartialViewState("T3List")
    data class T3ListForRV(val vsT3: List<ViewStateT3>?): PartialViewState("T5List")
    data class T3ForViewing(val t3 : ViewStateT3): PartialViewState(t3.name)
    data class T5ForViewing(val t5 : ViewStateT5): PartialViewState(t5.name)
    object NavigateBackEffect:PartialViewState("NavigateEffect")
    object ClearEffectEffect:PartialViewState("ClearState")
    object SnackbarEffect:PartialViewState("SnackbarEffect")
}

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
            result = 31 * result + thumbnail.hashCode()
            result = 31 * result + bannerImg.hashCode()
            result = 31 * result + subscribers
            result = 31 * result + created.hashCode()
            return result
    }
}

data class ViewStateT3(val name: String, val displayName: String, val subredditId: String,
                       val selftext: String, val url: String, val thumbnail: String, val created: String ) {
                        override fun toString(): String = "ViewStateT3($name) " }