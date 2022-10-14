package com.example.renewed.models

sealed class MyEvent{
    data class ScreenLoadEvent(val name:String?): MyEvent() // will lead to returning a list t5 viewstate
    data class ClickOnT5ViewEvent(val name: String): MyEvent() //this returns two things one a view state and one a view effect
    data class ClickOnT3ViewEvent(val name: String): MyEvent()
    data class RemoveAllSubreddits(val srList:List<String>): MyEvent()
    data class UpdateViewingState(val name: String?): MyEvent()
    data class SaveOrDeleteEvent(val targetedSubreddit: String?,val previousState:List<ViewStateT5>,val shouldDelete:Boolean) : MyEvent()
    object ClearEffectEvent:MyEvent()
    object MakeSnackBarEffect:MyEvent()

}

