package com.example.renewed.models

sealed class MyEvent{
    object ScreenLoadEvent: MyEvent() // will lead to returning a list t5 viewstate
    data class ClickOnT5ViewEvent(val name: String): MyEvent() //this returns two things one a view state and one a view effect
    data class ClickOnT3ViewEvent(val name: String): MyEvent()
    data class RemoveAllSubreddits(val srList:List<String>): MyEvent()
    //data class DeletePostEvent(val name:String): MyEvent()
}
//this is returned to the view in one observable
sealed class MyViewState{
    //TODO make it on a superclass and consolidate?
    data class T5ListForRV(val vsT5: List<ViewStateT5>): MyViewState()
    data class T3ListForRV(val vsT3: List<ViewStateT3>?): MyViewState()
    data class T3ForViewing(val t3 : ViewStateT3): MyViewState()
    data class T5ForViewing(val t5 : ViewStateT5): MyViewState()


}