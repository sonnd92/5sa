package fiveship.vn.fiveship.activity.shipper;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.service.ShipperService;
import fiveship.vn.fiveship.utility.Utils;


public class ShipperInfoDialogFragment extends DialogFragment{

    private int id = 0;
    private ShipperService shipperService;
    private View view;

    public ShipperInfoDialogFragment(){
    }

    @SuppressLint("ValidFragment")
    public ShipperInfoDialogFragment(int id){
        this.id = id;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        shipperService = ShipperService.get_instance(getActivity());

        // Build the dialog and set up the button click handlers
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        view = getActivity().getLayoutInflater().inflate(R.layout.fragment_shipper_info, null);
        builder.setView(view);

        view.findViewById(R.id.cus_phone).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.callPhone(getActivity(), ((TextView)view.findViewById(R.id.cus_phone)).getText().toString());
            }
        });

        builder.setPositiveButton("Đóng", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int id) {
            }
        });

        new GetShipperInfoDialog().execute();

        return builder.create();
    }

    private class GetShipperInfoDialog extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {

            return shipperService.getShipperInfo(String.valueOf(id), "0");
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            try {

                if (!result.getBoolean("error")) {

                    Utils.setImageToImageView(getActivity(), result.getJSONObject("data").getString("UrlAvatar"), (ImageView)view.findViewById(R.id.cus_image));

                    ((TextView)view.findViewById(R.id.cus_name)).setText(result.getJSONObject("data").getString("Name"));
                    ((TextView)view.findViewById(R.id.cus_address)).setText(result.getJSONObject("data").getString("Address"));
                    ((TextView)view.findViewById(R.id.receive_ship)).setText(result.getJSONObject("data").getString("NumberShip"));
                    ((TextView)view.findViewById(R.id.complete_ship)).setText(result.getJSONObject("data").getString("NumberShipSuccess"));
                    ((TextView)view.findViewById(R.id.cus_phone)).setText(result.getJSONObject("data").getString("Phone"));
                    ((TextView)view.findViewById(R.id.cus_motor)).setText(result.getJSONObject("data").getString("MotorId"));

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
