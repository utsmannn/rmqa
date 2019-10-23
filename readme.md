# rmqa
[ ![Download](https://api.bintray.com/packages/kucingapes/utsman/com.utsman.rmqa/images/download.svg) ](https://bintray.com/kucingapes/utsman/com.utsman.rmqa/_latestVersion)

### Rabbit Message Queue for Android
Implementation minimalized feature of RabbitMQ in android. Goodbye to the fcm API key.

> RabbitMQ is a message-queueing software also known as a message broker or queue manager. Simply said; it is software where queues are defined, to which applications connect in order to transfer a message or messages.
> 

rmqa library minimizes RabbitMQ feature for Android, making it easy to use even without knowledge of Message Brokers. While it only supports broadcast messaging and one-way messaging features.

## Download
```gradle
implementation 'com.utsman.rmqa:rmqa:0.0.2'
```

## Prepare
Open [https://www.cloudamqp.com](https://www.cloudamqp.com) and login or sign up. Create your instance, get the server, username, password and vhost. <br>
For detail, you can read offcial RabbitMQ guide for cloudamqp [here](https://www.cloudamqp.com/blog/2015-05-18-part1-rabbitmq-for-beginners-what-is-rabbitmq.html#set-up-a-rabbitmq-instance)

## Let's Coding !

### 1. Setup connection
```kotlin
val rmqaConnection = RmqaConnection.Builder(this)
        .setServer("server")
        .setUsername("username")
        .setPassword("password")
        .setVhost("vhost")
        .setExchangeName("exchange_name")
        .setConnectionName("connection_name")
        .setRoutingKey("route_key")
        .setAutoClearQueue(true) // By default it is `false`, when the connection is closed, the queue will be cleared
        .build()
```

### 2. Connect and publish for broadcasting message
#### Connect
```kotlin
Rmqa.connect(rmqaConnection, queueName) { senderId, data ->
      // your code when message arrived
  }
```

Messages will be received at each ```queueName``` that has the same ```exhangeName```. ```queueName``` is buffer the stores messages, you can only create one ```queueName``` for one ```rmqaConnection```.

#### Publish / broadcast message
```kotlin
Rmqa.publish(exchangeName, senderId, data)
```
A message will be sent to each ```queueName``` owned by ```exchangeName```.

<p align="center">
  <img src="https://i.ibb.co/mqP1CqQ/rmqa1.png"/>
</p>


## 3. Connect and publish for single receiver
#### Connect
```kotlin
Rmqa.connect(rmqaConnection, queueName, Rmqa.TYPE.DIRECT) { senderId, data ->
      // your code when message arrived
  }
```

For this case, the message will be received specifically by the defined ```queueName``` without passing through ```exchangeName```. You cannot multiple connect in one ```RmqaConnection```.

#### Publish single receiver
```kotlin
Rmqa.publishTo(queueName, senderId, data)
```

<p align="center">
  <img src="https://i.ibb.co/nRstgWR/rmqa2.png"/>
</p>


[Sample chat app](https://github.com/utsmannn/rmqa/tree/master/app-chat) <br>
[Article on Medium (in bahasa)](https://medium.com/@utsmannn/cerita-si-anggun-membuat-aplikasi-chat-sederhana-di-android-tanpa-fcm-989956dffc6b)

* * *
```
Copyright 2019 Muhammad Utsman

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```