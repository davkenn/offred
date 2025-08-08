package com.example.offred.Screen1.Subscreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.offred.R


class BlankFragment : ContentFragment() {
    override fun getName():String = "BlankFragment"
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                                                     savedInstanceState: Bundle?): View =
                        inflater.inflate(R.layout.blank_view, container, false)
}