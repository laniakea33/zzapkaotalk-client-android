package com.dh.test.zzapkaotalk.databinding

import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dh.test.zzapkaotalk.R
import com.dh.test.zzapkaotalk.model.ChatModel
import com.dh.test.zzapkaotalk.model.RoomModel
import com.dh.test.zzapkaotalk.ui.chat.SecondRecyclerAdapter
import com.dh.test.zzapkaotalk.ui.main.MainAdapter
import java.text.SimpleDateFormat
import java.util.*

@BindingAdapter("setImgSrc")
fun setImgSrc(view: ImageView, url: String?) {
    Glide.with(view)
        .load(url)
        .placeholder(R.drawable.ic_baseline_account_circle_24)
        .into(view)
}

@BindingAdapter("listItem")
fun <T> applyListItem(view: RecyclerView, list: List<T>?) {
    Log.d("dhlog", "applyListItem : ${list?.size}")
    list?.let {
        val adapter = view.adapter
        if (adapter is SecondRecyclerAdapter) {
            adapter.submitList(it as List<ChatModel>)
        } else if (adapter is MainAdapter) {
            adapter.submitList(it as List<RoomModel>)
        }
    }
}

@BindingAdapter("timeText")
fun timeText(view: TextView, time: Date?) {
    view.text = if (time != null) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(time)
    } else {
        "null"
    }
}