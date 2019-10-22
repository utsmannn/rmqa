package com.utsman.rmqa;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.utsman.rmqa.event.PublishEvent;
import com.utsman.rmqa.event.PublishSingleEvent;
import com.utsman.rmqa.listener.ConnectionListener;
import com.utsman.rmqa.listener.DeliveryListener;
import com.utsman.rmqa.listener.DeliveryListenerRaw;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;
import org.json.JSONObject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import kotlinx.android.parcel.Parcelize;

@Parcelize
public class RmqaConnection {

    Builder builder;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private RmqaApp rmqaApp = new RmqaApp();
    private MutableLiveData<JSONObject> mutableLiveData = new MutableLiveData<>();

    private RmqaConnection(Builder builder) {
        this.builder = builder;
    }

    public static class Builder {
        private String url;
        private String server;
        private String username;
        private String password;
        private String vhost;
        private String connectionName;
        private String exchangeName;
        private String routingKey;
        private boolean clear = false;

        private Context context;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder setServer(String server) {
            this.server = server;
            return this;
        }

        public Builder setUsername(String username) {
            this.username = username;
            return this;
        }

        public Builder setPassword(String password) {
            this.password = password;
            return this;
        }

        public Builder setVhost(String vhost) {
            this.vhost = vhost;
            return this;
        }

        public Builder setConnectionName(String connectionName) {
            this.connectionName = connectionName;
            return this;
        }

        public Builder setExchangeName(String exchangeName) {
            this.exchangeName = exchangeName;
            return this;
        }

        public Builder setRoutingKey(String routingKey) {
            this.routingKey = routingKey;
            return this;
        }

        public Builder setAutoClearQueue(boolean clear) {
            this.clear = clear;
            return this;
        }

        public RmqaConnection build() {
            url = "amqp://" + username + ":" + password + "@" + server + "/" + vhost;
            return new RmqaConnection(this);
        }
    }

    void connect(final String queueName, final String type, final ConnectionListener connectionListener) {
        final String exName = builder.context.getPackageName() + "." + builder.exchangeName;
        Disposable disposable = Observable.just(rmqaApp)
                .subscribeOn(Schedulers.io())
                .doOnNext(new Consumer<RmqaApp>() {
                    @Override
                    public void accept(RmqaApp rabbitConnection) {
                        rabbitConnection.connect(
                                builder.connectionName,
                                exName,
                                queueName,
                                builder.url,
                                builder.routingKey,
                                builder.clear,
                                type);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<RmqaApp>() {
                    @Override
                    public void accept(RmqaApp rabbitConnection) throws Exception {
                        connectionListener.onConnected();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.e("anjay", "connect fail");
                    }
                });

        compositeDisposable.add(disposable);

    }

    void subscribe(final String queueName, final DeliveryListener listener) {
        Disposable disposable = Observable.just(rmqaApp)
                .subscribeOn(Schedulers.newThread())
                .doOnNext(new Consumer<RmqaApp>() {
                    @Override
                    public void accept(RmqaApp rabbitConnection) {

                        rabbitConnection.subscribe(queueName, new DeliveryListenerRaw() {
                            @Override
                            public void onRawJson(JSONObject jsonObject) {
                                mutableLiveData.postValue(jsonObject);
                            }
                        });
                    }
                })
                .subscribe(new Consumer<RmqaApp>() {
                    @Override
                    public void accept(RmqaApp rabbitConnection) throws Exception {

                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {

                    }
                });

        mutableLiveData.observeForever(new Observer<JSONObject>() {
            @Override
            public void onChanged(JSONObject jsonObject) {
                try {
                    String senderId = jsonObject.getString("sender_id");
                    JSONObject data = jsonObject.getJSONObject("body");
                    listener.onDelivery(senderId, data);
                } catch (JSONException e) {
                    Log.e("anjay", "Failed parsing json");
                }
            }
        });

        compositeDisposable.add(disposable);
    }


    @Subscribe
    public void publish(final PublishEvent publishEvent) {
        final String exName = builder.context.getPackageName() + "." + publishEvent.exchangeName;
        Disposable disposable = Observable.just(rmqaApp)
                .subscribeOn(Schedulers.io())
                .doOnNext(new Consumer<RmqaApp>() {
                    @Override
                    public void accept(RmqaApp rabbitConnection) {
                        rmqaApp.publish(builder.routingKey, exName, publishEvent.jsonData, publishEvent.senderId);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();

        compositeDisposable.add(disposable);
    }

    @Subscribe
    public void publish(final PublishSingleEvent singleEvent) {
        Disposable disposable = Observable.just(rmqaApp)
                .subscribeOn(Schedulers.io())
                .doOnNext(new Consumer<RmqaApp>() {
                    @Override
                    public void accept(RmqaApp rmqaConnection) throws Exception {
                        rmqaApp.publishTo(singleEvent.queueName, singleEvent.jsonData, singleEvent.senderId);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();

        compositeDisposable.add(disposable);
    }

    void registerPublisher() {
        boolean registered = EventBus.getDefault().isRegistered(this);

        if (!registered) {
            EventBus.getDefault().register(this);
        } else  {
            Log.i("anjay", "Publisher has registerd");
        }
    }

    void disconnect() {
        compositeDisposable.dispose();
        rmqaApp.disconnect();
        EventBus.getDefault().unregister(this);
    }
}
