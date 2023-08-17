package com.example.renewed.test

import androidx.test.espresso.idling.CountingIdlingResource

object CountingIdleResource {

    private const val RESOURCE = "GLOBAL"

    @JvmField
    val countingIdlingResource = CountingIdlingResource(RESOURCE)

    fun increment() {
        countingIdlingResource.increment()
    }

    fun decrement() {
   //     if (!countingIdlingResource.isIdleNow) {
            countingIdlingResource.decrement()
     //   }
    }
}