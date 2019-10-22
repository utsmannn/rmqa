package com.utsman.rmqa.app_sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.utsman.rmqa.Rmqa
import com.utsman.rmqa.RmqaConnection
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private var rmqaConnection: RmqaConnection? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rmqaConnection = RmqaConnection.Builder(this)
            .setServer("owl.rmq.cloudamqp.com")
            .setUsername("lgkqorva")
            .setPassword("qRVr7HtMAO0q4fUqK9oOKN4G8o31d5nZ")
            .setVhost("lgkqorva")
            .setExchangeName("mantan.test")
            .setConnectionName("connection")
            .setRoutingKey("route_chat")
            .setAutoClearQueue(true)
            .build()


        btn_test.isEnabled = false

        btn_my_queue.setOnClickListener {
            val myQueue = input_my_queue.text.toString()

            btn_test.isEnabled = true

            Rmqa.connect(rmqaConnection, myQueue, Rmqa.TYPE.DIRECT) { senderId, jsonObject ->
                val msg = jsonObject.getString("message")
                text_result.append(msg + "\n")
            }
        }


        btn_test.setOnClickListener {
            val json = JSONObject()
            json.put("message", "this message body from ${input_my_queue.text} in --> ${System.currentTimeMillis()}")

            val toQueue = input_to_queue.text.toString()
            Rmqa.publishTo(toQueue, "id-is-${input_my_queue.text}", json)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Rmqa.disconnect(rmqaConnection)
    }
}