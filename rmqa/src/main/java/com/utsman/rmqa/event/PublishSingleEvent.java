package com.utsman.rmqa.event;

import org.json.JSONObject;

public class PublishSingleEvent {
    public String queueName;
    public String senderId;
    public JSONObject jsonData;

    public PublishSingleEvent(String queueName, String senderId, JSONObject jsonData) {
        this.queueName = queueName;
        this.senderId = senderId;
        this.jsonData = jsonData;
    }
}
