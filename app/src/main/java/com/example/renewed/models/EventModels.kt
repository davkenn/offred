package com.example.renewed.models

sealed class MyEvent{
    data class ScreenLoadEvent(val name:String?): MyEvent()
    data class ClickOnT5ViewEvent(val name: String): MyEvent() //this returns two things one a view state and one a view effect
    data class ClickOnT3ViewEvent(val name: String): MyEvent()
    data class RemoveAllSubreddits(val srList:List<String>): MyEvent()
    data class UpdateViewingState(val name: String?): MyEvent()
    data class SaveEvent(val targetedSubreddit: String?, val previousState:List<ViewStateT5>) : MyEvent()
    object ClearEffectEvent:MyEvent()
    object MakeSnackBarEffect:MyEvent()
}

sealed class MyFavsEvent{
    data class UpdatePositionEvent(val newPosition: Int): MyFavsEvent()
    data class DeleteSubredditEvent(val targets:List<String>):MyFavsEvent()
    data class AddSubredditsEvent(val count:Int = 6):MyFavsEvent()
}

