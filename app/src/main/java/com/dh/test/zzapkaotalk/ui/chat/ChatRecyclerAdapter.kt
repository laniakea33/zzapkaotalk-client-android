package com.dh.test.zzapkaotalk.ui.chat

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dh.test.zzapkaotalk.OsUtil
import com.dh.test.zzapkaotalk.R
import com.dh.test.zzapkaotalk.databinding.*
import com.dh.test.zzapkaotalk.model.ChatModel

class SecondRecyclerAdapter(
    private val context: Context,
    val viewModel: ChatViewModel
) : ListAdapter<ChatModel, RecyclerView.ViewHolder>(ItemDiffCallback()) {

    private val deviceId = OsUtil.getDeviceId(context)

    override fun getItemViewType(position: Int): Int {
        val chat = viewModel.chatState.value?.get(position) ?: return -1
        return when(chat.type) {
            "system" -> {
                ViewType.System.ordinal
            }
            "text" -> {
                if (chat.user.deviceId == deviceId) {
                    ViewType.MyText.ordinal
                } else {
                    ViewType.OthersText.ordinal
                }
            }
            "image" -> {
                if (chat.user.deviceId == deviceId) {
                    ViewType.MyImage.ordinal
                } else {
                    ViewType.OthersImage.ordinal
                }
            }
            else -> -1
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            ViewType.MyText.ordinal -> {
                MyTextChatHolder(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(context),
                        R.layout.layout_chat_text_my,
                        parent,
                        false
                    )
                )
            }
            ViewType.OthersText.ordinal -> {
                OthersTextChatHolder(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(context),
                        R.layout.layout_chat_text_others,
                        parent,
                        false
                    )
                )
            }
            ViewType.MyImage.ordinal -> {
                MyImageChatHolder(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(context),
                        R.layout.layout_chat_image_my,
                        parent,
                        false
                    )
                )
            }
            ViewType.OthersImage.ordinal -> {
                OthersImageChatHolder(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(context),
                        R.layout.layout_chat_image_others,
                        parent,
                        false
                    )
                )
            }
            else -> {
                SystemChatHolder(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(context),
                        R.layout.layout_chat_system,
                        parent,
                        false
                    )
                )
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder) {
            is MyTextChatHolder -> holder.bind(position)
            is OthersTextChatHolder -> holder.bind(position)
            is SystemChatHolder -> holder.bind(position)
            is MyImageChatHolder -> holder.bind(position)
            is OthersImageChatHolder -> holder.bind(position)
        }
    }

    inner class OthersTextChatHolder(private val binding: LayoutChatTextOthersBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.viewModel = viewModel
            binding.position = position
            binding.executePendingBindings()
        }
    }

    inner class MyTextChatHolder(private val binding: LayoutChatTextMyBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.viewModel = viewModel
            binding.position = position
            binding.executePendingBindings()
        }
    }

    inner class OthersImageChatHolder(private val binding: LayoutChatImageOthersBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.viewModel = viewModel
            binding.position = position
            binding.executePendingBindings()
        }
    }

    inner class MyImageChatHolder(private val binding: LayoutChatImageMyBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.viewModel = viewModel
            binding.position = position
            binding.executePendingBindings()
        }
    }

    inner class SystemChatHolder(private val binding: LayoutChatSystemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.viewModel = viewModel
            binding.position = position
            binding.executePendingBindings()
        }
    }

    enum class ViewType {
        MyText, OthersText, MyImage, OthersImage, System
    }
}

class ItemDiffCallback : DiffUtil.ItemCallback<ChatModel>() {

    override fun areItemsTheSame(oldItem: ChatModel, newItem: ChatModel): Boolean {
        return oldItem.dateTime == newItem.dateTime
    }

    override fun areContentsTheSame(oldItem: ChatModel, newItem: ChatModel): Boolean {
        return oldItem == newItem
    }
}