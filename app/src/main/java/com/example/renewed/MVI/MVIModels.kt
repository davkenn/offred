package com.example.renewed.MVI

import com.example.renewed.models.ViewStateT5

sealed class SubredditsStreamEvent{
    object OnCreateEvent : SubredditsStreamEvent()
}

sealed class PostsAndSubredditsListItem {
    object EmptyListItem : PostsAndSubredditsListItem()
    data class HeaderListItem(val title: String) : PostsAndSubredditsListItem()
    data class ListItemT5(val post: ViewStateT5) : PostsAndSubredditsListItem()
}

