package com.example.renewed.models

/**
 * Events are sent into the view model, then they are processed, resulting in updates to the view
 * state observable that the fragments observe from the view model. These classes
 * are complete lists of all events sent into the view model from screen 1 and all events sent
 * into the view model from screen 2.
 */
sealed class Screen1Event{
    data class ScreenLoadEvent(val name:String?): Screen1Event()
    data class ClickOnT5ViewEvent(val name: String): Screen1Event()
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
    data class UpdateViewedPosts(val newPosts:List<String>): Screen2Event()
    object ClearEffectEvent:Screen2Event()
}

