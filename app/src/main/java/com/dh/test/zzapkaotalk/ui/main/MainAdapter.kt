package com.dh.test.zzapkaotalk.ui.main

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dh.test.zzapkaotalk.R
import com.dh.test.zzapkaotalk.databinding.LayoutRoomBinding
import com.dh.test.zzapkaotalk.model.RoomModel

class MainAdapter(private val context: Context,
                  val viewModel: MainViewModel)
    : ListAdapter<RoomModel, MainAdapter.MainViewHolder>(ItemDiffCallback()){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        return MainViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.layout_room,
                parent,
                false)
        )
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class MainViewHolder(private val binding: LayoutRoomBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {
            binding.viewModel = viewModel
            binding.position = position
            binding.executePendingBindings()
        }
    }
}

class ItemDiffCallback : DiffUtil.ItemCallback<RoomModel>() {

    override fun areItemsTheSame(oldItem: RoomModel, newItem: RoomModel): Boolean {
        return oldItem.id == newItem.id && oldItem.lastChat == newItem.lastChat
    }

    override fun areContentsTheSame(oldItem: RoomModel, newItem: RoomModel): Boolean {
        return oldItem == newItem
    }
}