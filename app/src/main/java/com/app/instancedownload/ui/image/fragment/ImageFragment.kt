package com.app.instancedownload.ui.image.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.instancedownload.R
import com.app.instancedownload.databinding.FragmentBinding
import com.app.instancedownload.ui.image.adapter.ListAdapter
import com.app.instancedownload.ui.image.viewmodel.ImageViewModel
import com.app.instancedownload.ui.show.activity.ShowActivity
import com.app.instancedownload.util.*
import com.app.instancedownload.util.Type.ADAPTER
import com.app.instancedownload.util.Type.SERVICE
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ImageFragment : Fragment() {

    private var listAdapter: ListAdapter? = null
    private lateinit var databinding: FragmentBinding
    private lateinit var imageViewModel: ImageViewModel
    private lateinit var animation: LayoutAnimationController

    @Inject
    lateinit var method: Method

    @Inject
    lateinit var liveData: LiveData<LiveDataType<String>>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        databinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment, container, false
        )

        val resId = R.anim.layout_animation_from_bottom
        animation = AnimationUtils.loadLayoutAnimation(activity, resId)

        databinding.progressbarFragment.gone()
        databinding.textViewNoDataFragment.gone()

        liveData.observe(requireActivity()) {
            when (it.type) {
                ADAPTER -> {
                    setImageAdapter()
                }
                SERVICE -> TODO()
            }
        }

        imageViewModel = ViewModelProvider(this)[ImageViewModel::class.java]
        imageViewModel.getImage()
        imageViewModel.imageLiveData.observe(requireActivity()) {
            when (it.status) {
                Status.SUCCESS -> {
                    Constant.imageArray.clear()
                    Constant.imageArray.addAll(it.data!!)
                    setImageAdapter()
                    databinding.progressbarFragment.gone()
                }
                Status.LOADING -> {
                    databinding.progressbarFragment.visible()
                }
                Status.ERROR -> {
                    databinding.progressbarFragment.gone()
                    Toast.makeText(requireActivity(), it.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        databinding.recyclerViewFragment.setHasFixedSize(true)
        val layoutManager: RecyclerView.LayoutManager = GridLayoutManager(activity, 2)
        databinding.recyclerViewFragment.layoutManager = layoutManager
        databinding.recyclerViewFragment.layoutAnimation = animation

        return databinding.root

    }

    private fun setImageAdapter() {
        if (listAdapter == null) {
            if (Constant.imageArray.size == 0) {
                databinding.textViewNoDataFragment.visible()
            } else {
                listAdapter = ListAdapter(requireActivity(),
                    Constant.imageArray,
                    "image",
                    method,
                    backResult = { position, type, _ ->
                        startActivity(
                            Intent(
                                activity, ShowActivity::class.java
                            ).putExtra("position", position).putExtra("type", type)
                        )
                    })
                databinding.recyclerViewFragment.adapter = listAdapter
                databinding.recyclerViewFragment.layoutAnimation = animation
                databinding.textViewNoDataFragment.gone()
            }
        } else {
            listAdapter!!.notifyDataSetChanged()
        }
    }
}