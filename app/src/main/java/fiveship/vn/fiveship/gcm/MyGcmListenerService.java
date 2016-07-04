package fiveship.vn.fiveship.gcm;

/**
 * Created by BVN on 20/01/2016.
 */
import android.os.Bundle;

import com.google.android.gms.gcm.GcmListenerService;

import org.json.JSONException;
import org.json.JSONObject;

import fiveship.vn.fiveship.model.NotificationItem;
import fiveship.vn.fiveship.utility.Notification;

public class MyGcmListenerService extends GcmListenerService {

    private static final String TAG = "MyGcmListenerService";

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("message");
        //Log.d(TAG, "From: " + from);
        //Log.d(TAG, "Message: " + message);

        if (from.startsWith("/topics/")) {
            // message received from some topic.
        } else {
            // normal downstream message.
        }

        // [START_EXCLUDE]
        /**
         * Production applications would usually process the message here.
         * Eg: - Syncing with server.
         *     - Store message in local database.
         *     - Update UI.
         */

        /**
         * In some cases it may be useful to show a notification indicating to the user
         * that a message was received.
         */
        sendNotification(message);
        // [END_EXCLUDE]
    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(String message) {

        NotificationItem notify = null;

        try {

            JSONObject jData = new JSONObject(message);

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

        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            Notification.showNotification(this, notify, true);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
