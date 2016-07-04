package fiveship.vn.fiveship.activity.shipper;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.victor.loading.rotate.RotateLoading;

import org.json.JSONObject;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.activity.RegisterActivity;
import fiveship.vn.fiveship.activity.shop.ShopShipCreateChosenShipDialogFragment;
import fiveship.vn.fiveship.adapter.PlacesAutoCompleteAdapter;
import fiveship.vn.fiveship.model.ShipTemplateItem;
import fiveship.vn.fiveship.service.OtherService;
import fiveship.vn.fiveship.utility.Utils;
import fiveship.vn.fiveship.utility.validator.Field;
import fiveship.vn.fiveship.utility.validator.Form;
import fiveship.vn.fiveship.utility.validator.validation.IsPhoneNumber;
import fiveship.vn.fiveship.utility.validator.validation.NotEmpty;
import fiveship.vn.fiveship.widget.DelayAutocompleteTextView;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AddShipInfoDialogFragment.OnAddInfoDialogListener} interface
 * to handle interaction events.
 * Use the {@link AddShipInfoDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddShipInfoDialogFragment extends DialogFragment implements
        GoogleApiClient.OnConnectionFailedListener {
    private OnAddInfoDialogListener mListener;
    private PlacesAutoCompleteAdapter mSendAdAdapter;

    private OtherService otherService;
    private View rootView;
    private EditText txtPhone;
    private EditText txtShipCost;
    private EditText txtPrePay;
    private EditText txtDetailAdd;
    private EditText txtNote;
    private DelayAutocompleteTextView txtToAddress;
    private TextView txtTitleDialog;
    private TextView chooseTemplateBtn;

    private GoogleApiClient mGoogleApiClient;
    private FragmentActivity myContext;
    private LatLng latlng;

    private int id;
    private double yPoint;
    private double xPoint;
    private String detailAdd;
    private String address;
    private int shipCost;
    private int prePay;
    private String note;
    private String phone;
    private boolean isEdit;
    private boolean isGroup;
    private String fromX;
    private String fromY;

    private Form form;

    public AddShipInfoDialogFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static AddShipInfoDialogFragment newInstance(int id, String phone, String detailAdd, String address, int shipCost, int prePay, double xPoint, double yPoint, String note, boolean isGroup, String fromX, String fromY) {
        AddShipInfoDialogFragment fragment = new AddShipInfoDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("id", id);
        bundle.putString("phone", phone);
        bundle.putString("detailAdd", detailAdd);
        bundle.putString("address", address);
        bundle.putInt("shipCost", shipCost);
        bundle.putInt("prePay", prePay);
        bundle.putDouble("xPoint", xPoint);
        bundle.putDouble("yPoint", yPoint);
        bundle.putString("note", note);
        bundle.putBoolean("isEdit", true);
        bundle.putBoolean("isGroup", isGroup);
        bundle.putString("fromX", fromX);
        bundle.putString("fromY", fromY);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static AddShipInfoDialogFragment newInstance(int id, boolean isGroup, String fromX, String fromY) {
        AddShipInfoDialogFragment fragment = new AddShipInfoDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("id", id);
        bundle.putBoolean("isEdit", false);
        bundle.putBoolean("isGroup", isGroup);
        bundle.putString("fromX", fromX);
        bundle.putString("fromY", fromY);
        fragment.setArguments(bundle);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        otherService = OtherService.get_instance(myContext);

        mGoogleApiClient = new GoogleApiClient.Builder(myContext)
                .addApi(Places.GEO_DATA_API)
                .build();

        form = new Form(myContext);

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setPositiveButton("Xác nhận", null)
                .setNegativeButton("Đóng", null); //Set to null. We override the onclick
        rootView = getActivity().getLayoutInflater().inflate(R.layout.fragment_add_ship_info_dialog, null);

        builder.setView(rootView);
        final AlertDialog dialog = builder.create();
        initView();
        initAutocompleteAddress();

        if (getArguments() != null) {
            isEdit = getArguments().getBoolean("isEdit");
            isGroup = getArguments().getBoolean("isGroup");
            if (isEdit) {
                getParams(getArguments());
                bindDataToView();
            } else {
                id = getArguments().getInt("id");
                fromX = getArguments().getString("fromX");
                fromY = getArguments().getString("fromY");
            }
            txtTitleDialog.setText("Thông tin đơn hàng " + String.format("%02d", id));
            if (!isGroup) {
                txtPrePay.addTextChangedListener(new Utils.MoneyTextWatcher(txtPrePay));
                txtShipCost.addTextChangedListener(new Utils.MoneyTextWatcher(txtShipCost));
            } else {
                rootView.findViewById(R.id.pre_pay_and_shipping_cost_layout).setVisibility(View.GONE);
            }
        }

        validation();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface d) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (form.isValid()) {
                            getValues();
                            mListener.onAddInfoDialogListener(id, phone, detailAdd, address, shipCost, prePay, xPoint, yPoint, note, isEdit);

                            dialog.dismiss();
                        }
                    }
                });
            }
        });

        return dialog;
    }

    public void getParams(Bundle args) {
        if (args != null) {
            id = args.getInt("id");
            phone = args.getString("phone");
            detailAdd = args.getString("detailAdd");
            address = args.getString("address");
            note = args.getString("note");
            shipCost = args.getInt("shipCost");
            prePay = args.getInt("prePay");
            xPoint = args.getDouble("xPoint");
            yPoint = args.getDouble("yPoint");
            fromX = args.getString("fromX");
            fromY = args.getString("fromY");
        }
    }

    public void getValues() {
        note = txtNote.getText().toString();
        detailAdd = txtDetailAdd.getText().toString();
        address = txtToAddress.getText().toString();

        if (!isGroup) {
            shipCost = Integer.parseInt(txtShipCost.getText().toString().replace(".", ""));
            prePay = txtPrePay.getText() != null && !txtPrePay.getText().toString().isEmpty()
                    ? Integer.parseInt(txtPrePay.getText().toString().replace(".", ""))
                    : 0;
        }
        phone = txtPhone.getText().toString();

    }

    public void initView() {

        txtNote = (EditText) rootView.findViewById(R.id.txt_shop_ship_create_note);
        txtDetailAdd = (EditText) rootView.findViewById(R.id.txt_shop_ship_create_to_home);
        txtShipCost = (EditText) rootView.findViewById(R.id.txt_shop_ship_create_cost_ship);
        txtPrePay = (EditText) rootView.findViewById(R.id.txt_shop_ship_create_cost);
        txtPhone = (EditText) rootView.findViewById(R.id.txt_customer_phone);
        txtTitleDialog = (TextView) rootView.findViewById(R.id.dialog_title);
        chooseTemplateBtn = (TextView) rootView.findViewById(R.id.btn_shop_create_chosen_ship);

        chooseTemplateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an instance of the dialog fragment and show it
                DialogFragment dialog = new ShopShipCreateChosenShipDialogFragment();

                dialog.show(myContext.getSupportFragmentManager(), "ShopShipCreateChosenShipDialogFragment");
            }
        });
    }

    public void bindDataToView() {
        txtNote.setText(note);
        txtDetailAdd.setText(detailAdd);
        txtToAddress.setText(address);
        txtPhone.setText(phone);
        txtPrePay.setText(String.valueOf(prePay));
        txtShipCost.setText(String.valueOf(shipCost));

    }

    public void validation() {
        form.addField(Field.using(txtPhone)
                .validate(NotEmpty.build(myContext, getString(R.string.phone_field)))
                .validate(IsPhoneNumber.build(myContext)));
        if (!isGroup) {
            form.addField(Field.using(txtShipCost)
                    .validate(NotEmpty.build(myContext, getString(R.string.shipping_cost_field))));
        }
        form.addField(Field.using(txtDetailAdd).validate(NotEmpty.build(myContext, getString(R.string.house_number_field))));
        form.addField(Field.using(txtToAddress).validate(NotEmpty.build(myContext, getString(R.string.street_field))));
    }

    private void initAutocompleteAddress() {

        // Register a listener that receives callbacks when a suggestion has been selected

        txtToAddress = (DelayAutocompleteTextView) rootView.findViewById(R.id.txt_shop_ship_create_to_address);

        txtToAddress.setOnItemClickListener(mSendAdAutocompleteClickListener);

        txtToAddress.setLoadingIndicator((RotateLoading) rootView.findViewById(R.id.li_delivery_add));

        mSendAdAdapter = new PlacesAutoCompleteAdapter(myContext, mGoogleApiClient, RegisterActivity.BOUNDS_GREATER_VIETNAM, null);

        txtToAddress.setAdapter(mSendAdAdapter);
    }

    private AdapterView.OnItemClickListener mSendAdAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            final AutocompletePrediction item = mSendAdAdapter.getItem(position);

            final String placeId = item.getPlaceId();

            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdateSendAddressCallback);
        }
    };

    private ResultCallback<PlaceBuffer> mUpdateSendAddressCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                // Request did not complete successfully
                places.release();
                return;
            }
            // Get the Place object from the buffer.
            final Place place = places.get(0);
            //Get coordinates of place
            latlng = place.getLatLng();

            xPoint = latlng.latitude;

            yPoint = latlng.longitude;

            places.release();
            if(fromX != null && !fromX.isEmpty() && fromY != null && !fromY.isEmpty())
                new GetDistanceAndCost().execute(fromX, fromY, String.valueOf(xPoint), String.valueOf(yPoint));
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        myContext = (FragmentActivity) context;
        if (context instanceof OnAddInfoDialogListener) {
            mListener = (OnAddInfoDialogListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onStart() {
        super.onStart();
        if (mGoogleApiClient != null)
            mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    public void onChosenShipTemplate(ShipTemplateItem item) {
        txtPrePay.setText(item.getPrepayShip());
        txtNote.setText(item.getNote());
        txtShipCost.setText(item.getCostShip());
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnAddInfoDialogListener {
        // TODO: Update argument type and phone
        void onAddInfoDialogListener(int id, String name, String detailAdd, String address, int shipCost, int prePay, double xPoint, double yPoint, String note, boolean isEdit);
    }
    private class GetDistanceAndCost extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... params) {
            return otherService.getDistanceAndCostShip(params[0], params[1], params[2], params[3]);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            try {
                if (result != null && !result.getBoolean("error")) {
                    txtShipCost.setText(result.getString("cost"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
