package com.example.renewed.Screen1.Subscreen

import android.os.Bundle
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.renewed.databinding.SubViewBinding
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers


@AndroidEntryPoint
class SubredditFragment : ContentFragment() {

    private val subVM: SubVM by viewModels()
    private var subBinding: SubViewBinding? = null

    override fun getName() : String = subVM.name

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        val binding = SubViewBinding.inflate(inflater,container,false)
        subBinding = binding
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        val name = arguments?.getString("key") ?: "NONE"
        subVM.setSub(name)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe { x ->
                subBinding!!.apply{ subname.text = x.t5.displayName
                                    subBinding!!.timeCreated.append(x.t5.created)
                                    subBinding!!.subscribers.append(x.t5.subscribers.toString())
                                    subBinding!!.description.text = x.t5.description
                                    Linkify.addLinks(subBinding!!.description, Linkify.WEB_URLS)
                                  }
            }
    }

    override fun onDestroyView() {
        subBinding = null
        super.onDestroyView()
    }
}

