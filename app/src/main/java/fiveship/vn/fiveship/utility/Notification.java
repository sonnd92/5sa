package fiveship.vn.fiveship.utility;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.action.UpdateNotificationReceiveAction;
import fiveship.vn.fiveship.activity.shipper.DetailOrderActivity;
import fiveship.vn.fiveship.activity.shipper.MainActivity;
import fiveship.vn.fiveship.activity.NotificationActivity;
import fiveship.vn.fiveship.activity.shipper.ShipHistoryTradeActivity;
import fiveship.vn.fiveship.activity.shop.ShopActivity;
import fiveship.vn.fiveship.activity.shop.ShopListShipperAssignShip;
import fiveship.vn.fiveship.activity.shop.ShopShipDetailActivity;
import fiveship.vn.fiveship.model.NotificationItem;

/**
 * Created by sonnd on 14/10/2015.
 */
public class Notification {

    public static void showNotification(final Context context, final NotificationItem notify, boolean isGCM) {

        try {

            if (notify == null) {
                return;
            }
            Intent intent = null;
            PendingIntent pendingIntent;
            NotificationCompat.Builder notificationBuilder;
            SessionManager sessionManager = new SessionManager(context);

            if (!sessionManager.isLoggedIn()) {
                return;
            }

            if (!sessionManager.getKeyUseGcm() && isGCM) {
                return;
            }

            boolean isShipper = sessionManager.isShipper() && sessionManager.getShipperId() == notify.getShipperId();

            boolean isShop = !sessionManager.isShipper() && sessionManager.getShopId() == notify.getShopId();

            if (notify.getTypeId() == 1 && isShipper) {
                intent = new Intent(context, DetailOrderActivity.class);
                intent.putExtra("OrderId", notify.getShipId());
            }

            if (notify.getTypeId() == 2 && isShipper) {
                intent = new Intent(context, DetailOrderActivity.class);
                intent.putExtra("OrderId", notify.getShipId());
            }

            if (notify.getTypeId() == 3 && isShipper) {
                intent = new Intent(context, DetailOrderActivity.class);
                intent.putExtra("OrderId", notify.getShipId());
            }

            if (notify.getTypeId() == 4 && isShop) {
                intent = new Intent(context, ShopListShipperAssignShip.class);
                intent.putExtra("id", notify.getShipId());
            }

            if (notify.getTypeId() == 5 && isShipper) {
                intent = new Intent(context, DetailOrderActivity.class);
                intent.putExtra("OrderId", notify.getShipId());
                intent.putExtra("NotifyItem", notify);
                intent.putExtra("NotifyType", 5);
                try {
                    Utils.CallToShopDialog(context, notify, true).show();
                }catch (Exception ex){
                    //
                }
            }

            if (notify.getTypeId() == 6 && isShipper) {
                intent = new Intent(context, DetailOrderActivity.class);
                intent.putExtra("OrderId", notify.getShipId());
            }

            if (notify.getTypeId() == 6 && isShop) {
                intent = new Intent(context, ShopShipDetailActivity.class);
                intent.putExtra("id", notify.getShipId());
            }

            if (notify.getTypeId() == 7 && isShipper) {
                intent = new Intent(context, ShipHistoryTradeActivity.class);
            }

            if (notify.getTypeId() == 8 && (isShipper || isShop)) {
                intent = new Intent(context, NotificationActivity.class);
            }

            if (notify.getTypeId() == 9) {
                intent = new Intent(context, isShipper ? MainActivity.class : ShopActivity.class);
            }

            if (notify.getTypeId() == 10) {
                intent = new Intent(context, ShopShipDetailActivity.class);
                long currentTime = new Date().getTime();
                intent.putExtra("OrderId", notify.getShipId());
                JSONObject notifyData = new JSONObject(notify.getData());

                intent.putExtra("NotifyType", 10);
                intent.putExtra("SecondsLeft", notifyData.getInt("Second"));
                intent.putExtra("NotifyTime", currentTime);
                intent.putExtra("Content", notifyData.getString("Content"));
                intent.putExtra("NumberShipper", notifyData.getInt("NumberShipper"));
                intent.putExtra("ShipName", notifyData.getString("ShipName"));
            }

            if (intent == null) {
                return;
            }
//            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
//            // Adds the back stack
//            stackBuilder.addParentStack(DetailOrderActivity.class);
//            // Adds the Intent to the top of the stack
//            stackBuilder.addNextIntent(intent);
            //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            //PendingIntent pendingIntent = PendingIntent.getActivity(context, notify.getNotifyId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

            pendingIntent = PendingIntent.getActivity(context, (int)System.currentTimeMillis(), intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            long[] pattern = {100, 200, 100, 500};

            notificationBuilder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLights(Color.BLUE, 500, 500)
                    .setContentTitle(notify.getTitle())
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(notify.getName()))
                    .setContentText(notify.getName())
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setVibrate(pattern)
                    .setContentIntent(pendingIntent);

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(notify.getNotifyId(), notificationBuilder.build());

            UpdateNotificationReceiveAction.get_instance(context).post(String.valueOf(notify.getId()));

            Intent callback = new Intent(sessionManager.isShipper() ? Config.RECEIVE_JSON_SHIPPER : Config.RECEIVE_JSON_SHOP);

            LocalBroadcastManager.getInstance(context).sendBroadcast(callback);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
