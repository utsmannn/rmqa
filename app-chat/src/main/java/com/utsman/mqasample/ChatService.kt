package com.utsman.mqasample

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.utsman.rmqa.Rmqa
import com.utsman.rmqa.RmqaConnection

class ChatService : Service() {

    private var rmqaConnection: RmqaConnection? = null

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        rmqaConnection = RmqaConnection.Builder(this)
            .setServer("owl.rmq.cloudamqp.com")
            .setUsername("lgkqorva")
            .setPassword("qRVr7HtMAO0q4fUqK9oOKN4G8o31d5nZ")
            .setVhost("lgkqorva")
            .setExchangeName("chat")
            .setConnectionName("connection")
            .setRoutingKey("route_chat")
            .setAutoClearQueue(false)
            .build()

        val user = getUserPref()

        Rmqa.connect(rmqaConnection, user) { senderId, data ->
            logi("$data from $senderId")

            val i = Intent("message_coming")
            i.putExtra("user", senderId)
            i.putExtra("body", data["body"] as String)
            i.putExtra("time", data["time"] as Long)
            sendBroadcast(i)
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Rmqa.disconnect(rmqaConnection)
    }
}