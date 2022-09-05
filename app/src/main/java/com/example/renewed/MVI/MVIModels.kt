package com.example.renewed.MVI

import com.example.renewed.models.ViewStateT5

sealed class SubredditsStreamEvent{
    object OnCreateEvent : SubredditsStreamEvent()
}

sealed class PostsAndSubredditsListItem(val viewType: Int) {
    object EmptyListItem : PostsAndSubredditsListItem(0)
    data class HeaderListItem(val title: String) : PostsAndSubredditsListItem(1)
    data class ListItemT5(val post: ViewStateT5) : PostsAndSubredditsListItem(2)
}

