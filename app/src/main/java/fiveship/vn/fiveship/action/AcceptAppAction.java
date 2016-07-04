package fiveship.vn.fiveship.action;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;

import org.json.JSONObject;

import java.util.ArrayList;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.activity.AlertAppDialogFragment;
import fiveship.vn.fiveship.activity.EditCustomerInfoActivity;
import fiveship.vn.fiveship.activity.WarningActivity;
import fiveship.vn.fiveship.service.CustomerService;
import fiveship.vn.fiveship.utility.GPSTracker;
import fiveship.vn.fiveship.utility.SessionManager;
import fiveship.vn.fiveship.utility.Utils;

/**
 * Created by BVN on 08/11/2015.
 */
public class AcceptAppAction {

    private Context mContext;
    private CustomerService customerService;
    private AcceptAppActionCallback mListener;
    private GPSTracker gpsTracker;
    private SessionManager sessionManager;
    private ArrayList<DialogFragment> mListDialog;

    public AcceptAppAction(Context context, SessionManager sessionManager) {
        this.mContext = context;
        this.customerService = CustomerService.get_instance(context);
        this.sessionManager = sessionManager;
        this.gpsTracker = new GPSTracker(context);
        mListDialog = new ArrayList<DialogFragment>();
    }

    public interface AcceptAppActionCallback {
        void callBackSuccess();
    }

    public void setAcceptAppActionCallback(AcceptAppActionCallback listener) {
        mListener = listener;
    }

    public void post() {
        new AcceptAppTask().execute();
    }

    private class AcceptAppTask extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {
            return customerService.accessApp(sessionManager.isShipper() ? sessionManager.getShipperId() : sessionManager.getShopId(),
                    !sessionManager.isShipper(),
                    sessionManager.getConnection(),
                    String.valueOf(gpsTracker.getLatitude()),
                    String.valueOf(gpsTracker.getLongitude()),
                    Utils.getImei(mContext),
                    Utils.getOSVersion(),
                    Utils.getModel(),
                    Utils.getVersionApp(mContext),
                    Utils.getManufacturer(),
                    Utils.getUuid(mContext),
                    Utils.getVersionCode(mContext)
            );
        }

        @Override
        protected void onPostExecute(JSONObject result) {

            try {
                if (result != null && !result.getBoolean("error")) {
                    if (result.getJSONObject("data").getBoolean("IsWarning")) {
                        Intent warningIntent = new Intent(mContext, WarningActivity.class);
                        warningIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        warningIntent.putExtra("img", result.getJSONObject("data").getString("ImgWarning"));
                        warningIntent.putExtra("message", result.getJSONObject("data").getString("MessageWarning"));
                        mContext.startActivity(warningIntent);
                    }

                    if (result.getJSONObject("data").getBoolean("IsUpdateInfo")) {
                        Intent updateInfo = new Intent(mContext, EditCustomerInfoActivity.class);
                        updateInfo.putExtra("isFocusUpdate", true);
                        mContext.startActivity(updateInfo);
                    }

                    if (result.getJSONObject("data").has("IsUpdate") && result.getJSONObject("data").getBoolean("IsUpdate")) {
                        DialogFragment dialog = AlertAppDialogFragment
                                .getInstance()
                                .setLayout(R.layout.update_info_popup_layout)
                                .setContent("Ứng dụng 5ship vừa cập nhật phiên bản mới\n" +
                                        "Vui lòng tải ngay để trải nghiệm!")
                                .setOnDialogButtonClick(new AlertAppDialogFragment.OnDialogButtonClick() {
                                    @Override
                                    public void onPositiveButtonClicked() {

                                        final String appPackageName = mContext.getPackageName(); // getPackageName() from Context or Activity object
                                        try {
                                            mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                                        } catch (android.content.ActivityNotFoundException anfe) {
                                            mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                                        }

                                    }

                                    @Override
                                    public void onNegativeButtonClicked() {

                                    }
                                });
                        mListDialog.add(dialog);
                    }

                    if (result.getJSONObject("data").has("IsAlert") && result.getJSONObject("data").getBoolean("IsAlert")) {
                        DialogFragment dialog = AlertAppDialogFragment
                                .getInstance()
                                .setContent(result.getJSONObject("data").getString("AlertContent"));
                        mListDialog.add(dialog);
                    }

                    if (result.getJSONObject("data").has("IsAlertConfirm") && result.getJSONObject("data").getBoolean("IsAlertConfirm")) {
                        DialogFragment dialog = AlertAppDialogFragment
                                .getInstance()
                                .setLayout(R.layout.notice_authentication_shipper_popup_layout);
                        mListDialog.add(dialog);
                    }


                    sessionManager.setNumberNotify(result.getJSONObject("data").getInt("NumberNotify"));
                    sessionManager.setKeyUseGcm(result.getJSONObject("data").has("IsUseGCM") ? result.getJSONObject("data").getBoolean("IsUseGCM") : true);
                    sessionManager.setName(result.getJSONObject("data").getJSONObject("Info").getString("Name"));
                    sessionManager.setAvatar(result.getJSONObject("data").getJSONObject("Info").getString("Avatar"));
                    sessionManager.setXPoint(result.getJSONObject("data").getJSONObject("Info").getString("XPoint"));
                    sessionManager.setYPoint(result.getJSONObject("data").getJSONObject("Info").getString("YPoint"));
                    sessionManager.setReferralCode(result.getJSONObject("data").getJSONObject("Info").getString("ReferralCode"));

                    mListener.callBackSuccess();

                    for (int i = 0; i < mListDialog.size(); i++) {

                        final int idx = i;

                        if (idx < mListDialog.size() - 1) {

                            AlertAppDialogFragment dialog = (AlertAppDialogFragment) mListDialog.get(idx);

                            dialog.setOnDismiss(new AlertAppDialogFragment.OnDialogClose() {

                                @Override
                                public void onDialogClose() {

                                    mListDialog
                                            .get(idx + 1)
                                            .show(((AppCompatActivity) mContext).getSupportFragmentManager(), "AlertAppDialogFragment" + idx);
                                }

                            });
                        }
                    }

                    mListDialog
                            .get(0)
                            .show(((AppCompatActivity) mContext).getSupportFragmentManager(), "AlertAppDialogFragment");
                }
            } catch (Exception e) {

                e.printStackTrace();

            }
        }
    }

}
