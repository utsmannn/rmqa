package com.utsman.rmqa;

import com.utsman.rmqa.event.PublishEvent;
import com.utsman.rmqa.event.PublishSingleEvent;
import com.utsman.rmqa.listener.ConnectionListener;
import com.utsman.rmqa.listener.DeliveryListener;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

public class Rmqa {

    public class TYPE {
        public static final String FANOUT = "fanout";
        public static final String DIRECT = "direct";
    }

    public static void connect(final RmqaConnection rmqaConnection, final String queueName, final DeliveryListener listener) {
        rmqaConnection.connect(queueName, TYPE.FANOUT, new ConnectionListener() {
            @Override
            public void onConnected() {
                rmqaConnection.registerPublisher();
                rmqaConnection.subscribe(queueName, listener);
            }
        });
    }

    public static void connect(final RmqaConnection rmqaConnection, final String queueName, final String type, final DeliveryListener listener) {
        rmqaConnection.connect(queueName, type, new ConnectionListener() {
            @Override
            public void onConnected() {
                rmqaConnection.registerPublisher();
                rmqaConnection.subscribe(queueName, listener);
            }
        });
    }

    public static void publish(String exchangeName, String senderId, JSONObject jsonObject) {
        PublishEvent publishEvent = new PublishEvent(exchangeName, senderId, jsonObject);
        EventBus.getDefault().post(publishEvent);
    }

    public static void publishTo(String queueName, String senderId, JSONObject jsonObject) {
        PublishSingleEvent singleEvent = new PublishSingleEvent(queueName, senderId, jsonObject);
        EventBus.getDefault().post(singleEvent);
    }

    public static void disconnect(RmqaConnection rmqaConnection) {
        if (rmqaConnection != null) {
            rmqaConnection.disconnect();
        }
    }
}
