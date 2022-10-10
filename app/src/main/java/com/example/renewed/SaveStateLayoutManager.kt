package com.example.renewed

import android.content.Context
import android.os.Parcelable
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SaveStateLayoutManager(var ctx: Context) :LinearLayoutManager(ctx){
    override fun onSaveInstanceState(): Parcelable? {
        return super.onSaveInstanceState()

    }


    override fun onRestoreInstanceState(state: Parcelable?) {
        super.onRestoreInstanceState(state)
    }
}