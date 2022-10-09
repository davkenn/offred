package com.example.renewed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup


class BlankFragment : ContentFragment() {
    override fun getName():String = "BlankFragment"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.blank_view, container, false)
    }
}