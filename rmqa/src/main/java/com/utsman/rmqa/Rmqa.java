package com.utsman.rmqa;

import android.util.Log;

import com.utsman.rmqa.event.PublishEvent;
import com.utsman.rmqa.listener.ConnectionListener;
import com.utsman.rmqa.listener.DeliveryListener;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

public class Rmqa {

    public static void connect(RmqaApp rmqaApp, String queueName, ConnectionListener listener) {
        rmqaApp.connect(queueName, listener);
    }

    public static void subscribe(RmqaApp rmqaApp, String queueName, DeliveryListener listener) {
        rmqaApp.registerPublisher();
        rmqaApp.subscribe(queueName, listener);
    }

    public static void publish(String exchangeName, String senderId, JSONObject jsonObject) {
        PublishEvent publishEvent = new PublishEvent(exchangeName, senderId, jsonObject);
        boolean hasObserver = EventBus.getDefault().hasSubscriberForEvent(publishEvent.getClass());

        if (hasObserver) {
            EventBus.getDefault().post(publishEvent);
        } else  {
            Log.e("anjay", "publish error: no subscriber or connection");
        }
    }

    public static void disconnect(RmqaApp rmqaApp) {
        if (rmqaApp != null) {
            rmqaApp.disconnect();
        }
    }
}
