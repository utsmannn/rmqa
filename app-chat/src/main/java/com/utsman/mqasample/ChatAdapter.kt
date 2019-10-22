package com.utsman.mqasample

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_sender.view.*

class ChatAdapter(private val context: Context, private val chats: MutableList<Chat>) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = when (viewType) {
            SENDER -> LayoutInflater.from(context).inflate(R.layout.item_sender, parent, false)
            else -> LayoutInflater.from(context).inflate(R.layout.item_receiver, parent, false)
        }
        return ChatViewHolder(view)
    }

    override fun getItemCount(): Int = chats.size

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chats[position]
        holder.bind(chat)
    }

    override fun getItemViewType(position: Int): Int {
        val me = context.getUserPref()
        return if (chats[position].user == me) {
            SENDER
        } else {
            RECEIVER
        }
    }

    fun addChat(chat: Chat) {
        chats.add(chat)
        notifyDataSetChanged()
    }

    class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(chat: Chat) = itemView.run {
            text_user.text = chat.user
            text_body.text = chat.body
        }
    }

    companion object {
        private const val SENDER = 0
        private const val RECEIVER = 1
    }
}