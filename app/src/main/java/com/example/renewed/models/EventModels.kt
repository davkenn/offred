package com.example.renewed.models

sealed class Screen1Event{
    data class ScreenLoadEvent(val name:String?): Screen1Event()
    data class ClickOnT5ViewEvent(val name: String): Screen1Event() //this returns two things one a view state and one a view effect
    data class ClickOnT3ViewEvent(val name: String): Screen1Event()
    data class RemoveAllSubreddits(val srList:List<String>): Screen1Event()
    data class UpdateViewingState(val name: String?): Screen1Event()
    data class SaveEvent(val targetedSubreddit: String?, val previousState:List<ViewStateT5>) : Screen1Event()
    object ClearEffectEvent:Screen1Event()
    object MakeSnackBarEffect:Screen1Event()
}

sealed class Screen2Event{
    data class UpdatePositionEvent(val newPosition: Int): Screen2Event()
    data class DeleteSubredditEvent(val targets:List<String>):Screen2Event()
    data class AddSubredditsEvent(val count:Int = 6):Screen2Event()
}

