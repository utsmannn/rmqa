package com.utsman.mqasample

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
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
        createNotificationChannel()
        val intentFilter = IntentFilter()
        intentFilter.addAction("message_coming")
        registerReceiver(receiverChat, intentFilter)

        chatAdapter = ChatAdapter(this, chats)
        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        main_chat_list.layoutManager = layoutManager
        main_chat_list.adapter = chatAdapter

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

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "chat"
            val descriptionText = "chat notification"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("chat_id", name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiverChat)
    }
}
