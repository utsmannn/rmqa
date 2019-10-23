package com.utsman.mqasample.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.utsman.mqasample.R
import com.utsman.mqasample.service.ChatService
import com.utsman.mqasample.util.ProgressHelper
import com.utsman.mqasample.util.saveUserPref
import kotlinx.android.synthetic.main.activity_launcher.*

class LauncherActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)

        val progressHelper = ProgressHelper()

        btn_login.setOnClickListener {
            val username = input_username.text.toString()
            saveUserPref(username)

            progressHelper.showProgressDialog(this)

            Handler().postDelayed({
                val chatService = Intent(this, ChatService::class.java)
                startService(chatService)

                progressHelper.hideProgressDialog()
                startActivity(Intent(this, ChatActivity::class.java))
                finish()
            }, 2000)
        }
    }
}