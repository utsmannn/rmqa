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

        Rmqa.connect(rmqaConnection, "test-", Rmqa.TYPE.DIRECT) { senderId, jsonObject ->
            val msg = jsonObject.getString("message")
            text_result.append(msg + "\n")
        }

        /*Rmqa.connect(rmqaConnection, "test-s-aaa") { senderId, jsonObject ->
            val msg = jsonObject.getString("message")
            text_result.append("$msg\n $senderId")
        }*/

        btn_test.setOnClickListener {
            val json = JSONObject()
            json.put("message", "this message body")

            //Rmqa.publish("mantan.test", "mantan", json)

            Rmqa.publishTo("mantan-2", "mantan", json)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Rmqa.disconnect(rmqaConnection)
    }
}