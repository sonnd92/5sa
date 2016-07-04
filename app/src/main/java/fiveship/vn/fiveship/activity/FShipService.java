package fiveship.vn.fiveship.activity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;
import fiveship.vn.fiveship.model.NotificationItem;
import fiveship.vn.fiveship.utility.Notification;
import fiveship.vn.fiveship.utility.SessionManager;
import fiveship.vn.fiveship.utility.Utils;

public class FShipService extends Service {

    private static final String TAG = "FShipService";
    private final String TOPIC_PUBLISH_RECEIVED = "/5ship/notify_received";
    private final String TOPIC_NOTIFY = "/5ship/notify";
    public static final String TOPIC_CONNECTED = "/5ship/connected";
    public static final String TOPIC_WILL = "/5ship/will";
    public static final String SERVER_MQTT = "tcp://mqtt.5ship.vn:1883";
    public static final String USERNAME_MQTT = "5ship";
    public static final String PASSWORD_MQTT = "bvn5ship@2016";
    private MqttAndroidClient client;
    private MqttConnectOptions options;
    private IMqttToken token;
    private SessionManager sessionManager;
    private static final long INITIAL_RETRY_INTERVAL = 1000 * 10;
    private static final long KEEP_ALIVE_INTERVAL = 1000 * 60 * 15;
    private String clientId;

    public FShipService() {
    }

    @Override
    public void onCreate() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        connect();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the services, if it has been started
        stop();
    }

    private void connect(){

        sessionManager = new SessionManager(this);

        clientId = Utils.getMQTTClientId(this, sessionManager, "");

        options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setUserName(USERNAME_MQTT);
        options.setPassword(PASSWORD_MQTT.toCharArray());
        try {
            options.setWill(TOPIC_WILL, clientId.getBytes("UTF-8"), 1, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //options.setConnectionTimeout(5000);
        //options.setKeepAliveInterval(1000 * 60 * 90);

        client = new MqttAndroidClient(this.getApplicationContext(), SERVER_MQTT, clientId);

        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                reconnectIfNecessary();
            }

            @Override
            public void messageArrived(String topic, final MqttMessage message) throws Exception {

                new Handler(getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {

                        NotificationItem notify;

                        try {

                            JSONObject jData = new JSONObject(new String(message.getPayload()));

                            notify = new NotificationItem();

                            notify.setId(jData.has("Id") ? jData.getInt("Id") : 0);

                            notify.setTypeId(jData.has("TypeId") ? jData.getInt("TypeId") : 0);

                            notify.setShipId(jData.has("ShipId") ? jData.getInt("ShipId") : 0);

                            notify.setShipperId(jData.has("ShipperId") ? jData.getInt("ShipperId") : 0);

                            notify.setShopId(jData.has("ShopId") ? jData.getInt("ShopId") : 0);

                            notify.setNotifyId(jData.has("NotifyId") ? jData.getInt("NotifyId") : 0);

                            notify.setName(jData.has("Name") ? jData.getString("Name") : "");

                            notify.setTitle(jData.has("Title") ? jData.getString("Title") : "");

                            notify.setData(jData.has("Data") ? jData.getString("Data") : "");

                            Notification.showNotification(FShipService.this, notify, false);

                            if(notify.getId() != 0){
                                publishMessageReceived(notify.getId());
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                });

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

        try {
            token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Log.d(TAG, "onSuccess");
                    subscribeNotify();
                    publishConnected();
                    startKeepAlives();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.d(TAG, "onFailure");
                    reconnectIfNecessary();
                }

            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void subscribeNotify() {
        try {
            String topicUser = "/5ship/" + (sessionManager.isShipper() ? "shipper/" : "shop/") + (sessionManager.isShipper() ? sessionManager.getShipperId() : sessionManager.getShopId());
            String[] arrTopic = new String[]{TOPIC_NOTIFY, topicUser };
            int[] arrQos = new int[]{2, 2};
            client.subscribe(arrTopic, arrQos);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void publishConnected() {
        try {
            client.publish(TOPIC_CONNECTED, clientId.getBytes("UTF-8"), 1, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void publishMessageReceived(int notifyId) {
        try {
            String message = "{\"clientId\":\"" + clientId + "\", \"id\":\"" + notifyId + "\"}";
            client.publish(TOPIC_PUBLISH_RECEIVED, message.getBytes("UTF-8"), 2, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Schedule application level keep-alives using the AlarmManager
    private void startKeepAlives() {
        Intent i = new Intent(this, FShipService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        AlarmManager alarmMgr = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + KEEP_ALIVE_INTERVAL, KEEP_ALIVE_INTERVAL, pi);
    }

    // Remove all scheduled keep alives
    private void stopKeepAlives() {
        Intent i = new Intent(this, FShipService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        AlarmManager alarmMgr = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarmMgr.cancel(pi);
    }

    public void scheduleReconnect() {
        // Schedule a reconnect using the alarm manager.
        Intent i = new Intent(this, FShipService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        AlarmManager alarmMgr = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarmMgr.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + INITIAL_RETRY_INTERVAL, pi);
    }

    private synchronized void reconnectIfNecessary() {
        stop();
        scheduleReconnect();
    }

    public void cancelReconnect() {
        Intent i = new Intent(this, FShipService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        AlarmManager alarmMgr = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarmMgr.cancel(pi);
    }

    public synchronized void stop() {
        // Any existing reconnect timers should be removed, since we explicitly stopping the service.
        cancelReconnect();

        // Destroy the MQTT connection if there is one
        if (client != null) {
            try {
                client.unregisterResources();
                client.disconnect();
                client = null;
                token = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
