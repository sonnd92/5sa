package fiveship.vn.fiveship.utility;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.squareup.picasso.Picasso;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.List;
import java.util.TimeZone;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.action.AssignShipAction;
import fiveship.vn.fiveship.activity.AlertAppDialogFragment;
import fiveship.vn.fiveship.activity.shipper.DetailOrderActivity;
import fiveship.vn.fiveship.model.NotificationItem;
import fiveship.vn.fiveship.model.ShippingOrderItem;

/**
 * Created by sonnd on 14/10/2015.
 */
public class Utils {

    public static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final int SELECT_FILE = 2;
    public static final int REQUEST_TOKEN_GET_GPS = 3;
    public static final int PLACE_PICKER_REQUEST = 4;
    public static final int REQUEST_CODE_ALERT_SYSTEM_PERMISSIONS = 5;
    public static final int REQUEST_CODE_ACCESS_FINE_LOCATION_PERMISSIONS = 6;
    public static final int CAMERA_PERMISSION_REQUEST_CODE = 7;
    public static final int EACCES_PERMISSION_REQUEST_CODE = 8;
    public static final int STATE_PHONE_PERMISSION_REQUEST_CODE = 8;

    public static boolean isConnectingToInternet(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null)
                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    return true;
                }

        }

        return false;
    }

    public static Dialog setupLoadingDialog(Context context) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.fiveship_loading_dialog);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        return dialog;
    }

    public static Dialog setupAlertDialog(Context context) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_alert);
        return dialog;
    }

    public static Dialog setupConfirmDialog(Context context) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_confirm);
        dialog.findViewById(R.id.btn_no).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        return dialog;
    }

    public static void showKeyboard(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);

        if (imm != null) {
            // only will trigger it if no physical keyboard is open
            imm.showSoftInput(view, 0);
        }
    }

    public static void hideKeyboard(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

    }

    public static void setBadgeCount(Context context, LayerDrawable icon, int count) {

        BadgeDrawable badge;

        // Reuse drawable if possible
        Drawable reuse = icon.findDrawableByLayerId(R.id.ic_badge);
        if (reuse != null && reuse instanceof BadgeDrawable) {
            badge = (BadgeDrawable) reuse;
        } else {
            badge = new BadgeDrawable(context);
        }

        badge.setCount(count);
        icon.mutate();
        icon.setDrawableByLayerId(R.id.ic_badge, badge);
    }

    public static class MoneyTextWatcher implements TextWatcher {
        private String current = "";
        private EditText editText;

        public MoneyTextWatcher(EditText editText) {
            this.editText = editText;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable editable) {

            if (!editable.toString().equals(current)) {
                editText.removeTextChangedListener(this);

                String cleanString = editable.toString().replaceAll("[$,.,₫,k,K,\\s]", "");

                if (cleanString.isEmpty()) {
                    cleanString = "0";
                }

                DecimalFormat numFormat = new DecimalFormat("#,###,###");

                current = numFormat.format(Double.parseDouble(cleanString));
                editText.setText(current);
                editText.setSelection(current.length());

                editText.addTextChangedListener(this);
            }
        }
    }

    public static void callPhone(Context context, String numberPhone) {

        try {
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + numberPhone));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    // draw text into image


    public static Bitmap writeTextOnDrawable(Context myContext, int drawableId, String text, int colorId) {

        Bitmap bm = BitmapFactory.decodeResource(myContext.getResources(), drawableId)
                .copy(Bitmap.Config.ARGB_8888, true);

        Typeface tf = Typeface.create("Helvetica", Typeface.BOLD);

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(ContextCompat.getColor(myContext, colorId));
        paint.setTypeface(tf);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(convertToPixels(myContext, 10));

        Rect textRect = new Rect();
        paint.getTextBounds(text, 0, text.length(), textRect);

        Canvas canvas = new Canvas(bm);

        //If the text is bigger than the canvas , reduce the font size
        if (textRect.width() >= (canvas.getWidth() - 4))     //the padding on either sides is considered as 4, so as to appropriately fit in the text
            paint.setTextSize(convertToPixels(myContext, 7));        //Scaling needs to be used for different dpi's

        //Calculate the positions
        int xPos = (canvas.getWidth() / 2);     //+ 2 is for regulating the x position offset

        //"- ((paint.descent() + paint.ascent()) / 2)" is the distance from the baseline to the center.
        DisplayMetrics metrics = myContext.getResources().getDisplayMetrics();
        ((Activity) myContext).getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int paddingY = 0;
        switch (metrics.densityDpi) {
            case DisplayMetrics.DENSITY_XXXHIGH:
                paddingY = 16;
                break;
            case DisplayMetrics.DENSITY_XXHIGH:
                paddingY = 12;
                break;
            case DisplayMetrics.DENSITY_HIGH:
                paddingY = 8;
                break;
            default:
                paddingY = 4;
                break;
        }

        int yPos = (int) ((canvas.getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2)) - paddingY;

        canvas.drawText(text, xPos, yPos, paint);

        return bm;
    }

    public static int convertToPixels(Context context, int nDP) {
        final float conversionScale = context.getResources().getDisplayMetrics().density;

        return (int) ((nDP * conversionScale) + 0.5f);

    }

    public static int getRelativeTop(View myView) {
        if (myView.getParent() == myView.getRootView())
            return myView.getTop();
        else
            return myView.getTop() + getRelativeTop((View) myView.getParent());
    }

    public static void setImageToImageView(Context context, String url, ImageView imgV) {

        try {
            if (url != "") {
                Picasso.with(context).load(Uri.parse(url)).placeholder(R.drawable.default_avatar).into(imgV);
            } else {
                Picasso.with(context).load(R.drawable.default_avatar).into(imgV);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public static void setImageToImageViewNoDefault(Context context, String url, ImageView imgV) {

        try {
            if (url != "") {
                Picasso.with(context).load(Uri.parse(url)).placeholder(R.drawable.ic_dot_loading).into(imgV);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public static String getUuid(Context context) {
        try {
            String uuid = Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
            return uuid;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }

    public static String getImei(Context context) {
        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                final TelephonyManager mTelephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                return mTelephony.getDeviceId();
            } else {
                ActivityCompat.requestPermissions((Activity) context, new String[]{android.Manifest.permission.READ_PHONE_STATE}, STATE_PHONE_PERMISSION_REQUEST_CODE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }

    public static String getModel() {
        try {
            return android.os.Build.MODEL;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getName() {
        try {
            return android.os.Build.PRODUCT;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getProductName() {
        try {
            return BluetoothAdapter.getDefaultAdapter().getName();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getName();
    }

    public String getDevice() {
        try {
            return android.os.Build.DEVICE;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getManufacturer() {

        String brand = "";

        try {
            brand = android.os.Build.MANUFACTURER.toLowerCase().trim();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (brand == "lge") {
            brand = "lg";
        }

        return brand;
    }

    public static String getOSVersion() {
        try {
            return android.os.Build.VERSION.RELEASE;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getSDKVersion() {
        try {
            return android.os.Build.VERSION.SDK;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getVersionApp(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static int getVersionCode(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static String getTimeZoneID() {
        try {
            return TimeZone.getDefault().getID();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getPhoneNumber(Context context) {
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            return tm.getLine1Number();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getLanguage() {
        try {
            return java.util.Locale.getDefault().getLanguage();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "vi";
    }

    public static String getMACAddress(Context context) {
        try {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wInfo = wifiManager.getConnectionInfo();
            return wInfo.getMacAddress();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static boolean isTablet(Context context) {

        try {
            return (context.getResources().getConfiguration().screenLayout
                    & Configuration.SCREENLAYOUT_SIZE_MASK)
                    >= Configuration.SCREENLAYOUT_SIZE_LARGE;

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return false;
    }

    public static void showKeyboard(Context context) {

        try {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void hideKeyboard(Context context) {

        try {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static String removeUnicode(String str, boolean isRemoveSpace) {

        str = str.toLowerCase().trim();
        str = str.replaceAll("[à|á|ạ|ả|ã|â|ầ|ấ|ậ|ẩ|ẫ|ă|ằ|ắ|ặ|ẳ|ẵ]", "a");
        str = str.replaceAll("[è|é|ẹ|ẻ|ẽ|ê|ề|ế|ệ|ể|ễ]", "e");
        str = str.replaceAll("[ì|í|ị|ỉ|ĩ]", "i");
        str = str.replaceAll("[ò|ó|ọ|ỏ|õ|ô|ồ|ố|ộ|ổ|ỗ|ơ|ờ|ớ|ợ|ở|ỡ]", "o");
        str = str.replaceAll("[ù|ú|ụ|ủ|ũ|ư|ừ|ứ|ự|ử|ữ]", "u");
        str = str.replaceAll("[ỳ|ý|ỵ|ỷ|ỹ]", "y");
        str = str.replaceAll("[đ]", "d");
        str = str.replaceAll("[!|@|%|^|*|(|)|+|=|<|>|?|/|,|.|:|;|'|\"|&|#|[|]|~|$|_|–|”|“|`]", "-");
        str = isRemoveSpace ? str.replaceAll(" ", "") : str.replaceAll(" ", "-");
        str = str.replaceAll("[-+-]", "-");
        //str = str.replaceAll("[^-+|-+$]", "");
        return str;
    }

    public static void expand(final View v) {
        v.measure(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        final int targetHeight = v.getMeasuredHeight();

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? LinearLayout.LayoutParams.WRAP_CONTENT
                        : (int) (targetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int) (targetHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    public static void collapse(final View v) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    v.setVisibility(View.GONE);
                } else {
                    v.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int) (initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public ProgressViewHolder(View v) {
            super(v);
            progressBar = (ProgressBar) v.findViewById(R.id.progress_bar);
        }
    }

    public static Dialog CallToShopDialog(final Context context, final NotificationItem notify, boolean isSystemDialog) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.NonActivityDialog));
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.alert_new_ship_assigned_dialog, null);

        // Set the dialog title
        ((TextView) dialogView.findViewById(R.id.dialog_title)).setText(notify.getTitle());
        // Set the dialog content
        ((TextView) dialogView.findViewById(R.id.dialog_content)).setText(notify.getName());

        builder.setView(dialogView);

        final AlertDialog dialog = builder.create();
        // Set the dialog negative button
        dialogView.findViewById(R.id.dialog_btn_detail).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent dIntent = new Intent(context, DetailOrderActivity.class);
                dIntent.putExtra("OrderId", notify.getId());
                dIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                //PendingIntent pendingIntent = PendingIntent.getActivity(context, notify.getNotifyId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
                context.startActivity(dIntent);
                dialog.dismiss();
            }
        });
        // Set the dialog positive button
        try {
            final JSONObject notifyDetail = new JSONObject(notify.getData());

            // Set the dialog open time
            ((TextView) dialogView.findViewById(R.id.dialog_time)).setText(notifyDetail.getString("Date"));

            if (notifyDetail.length() > 0 && notifyDetail.has("Phone") && !notifyDetail.getString("Phone").isEmpty()) {
                dialogView.findViewById(R.id.dialog_btn_call).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            Utils.callPhone(context, notifyDetail.getString("Phone"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        dialog.dismiss();
                    }
                });
            } else {
                dialogView.findViewById(R.id.dialog_btn_call).setVisibility(View.GONE);
            }
        } catch (JSONException e) {
            dialogView.findViewById(R.id.dialog_btn_call).setVisibility(View.GONE);
        }
        if (isSystemDialog) {
            dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        } else {
            dialogView.findViewById(R.id.dialog_btn_detail).setVisibility(View.GONE);
            dialogView.findViewById(R.id.iv_logo).setVisibility(View.GONE);
        }
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        return dialog;
    }

    public static AlertDialog setupRecommendShippingCostDialog(final Context context, final ShippingOrderItem orderItem) {
        final int shipId = new SessionManager(context).getShipperId();
        AlertDialog.Builder rBuilder = new AlertDialog.Builder(context);

        LayoutInflater _inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rView = _inflater.inflate(R.layout.recomment_ship_cost_dialog_layout, null);
        final EditText etShippingRecommend = (EditText) rView.findViewById(R.id.tv_recommended_shipping_cost);

        ((TextView) rView.findViewById(R.id.order_name)).setText(orderItem.getName());

        if (orderItem.getDateEnd() != null && !orderItem.getDateEnd().isEmpty()) {
            ((TextView) rView.findViewById(R.id.order_end_time)).setText(orderItem.getDateEnd());
        } else {
            rView.findViewById(R.id.order_end_time_box).setVisibility(View.GONE);
        }

        if (orderItem.getDetails() != null && !orderItem.getDetails().isEmpty()) {
            ((TextView) rView.findViewById(R.id.order_note)).setText(orderItem.getDetails());
        } else {
            rView.findViewById(R.id.order_note_box).setVisibility(View.GONE);
        }

        if (!orderItem.isShopReliable()) {
            rView.findViewById(R.id.warning_for_reliable).setVisibility(View.VISIBLE);
            rView.findViewById(R.id.divider_top).setVisibility(View.VISIBLE);
        }

        rBuilder.setView(rView);

        final AlertDialog dialog = rBuilder.create();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        rView.findViewById(R.id.submit_recommended_shipping_cost).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!"".equals(etShippingRecommend.getText().toString()) && Integer.parseInt(etShippingRecommend.getText().toString()) != 0) {
                    new AssignShipAction(
                            context,
                            orderItem,
                            shipId,
                            orderItem.getStatusNote(),
                            orderItem.isShopReliable(),
                            Integer.parseInt(etShippingRecommend.getText().toString())).assign();
                    dialog.dismiss();
                } else {
                    Toast.makeText(context, "Bạn chưa nhập phí ship đề xuất", Toast.LENGTH_SHORT).show();
                }
            }
        });


        return dialog;
    }


    /*
     * get client id of mqtt service
     */
    public static String getMQTTClientId(Context context, SessionManager sessionManager, String suffix) {

        try {
            return "5ship_" +
                    (sessionManager.isShipper() ? "shipper_" : "shop_") +
                    (sessionManager.isShipper() ? sessionManager.getShipperId() : sessionManager.getShopId()) +
                    "_" + Utils.getUuid(context) + "_android" + suffix;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return MqttClient.generateClientId();
    }

    public static AlertDialog shareJourneyDialog(final Context context, final String link) {

        final String title = "Chia sẻ hành trình";

        AlertDialog.Builder rBuilder = new AlertDialog.Builder(context);
        LayoutInflater _inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final View rView = _inflater.inflate(R.layout.share_journey_popup_layout, null);
        rBuilder.setView(rView);
        final AlertDialog dialog = rBuilder.create();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        FacebookSdk.sdkInitialize(context.getApplicationContext());

        rView.findViewById(R.id.btn_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        rView.findViewById(R.id.btn_share_via_facebook).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ShareDialog.canShow(ShareLinkContent.class)) {

                    ShareLinkContent linkContent = new ShareLinkContent.Builder()
                            .setContentTitle(title)
                            .setImageUrl(Uri.parse(context.getString(R.string.icon_launcher_url)))
                            .setContentDescription(link)
                            .setContentUrl(Uri.parse(link))
                            .build();

                    ShareDialog.show((Activity) context, linkContent);
                }
            }
        });

        rView.findViewById(R.id.btn_share_via_sms).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                sendIntent.putExtra("sms_body", link);
                sendIntent.setType("vnd.android-dir/mms-sms");
                context.startActivity(sendIntent);
            }
        });

        rView.findViewById(R.id.btn_share_via_zalo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PackageManager pm = context.getPackageManager();
                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                sendIntent.setType("text/plain");
                List<ResolveInfo> resInfo = pm.queryIntentActivities(sendIntent, 0);
                for (int i = 0; i < resInfo.size(); i++) {
                    // Extract the label, append it, and repackage it in a LabeledIntent
                    ResolveInfo ri = resInfo.get(i);
                    String packageName = ri.activityInfo.packageName;
                    if (packageName.contains("zalo")) {
                        Intent intent = new Intent();
                        intent.setComponent(new ComponentName(packageName, ri.activityInfo.name));
                        intent.setAction(Intent.ACTION_SEND);
                        intent.setType("text/plain");
                        intent.putExtra(Intent.EXTRA_SUBJECT, title);
                        intent.putExtra(Intent.EXTRA_TEXT, link);
                        context.startActivity(intent);
                    }
                }
            }
        });

        rView.findViewById(R.id.btn_copy_link).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = android.content.ClipData.newPlainText(title, link);
                clipboard.setPrimaryClip(clip);

                Toast.makeText(context, "Đường link đã được lưu vào bộ nhớ tạm", Toast.LENGTH_SHORT).show();
            }
        });


        return dialog;
    }

    public static DialogFragment getDialogNoInternetConnection(final Context ctx) {
        return AlertAppDialogFragment
                .getInstance()
                .setLayout(R.layout.not_internet_connection_dialog_layout);
    }
}
