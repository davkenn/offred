package com.example.renewed.Room

import android.annotation.SuppressLint
import androidx.room.TypeConverter
import java.time.Instant

class Converters {

        companion object {
            //todo is this right
            @SuppressLint("NewApi")
            @TypeConverter
            @JvmStatic
            fun fromInstant(value: Instant): Long {
                return value.toEpochMilli()
            }

            @SuppressLint("NewApi")
            @TypeConverter
            @JvmStatic
            fun toInstant(value: Long): Instant {
                return Instant.ofEpochMilli(value)
            }
        }

}

