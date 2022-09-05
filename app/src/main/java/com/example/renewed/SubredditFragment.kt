package com.example.renewed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.renewed.databinding.SubViewBinding

import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

@AndroidEntryPoint
class SubredditFragment : Fragment() {

    private val subVM: SubVM by viewModels()
    private var subBinding: SubViewBinding? = null

    fun getName() : String {
       return subVM.name
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

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
                subBinding!!.apply{
                    subname.text = x.t5.displayName
                    subBinding!!.timeCreated.append(x.t5.created)
                    subBinding!!.subscribers.append(x.t5.subscribers.toString())
                    subBinding!!.description.text = x.t5.description
                }
            }
    }

    override fun onDestroyView() {
        subBinding = null
        super.onDestroyView()
    }
}

