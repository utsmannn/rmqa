package com.utsman.mqasample.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.utsman.mqasample.R
import com.utsman.mqasample.adapter.ChatAdapter
import com.utsman.mqasample.model.Chat
import com.utsman.mqasample.util.getUserPref
import com.utsman.mqasample.util.logi
import com.utsman.rmqa.Rmqa
import kotlinx.android.synthetic.main.activity_chat.*
import org.json.JSONObject

class ChatActivity : AppCompatActivity() {

    private val chats: MutableList<Chat> = mutableListOf()
    private lateinit var chatAdapter: ChatAdapter

    private val receiverChat = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val user = intent.getStringExtra("user") as String
            val body = intent.getStringExtra("body") as String
            val time = intent.getLongExtra("time", 0)

            val chat = Chat(user, body, time)
            chatAdapter.addChat(chat)
            main_chat_list.smoothScrollToPosition(chats.size)
            logi(body)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        setupRegisterBroadcastReceiver()
        setupRecyclerView()

        val user = getUserPref()
        btn_chat_now.setOnClickListener {
            val body = input_chat.text.toString()
            val data = JSONObject()
            data.put("time", System.currentTimeMillis())
            data.put("body", body)

            Rmqa.publish("chat", user, data)

            input_chat.setText("")
        }
    }

    private fun setupRegisterBroadcastReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction("message_coming")
        registerReceiver(receiverChat, intentFilter)
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(this, chats)
        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        main_chat_list.layoutManager = layoutManager
        main_chat_list.adapter = chatAdapter
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiverChat)
    }
}
