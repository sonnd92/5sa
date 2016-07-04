package fiveship.vn.fiveship.other;

/**
 * Created by BVN-PC on 16/03/2016.
 */
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import fiveship.vn.fiveship.service.adapter.PostLocation;
import fiveship.vn.fiveship.utility.GPSTracker;
import fiveship.vn.fiveship.utility.SessionManager;
import fiveship.vn.fiveship.utility.Utils;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        try {

            SessionManager sessionManager = new SessionManager(context);
            GPSTracker gps = new GPSTracker(context);
            if(sessionManager.isShipper() && gps.canGetLocation() && Utils.isConnectingToInternet(context)) {

                new PostLocation(context).execute(String.valueOf(sessionManager.getShipperId()),
                        String.valueOf(sessionManager.getShopId()),
                        String.valueOf(gps.getLatitude()),
                        String.valueOf(gps.getLongitude()),
                        sessionManager.getConnection(),
                        String.valueOf(sessionManager.isShipper()),
                        Utils.getUuid(context));
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
