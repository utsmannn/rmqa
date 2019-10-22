package com.utsman.rmqa;

import android.util.Log;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;
import com.utsman.rmqa.listener.DeliveryListenerRaw;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

class RmqaApp {

    private Connection connection;
    private Channel channel;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    RmqaApp() {
    }

    void connect(final String connectionName,
                 final String exchangeName,
                 final String queueName,
                 final String url,
                 final String routingKey,
                 final boolean clear,
                 final String type) {

        final ConnectionFactory factory = new ConnectionFactory();

        try {
            factory.setUri(url);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        factory.setAutomaticRecoveryEnabled(true);

        try {
            connection = factory.newConnection(connectionName);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

        Disposable connectionDisposable = Observable.just(connection)
                .doOnNext(new Consumer<Connection>() {
                    @Override
                    public void accept(Connection connection) throws Exception {
                        channel = connection.createChannel();
                        channel.exchangeDeclare(exchangeName, type, true);
                        channel.basicQos(1);
                        channel.queueDeclare(queueName, true, clear, false, null);

                        channel.queueBind(queueName, exchangeName, routingKey);
                    }
                })
                .doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {

                    }
                })
                .subscribe(new Consumer<Connection>() {
                    @Override
                    public void accept(Connection connection) throws Exception {
                        Log.i("anjay", "Channel ready");

                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                        Log.e("anjay", "connection fail " + throwable.getLocalizedMessage());
                    }
                });

        compositeDisposable.add(connectionDisposable);
    }

    void publish(String routingKey, String exchangeName, JSONObject jsonObject, String senderId) {

        try {
            JSONObject data = new JSONObject();
            data.put("sender_id", senderId);
            data.put("body", jsonObject);

            final byte[] body = String.valueOf(data).getBytes();

            AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
                    .type("text/plain")
                    .priority(1)
                    .build();

            channel.basicPublish(exchangeName, routingKey, properties, body);
            Log.i("anjay", "publish: try publish --> " + jsonObject.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    void publishTo(String queueName, JSONObject jsonObject, String senderId) {
        try {
            JSONObject data = new JSONObject();
            data.put("sender_id", senderId);
            data.put("body", jsonObject);

            final byte[] body = String.valueOf(data).getBytes();

            channel.basicPublish("", queueName, false, false, null, body);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void subscribe(final String queueName, final DeliveryListenerRaw listener) {
        try {
            channel.basicConsume(queueName, false, "tag_of_"+queueName, new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    super.handleDelivery(consumerTag, envelope, properties, body);
                    String routingKey = envelope.getRoutingKey();
                    long deliveryTag = envelope.getDeliveryTag();

                    Log.i("anjay", "Msg Deliver --> " + new String(body));

                    try {
                        JSONObject jsonObject = new JSONObject(new String(body));
                        String senderId = jsonObject.getString("sender_id");

                        Log.i("anjay", "Msg Deliver, routing key --> " + routingKey + " from --> " + senderId);
                        listener.onRawJson(jsonObject);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    channel.basicAck(deliveryTag, false);
                }

                @Override
                public void handleConsumeOk(String consumerTag) {
                    super.handleConsumeOk(consumerTag);
                    Log.i("anjay", "Consume ready");
                    try {
                        channel.basicGet(queueName, true);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
                    super.handleShutdownSignal(consumerTag, sig);
                    //sig.fillInStackTrace();
                    Log.i("anjay", "Shutdown signal");
                }


                @Override
                public void handleRecoverOk(String consumerTag) {
                    super.handleRecoverOk(consumerTag);
                    Log.i("anjay", "Recover Ok");
                }

                @Override
                public void handleCancel(String consumerTag) throws IOException {
                    super.handleCancel(consumerTag);
                    Log.i("anjay", "Cancel");
                }

                @Override
                public void handleCancelOk(String consumerTag) {
                    super.handleCancelOk(consumerTag);
                    Log.i("anjay", "Cancel Ok");
                }

            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void disconnect() {
        Observable.just(connection)
                .subscribeOn(Schedulers.io())
                .doOnNext(new Consumer<Connection>() {
                    @Override
                    public void accept(Connection connection) throws Exception {
                        connection.close();
                        channel.close();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Connection>() {
                    @Override
                    public void accept(Connection connection) throws Exception {
                        Log.i("anjay", "connection close Ok");
                        compositeDisposable.dispose();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.e("anjay", "connection close failed");
                        compositeDisposable.dispose();
                    }
                })
                .dispose();
    }
}
