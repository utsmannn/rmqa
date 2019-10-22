package com.utsman.rmqa.listener

import org.json.JSONObject

internal interface DeliveryListenerRaw {
    fun onRawJson(jsonObject: JSONObject)
}