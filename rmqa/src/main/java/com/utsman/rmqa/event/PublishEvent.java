package com.utsman.rmqa.event;

import org.json.JSONObject;

public class PublishEvent {
    public String exchangeName;
    public String senderId;
    public JSONObject jsonData;

    public PublishEvent(String exchangeName, String senderId, JSONObject jsonData) {
        this.exchangeName = exchangeName;
        this.senderId = senderId;
        this.jsonData = jsonData;
    }
}