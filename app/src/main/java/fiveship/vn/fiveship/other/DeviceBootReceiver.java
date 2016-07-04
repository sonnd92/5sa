package fiveship.vn.fiveship.other;

/**
 * Created by BVN-PC on 16/03/2016.
 */

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

import fiveship.vn.fiveship.activity.FShipService;
import fiveship.vn.fiveship.utility.SessionManager;

public class DeviceBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            /* Setting the alarm here */
            AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            SessionManager sessionManager = new SessionManager(context);

            if (sessionManager.isLoggedIn() && sessionManager.isShipper()) {

                Intent alarmIntent = new Intent(context, AlarmReceiver.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
                Calendar c = Calendar.getInstance();
                manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), 30000, pendingIntent);

            }

            if (sessionManager.isNotificationOn()) {

                Intent pushIntent = new Intent(context, FShipService.class);
                PendingIntent pendingPush = PendingIntent.getService(context, 0, pushIntent, 0);
                manager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 10000, pendingPush);
            }
        }
    }
}
