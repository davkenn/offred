package com.example.renewed.DefaultDBContents

import com.example.renewed.models.RoomT5
import java.time.Instant

var t5SampleList = listOf(
    RoomT5(
name = "t5_tu4j3",
displayName = "Fun with roses",
description ="A great site",
thumbnail ="",
banner_img ="",
created_utc = Instant.now(),
timeLastAccessed = Instant.now(),
subscribers = 8,
isSaved = false,
totalViews = 0)
)