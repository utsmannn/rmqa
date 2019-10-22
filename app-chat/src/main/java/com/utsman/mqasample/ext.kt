@file:Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package com.utsman.mqasample

import android.app.ProgressDialog
import android.content.Context
import android.util.Log

fun logi(msg: String?) = Log.i("rmqa-sample", msg)
fun loge(msg: String?) = Log.e("rmqa-sample", msg)

fun Context.saveUserPref(username: String) {
    getSharedPreferences("chat", Context.MODE_PRIVATE).edit().putString("user", username).apply()
}

fun Context.getUserPref(): String {
    return getSharedPreferences("chat", Context.MODE_PRIVATE).getString("user", "user") ?: "user"
}


class ProgressHelper {
    private var mProgressDialog: ProgressDialog? = null

    fun showProgressDialog(context: Context) {
        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog(context)
            mProgressDialog!!.setMessage("Loading...")
            mProgressDialog!!.isIndeterminate = true
        }

        mProgressDialog!!.show()
    }

    fun hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog!!.isShowing) {
            mProgressDialog!!.dismiss()
        }
    }
}