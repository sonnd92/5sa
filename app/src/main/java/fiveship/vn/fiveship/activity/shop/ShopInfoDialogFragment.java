package fiveship.vn.fiveship.activity.shop;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.service.ShipperService;
import fiveship.vn.fiveship.service.ShopService;
import fiveship.vn.fiveship.utility.Utils;


public class ShopInfoDialogFragment extends DialogFragment{

    private int id = 0;
    private ShopService shopService;
    private View view;
    private String facebookPageID = "982314495196322";
    private TextView txtFacebook;

    public ShopInfoDialogFragment(){
    }

    @SuppressLint("ValidFragment")
    public ShopInfoDialogFragment(int id){
        this.id = id;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        shopService = ShopService.get_instance(getActivity());

        // Build the dialog and set up the button click handlers
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        view = getActivity().getLayoutInflater().inflate(R.layout.fragment_shop_info, null);
        builder.setView(view);

        txtFacebook = (TextView)view.findViewById(R.id.cus_facebook);

        builder.setPositiveButton("Đóng", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int id) {
            }
        });

        view.findViewById(R.id.cus_facebook).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFacebookPage();
            }
        });

        new GetShipperInfoDialog().execute();

        return builder.create();
    }

    private void buidUI(){
        if(facebookPageID != "" && facebookPageID.length() > 0){
            txtFacebook.setVisibility(View.VISIBLE);
            SpannableString spanString = new SpannableString("Xem facebook shop");
            spanString.setSpan(new UnderlineSpan(), 0, spanString.length(), 0);
            txtFacebook.setText(spanString);
        }
    }

    private void openFacebookPage(){

        String facebookUrl = "https://www.facebook.com/" + facebookPageID;

        String facebookUrlScheme = "fb://page/" + facebookPageID;

        try {
            int versionCode = getActivity().getPackageManager().getPackageInfo("com.facebook.katana", 0).versionCode;

            if (versionCode >= 3002850) {
                Uri uri = Uri.parse("fb://facewebmodal/f?href=" + facebookUrl);
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
            } else {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(facebookUrlScheme)));
            }
        } catch (Exception e) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(facebookUrl)));
        }

    }

    private class GetShipperInfoDialog extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {

            return shopService.getShopInfo(String.valueOf(id));
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            try {

                if (!result.getBoolean("error")) {

                    Utils.setImageToImageView(getActivity(), result.getJSONObject("data").getString("UrlAvatar"), (ImageView) view.findViewById(R.id.cus_image));

                    ((TextView)view.findViewById(R.id.cus_name)).setText(result.getJSONObject("data").getString("ShopName"));
                    ((TextView)view.findViewById(R.id.cus_address)).setText(result.getJSONObject("data").getString("ShopAddress"));

                    facebookPageID = result.getJSONObject("data").has("FacebookId") ? result.getJSONObject("data").getString("FacebookId") : "";

                    buidUI();

                }

                if (result.getBoolean("error")) {
                    Toast.makeText(getActivity(), result.getString("message"), Toast.LENGTH_LONG).show();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
