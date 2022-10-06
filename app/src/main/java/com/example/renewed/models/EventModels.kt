package com.example.renewed.models

sealed class MyEvent{
    data class ScreenLoadEvent(val name:String?): MyEvent() // will lead to returning a list t5 viewstate
    data class ClickOnT5ViewEvent(val name: String): MyEvent() //this returns two things one a view state and one a view effect
    data class ClickOnT3ViewEvent(val name: String): MyEvent()
    data class RemoveAllSubreddits(val srList:List<Pair<String,String>>): MyEvent()
   data class UpdateViewingState(val name: String?): MyEvent()
    data class SaveOrDeleteEvent(val targetedSubreddit: String?, val shouldDelete:Boolean) : MyEvent() {

    }

}
//this is returned to the view in one observable
