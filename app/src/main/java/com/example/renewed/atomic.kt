package com.example.renewed

import java.util.concurrent.atomic.AtomicReference
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun <T>atomic(tIn: T): ReadWriteProperty<Any?, T> {

    return object : ReadWriteProperty<Any?, T> {

        val t = AtomicReference(tIn)

        override fun getValue(thisRef: Any?, property: KProperty<*>): T = t.get()

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) = t.set(value)
    }
}