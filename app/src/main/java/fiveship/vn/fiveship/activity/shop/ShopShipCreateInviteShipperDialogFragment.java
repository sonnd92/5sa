package fiveship.vn.fiveship.activity.shop;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.adapter.SuggestShipperAmityAdapter;
import fiveship.vn.fiveship.model.CustomerItem;


public class ShopShipCreateInviteShipperDialogFragment extends DialogFragment{

    public interface ShopShipCreateInviteShipperDialog {
        void onSuccessInvite(boolean isNear, boolean isSafe, String arrShipper);
    }

    ShopShipCreateInviteShipperDialog mListener;

    private ArrayList<String> listId = new ArrayList<>();

    private boolean isNear;

    private boolean isSafe;

    private LinearLayout boxListShipper;

    private SuggestShipperAmityAdapter suggestShipperAmityAdapter;

    public ShopShipCreateInviteShipperDialogFragment(){}

    public static ShopShipCreateInviteShipperDialogFragment getInstance(int shopId){
        ShopShipCreateInviteShipperDialogFragment instance = new ShopShipCreateInviteShipperDialogFragment();
        Bundle args = new Bundle();
        args.putInt("ShopId", shopId);
        instance.setArguments(args);
        return instance;
    }

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (ShopShipCreateInviteShipperDialog) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Build the dialog and set up the button click handlers

        int shopId = getArguments().getInt("ShopId");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final View viewChoiceShipper = getActivity().getLayoutInflater().inflate(R.layout.dialog_choice_shipper_assign_ship, null);
        builder.setView(viewChoiceShipper);

        boxListShipper = (LinearLayout)viewChoiceShipper.findViewById(R.id.list_shipper_amity);

        final AutoCompleteTextView txtShipper = (AutoCompleteTextView) viewChoiceShipper.findViewById(R.id.txt_shop_ship_create_detail_list_shipper);

        txtShipper.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                try{

                    final View viewItem = getActivity().getLayoutInflater().inflate(R.layout.shipper_choice_amity_item, null);

                    final CustomerItem item = suggestShipperAmityAdapter.getCustomerItem(position);

                    listId.add(String.valueOf(item.getId()));

                    ((TextView)viewItem.findViewById(R.id.name_amity)).setText(item.getFullName());

                    viewItem.findViewById(R.id.btn_remove_amity).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            listId.remove(String.valueOf(item.getId()));

                            boxListShipper.removeView(viewItem);
                        }
                    });

                    boxListShipper.addView(viewItem);
                    txtShipper.setText("");

                }catch (Exception ex){
                    ex.printStackTrace();
                }

            }
        });

        suggestShipperAmityAdapter = new SuggestShipperAmityAdapter(getActivity(), shopId);

        txtShipper.setAdapter(suggestShipperAmityAdapter);

        viewChoiceShipper.findViewById(R.id.cb_shop_ship_create_detail_near).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isNear = !isNear;

                ((TextView) viewChoiceShipper.findViewById(R.id.cb_shop_ship_create_detail_near)).setCompoundDrawablesWithIntrinsicBounds(0, 0, isNear ? R.drawable.ic_checked : R.drawable.ic_nocheck, 0);
            }
        });

        viewChoiceShipper.findViewById(R.id.cb_shop_ship_create_detail_safe).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSafe = !isSafe;

                ((TextView)viewChoiceShipper.findViewById(R.id.cb_shop_ship_create_detail_safe)).setCompoundDrawablesWithIntrinsicBounds(0, 0, isSafe ? R.drawable.ic_checked : R.drawable.ic_nocheck, 0);
            }
        });

        // Set the dialog title
        builder.setCancelable(false)
                .setNegativeButton("Tạo đơn hàng", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        String strId = "";

                        for (int i = 0; i < listId.size(); i++){
                            strId += (i > 0 ? "," : "") + listId.get(i);
                        }

                        mListener.onSuccessInvite(isNear, isSafe, strId);
                    }
                });

        return builder.create();
    }

}
