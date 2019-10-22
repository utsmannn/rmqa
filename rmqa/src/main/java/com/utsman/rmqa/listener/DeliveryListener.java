package com.utsman.rmqa.listener;

import org.json.JSONObject;

public interface DeliveryListener {
    void onDelivery(String senderId, JSONObject jsonObject);
}
