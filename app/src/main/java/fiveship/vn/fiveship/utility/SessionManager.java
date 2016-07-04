package fiveship.vn.fiveship.utility;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import fiveship.vn.fiveship.activity.FShipService;
import fiveship.vn.fiveship.activity.LoginActivity;
import fiveship.vn.fiveship.model.CustomerItem;

/**
 * Created by sonnd on 16/10/2015.
 */
public class SessionManager {
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context mContext;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Sharedpref file name
    private static final String PREF_NAME = "LoginPref";

    // All Shared Preferences Keys
    private static final String IS_LOGIN = "isLogged";
    public static final String KEY_NAME = "name";
    public static final String KEY_AVATAR = "avatar";
    public static final String KEY_AVATAR_LABEL = "avatarLabel";
    public static final String KEY_SHIPPER_ID = "shipperId";
    public static final String KEY_ACCOUNT_ID = "accId";
    public static final String KEY_IS_SHIPPER = "isShipper";
    public static final String KEY_SHOP_ID = "shopId";
    public static final String KEY_CONNECTION = "connection";
    public static final String KEY_NUMBER_NOTIFICATION = "numberNotify";
    public static final String KEY_X_POINT = "xPoint";
    public static final String KEY_Y_POINT = "yPoint";
    public static final String KEY_USE_GCM = "isUseGCM";
    public static final String KEY_REFERRAL_CODE = "referralCode";
    public static final String KEY_IS_NOTIFICATION_ON = "isOnNotification";

    // Constructor
    public SessionManager(Context context){
        this.mContext = context;
        pref = mContext.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    /**
     * Create login session
     * */

    public void createLoginSessionCustomer(int accId,
                                           int shipperId,
                                           int shopId,
                                           String name,
                                           String avatar,
                                           String avatarLabel,
                                           String referralCode,
                                           boolean isShipper){
        // Storing login value as TRUE
        editor.putBoolean(IS_LOGIN, true);
        editor.putInt(KEY_ACCOUNT_ID, accId);
        editor.putInt(KEY_SHIPPER_ID, shipperId);
        editor.putInt(KEY_SHOP_ID, shopId);
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_AVATAR, avatar);
        editor.putString(KEY_AVATAR_LABEL, avatarLabel);
        editor.putBoolean(KEY_IS_SHIPPER, isShipper);
        editor.putString(KEY_REFERRAL_CODE,referralCode);

        // commit changes
        editor.commit();
    }

    /**
     * Check login method wil check user login status
     * If false it will redirect user to login page
     * Else won't do anything
     * */
    public boolean checkLogin(){
        // Check login status
        return this.isLoggedIn();
    }



    /**
     * Get stored session data
     * */
    public CustomerItem getCustomerInfo(){
        CustomerItem cus = new CustomerItem();
        cus.setId(pref.getInt(KEY_ACCOUNT_ID, 0));
        cus.setIsShipper(pref.getBoolean(KEY_IS_SHIPPER, false));
        cus.setFirstName("");
        cus.setLastName(pref.getString(KEY_NAME, ""));
        cus.setAvatar(pref.getString(KEY_AVATAR, ""));
        cus.setAvatarLabel(pref.getString(KEY_AVATAR_LABEL, ""));
        return cus;
    }

    /**
     * Clear session details
     * */
    public void logoutUser(){
        // Clearing all data from Shared Preferences
        editor.clear();
        editor.commit();

        // After logout redirect user to Login Activity
        Intent intentLogin = new Intent(mContext, LoginActivity.class);

        intentLogin.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intentLogin.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // Staring Login Activity
        mContext.startActivity(intentLogin);
        ((Activity) mContext).finish();

        Intent i = new Intent(mContext, FShipService.class);
        mContext.stopService(i);

        PendingIntent pi = PendingIntent.getService(mContext, 0, i, 0);
        AlarmManager alarmMgr = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        alarmMgr.cancel(pi);
    }

    /**
     * Quick check for login
     * **/
    // Get Login State
    public boolean isLoggedIn(){

        return pref != null && pref.getBoolean(IS_LOGIN, false);
    }
    /**
     * Quick check shipper or shop
     * **/
    public boolean isShipper(){

        return pref.getBoolean(KEY_IS_SHIPPER, false);
    }

    public int getShopId(){
        return pref.getInt(KEY_SHOP_ID, 0);
    }
    public int getShipperId(){
        return pref.getInt(KEY_SHIPPER_ID, 0);
    }

    public String getConnection(){
        return pref.getString(KEY_CONNECTION, "");
    }
    public void setConnection(String connection){
        editor.putString(KEY_CONNECTION, connection);
        editor.commit();
    }

    public int getNumberNotify(){
        return pref.getInt(KEY_NUMBER_NOTIFICATION, 0);
    }

    public void setNumberNotify(int numberNotify){
        editor.putInt(KEY_NUMBER_NOTIFICATION, numberNotify);
        editor.commit();
    }

    public void setAvatar(String avatar){
        editor.putString(KEY_AVATAR, avatar);
        editor.commit();
    }

    public String getAvatar(){
        return pref.getString(KEY_AVATAR, "");
    }

    public void setAvatarLabel(String avatarLabel){
        editor.putString(KEY_AVATAR_LABEL, avatarLabel);
        editor.commit();
    }

    public String getAvatarLabel(){
        return pref.getString(KEY_AVATAR_LABEL, "");
    }

    public void setName(String name){
        editor.putString(KEY_NAME, name);
        editor.commit();
    }

    public String getName(){
        return pref.getString(KEY_NAME, "");
    }

    public void setXPoint(String x){
        editor.putString(KEY_X_POINT, x);
        editor.commit();
    }

    public String getXPoint(){
        return pref.getString(KEY_X_POINT, "");
    }

    public void setYPoint(String name){
        editor.putString(KEY_Y_POINT, name);
        editor.commit();
    }

    public boolean isNotificationOn(){
        return pref.getBoolean(KEY_IS_NOTIFICATION_ON, true);
    }

    public void setIsOnNotification(boolean isOn){
        editor.putBoolean(KEY_IS_NOTIFICATION_ON, isOn);
        editor.commit();
    }

    public String getYPoint(){
        return pref.getString(KEY_Y_POINT, "");
    }

    public void setKeyUseGcm(boolean isUseGCM){
        editor.putBoolean(KEY_USE_GCM, isUseGCM);
        editor.commit();
    }

    public boolean getKeyUseGcm(){
        return pref.getBoolean(KEY_USE_GCM, true);
    }

    public void setReferralCode(String referralCode){
        editor.putString(KEY_REFERRAL_CODE, referralCode);
        editor.commit();
    }

    public String getReferralCode(){
        return pref.getString(KEY_REFERRAL_CODE, "");
    }

}
