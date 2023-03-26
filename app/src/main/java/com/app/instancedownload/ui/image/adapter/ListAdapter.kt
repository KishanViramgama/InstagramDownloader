package com.app.instancedownload.ui.image.adapter

import android.app.Activity
import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.app.instancedownload.R
import com.app.instancedownload.databinding.ViewAdapterBinding
import com.app.instancedownload.util.Method
import java.io.File

class ListAdapter constructor(
    private val context: Context,
    private val stringList: List<File>,
    private val string: String,
    private val method: Method,
    private val backResult: (position: Int, type: String, data: String) -> Unit
) : RecyclerView.Adapter<ListAdapter.ViewHolder>() {

    private var columnWidth: Int = 0

    init {
        val padding = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 5f, context.resources.displayMetrics
        ).toDouble()
        columnWidth = ((method.screenWidth - (2 + 1) * padding) / 2).toInt()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ViewAdapterBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.binding.imageViewViewAdapter.layoutParams =
            ConstraintLayout.LayoutParams(columnWidth, columnWidth)

        Glide.with(context).load(stringList[position].toString())
            .placeholder(R.drawable.place_holder).into(holder.binding.imageViewViewAdapter)

        holder.binding.imageViewViewAdapter.setOnClickListener {
            method.onClick(context as Activity,position, string, "") { position, type, data ->
                backResult.invoke(position, type, data)
            }
        }

    }

    override fun getItemCount(): Int {
        return stringList.size
    }

    inner class ViewHolder(val binding: ViewAdapterBinding) : RecyclerView.ViewHolder(binding.root)
}