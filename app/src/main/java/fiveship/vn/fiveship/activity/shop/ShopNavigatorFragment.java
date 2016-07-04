package fiveship.vn.fiveship.activity.shop;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.activity.EditCustomerInfoActivity;
import fiveship.vn.fiveship.activity.NewsFragment;
import fiveship.vn.fiveship.activity.ReferralCodeFragment;
import fiveship.vn.fiveship.interfaces.OnNavigatorSelected;
import fiveship.vn.fiveship.utility.Config;
import fiveship.vn.fiveship.utility.SessionManager;
import fiveship.vn.fiveship.utility.Utils;
import fiveship.vn.fiveship.utility.enums.ShopTabManagerPositionEnum;

public class ShopNavigatorFragment extends Fragment implements View.OnClickListener, ShopActivity.OnTabChangedFromActivity {

    private View prevMenuSelected;
    private SessionManager sessionManager;
    private ImageView avatar;
    private TextView name;
    private View rootView;
    private FragmentActivity mContext;
    private OnNavigatorSelected mNavigatorSelectListener;

    private ShopNavigator mListener;

    private Fragment mLastFragment;
    Fragment fragment;


    public interface ShopNavigator {
        void onCapture();
    }

    public ShopNavigatorFragment(){

    }

    public void setShopNavigator(ShopNavigator listener) {
        mListener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.shop_activity_navigator, container, false);
        View home = rootView.findViewById(R.id.shop_ship_menu_home);

        sessionManager = new SessionManager(getActivity());

        // set menu item on click
        prevMenuSelected = home;
        home.setSelected(true);
        home.setOnClickListener(this);
        rootView.findViewById(R.id.shop_ship_menu_home).setOnClickListener(this);
        rootView.findViewById(R.id.shop_ship_menu_create).setOnClickListener(this);
        rootView.findViewById(R.id.shop_ship_menu_create_template).setOnClickListener(this);
        rootView.findViewById(R.id.shop_ship_menu_capture).setOnClickListener(this);
        rootView.findViewById(R.id.shop_ship_menu_all).setOnClickListener(this);
        rootView.findViewById(R.id.shop_ship_menu_wait).setOnClickListener(this);
        rootView.findViewById(R.id.shop_ship_menu_receive).setOnClickListener(this);
        rootView.findViewById(R.id.shop_ship_menu_shipping).setOnClickListener(this);
        rootView.findViewById(R.id.shop_ship_menu_end).setOnClickListener(this);
        rootView.findViewById(R.id.shop_ship_menu_complete).setOnClickListener(this);
        rootView.findViewById(R.id.shop_ship_menu_cancel).setOnClickListener(this);
        //rootView.findViewById(R.id.shop_ship_menu_info).setOnClickListener(this);
        rootView.findViewById(R.id.logout).setOnClickListener(this);
        rootView.findViewById(R.id.menu_support).setOnClickListener(this);
        rootView.findViewById(R.id.menu_referral_code).setOnClickListener(this);
        rootView.findViewById(R.id.menu_news).setOnClickListener(this);
        rootView.findViewById(R.id.shop_ship_menu_shipper_manager).setOnClickListener(this);

        rootView.findViewById(R.id.shop_ship_menu_list).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rootView.findViewById(R.id.box_menu_ship).getVisibility() == View.GONE) {
                    rootView.findViewById(R.id.box_menu_ship).setVisibility(View.VISIBLE);
                } else {
                    rootView.findViewById(R.id.box_menu_ship).setVisibility(View.GONE);
                }
            }
        });

        BroadcastReceiver bReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Config.RECEIVE_UPDATE_NAVIGATOR)) {
                    Utils.setImageToImageView(getActivity(), sessionManager.getCustomerInfo().getAvatar(), avatar);
                    name.setText(sessionManager.getCustomerInfo().getFullName());
                }

//                if (intent.getAction().equals(Config.RECEIVE_SHOP_MOVE_TAB_WAIT)) {
//                    new Handler().postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            onClick(rootView.findViewById(R.id.shop_ship_menu_wait));
//                        }
//                    }, 300);
//                }
            }
        };

        LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(getContext());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Config.RECEIVE_UPDATE_NAVIGATOR);
//        intentFilter.addAction(Config.RECEIVE_SHOP_MOVE_TAB_WAIT);
        bManager.registerReceiver(bReceiver, intentFilter);

        initView();

        return rootView;
    }

    public void initView() {
        avatar = (ImageView) rootView.findViewById(R.id.cus_avatar);

        avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent editInfoIntent = new Intent(getActivity(), EditCustomerInfoActivity.class);
                startActivity(editInfoIntent);
            }
        });

        name = (TextView) rootView.findViewById(R.id.cus_name);

        Utils.setImageToImageView(getActivity(), sessionManager.getCustomerInfo().getAvatar(), avatar);

        Utils.setImageToImageViewNoDefault(getActivity(), sessionManager.getCustomerInfo().getAvatarLabel(), (ImageView) rootView.findViewById(R.id.cus_avatar_label));

        name.setText(sessionManager.getCustomerInfo().getFullName());

        ImageView setting = (ImageView) rootView.findViewById(R.id.cus_setting);

        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent editInfoIntent = new Intent(getActivity(), EditCustomerInfoActivity.class);
                startActivity(editInfoIntent);
            }
        });
    }

//    private void reBuildDrawerLayout() {
//
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    mDrawerLayout.addDrawerListener(mDrawerToggle);
//                    mDrawerLayout.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            mDrawerToggle.syncState();
//                        }
//                    });
//                } catch (Exception ex) {
//                    ex.printStackTrace();
//                }
//            }
//        }, 5 * 1000);
//
//    }

    @Override
    public void onClick(View v) {
        if (!v.isSelected()) {

            //ShopActivity.mDrawerLayout.closeDrawer(Gravity.LEFT);
            if (v.getId() != R.id.shop_ship_menu_create
                    && v.getId() != R.id.shop_ship_menu_capture
                    && v.getId() != R.id.menu_support
                    && v.getId() != R.id.shop_ship_menu_list) {
                prevMenuSelected.setSelected(false);
                v.setSelected(true);
                prevMenuSelected = v;
            }

            switch (v.getId()) {
                case R.id.shop_ship_menu_home:
                    fragment = ShopShipFragment.newInstance();
                    break;
                case R.id.shop_ship_menu_create:
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent create = new Intent(getActivity(), ShopCreateGroupOrderActivity.class);
                            create.putExtra("isGroup", true);
                            startActivity(create);
                        }
                    }, 500);

                    /*final Dialog dialog = new Dialog(getActivity());
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.choose_create_order_template_dialog);
                    dialog.findViewById(R.id.create_single_order_btn).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent create = new Intent(getActivity(), ShopShipCreateActivity.class);
                            startActivity(create);
                            dialog.dismiss();
                        }
                    });

                    dialog.findViewById(R.id.create_group_order_btn).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent create = new Intent(getActivity(), ShopCreateGroupOrderActivity.class);
                            create.putExtra("isGroup", true);
                            startActivity(create);
                            dialog.dismiss();
                        }
                    });

                    dialog.findViewById(R.id.create_multi_order_btn).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent create = new Intent(getActivity(), ShopCreateGroupOrderActivity.class);
                            create.putExtra("isGroup", false);
                            startActivity(create);
                            dialog.dismiss();
                        }
                    });
                    dialog.show();*/
                    break;
                case R.id.shop_ship_menu_create_template:
                    fragment = ShopShipTemplateFragment.newInstance();
                    break;
                case R.id.shop_ship_menu_capture:
                    mListener.onCapture();
                    break;
                case R.id.shop_ship_menu_all:
                    if (mLastFragment instanceof ShopShipAllFragment) {
                        ShopShipAllFragment.ChangeCurrentPager(ShopTabManagerPositionEnum.ALL.getPosition());
                    } else {
                        fragment = ShopShipAllFragment.newInstance(ShopTabManagerPositionEnum.ALL.getPosition());
                    }
                    break;
                case R.id.shop_ship_menu_wait:
                    if (mLastFragment instanceof ShopShipAllFragment) {
                        ShopShipAllFragment.ChangeCurrentPager(ShopTabManagerPositionEnum.WAIT.getPosition());
                    } else {
                        fragment = ShopShipAllFragment.newInstance(ShopTabManagerPositionEnum.WAIT.getPosition());
                    }
                    break;
                case R.id.shop_ship_menu_receive:
                    if(mLastFragment instanceof ShopShipAllFragment){
                        ShopShipAllFragment.ChangeCurrentPager(ShopTabManagerPositionEnum.RECEIVED.getPosition());
                    }else {
                        fragment = ShopShipAllFragment.newInstance(ShopTabManagerPositionEnum.RECEIVED.getPosition());
                    }
                    break;
                case R.id.shop_ship_menu_shipping:
                    if(mLastFragment instanceof ShopShipAllFragment){
                        ShopShipAllFragment.ChangeCurrentPager(ShopTabManagerPositionEnum.SHIPPING.getPosition());
                    }else {
                        fragment = ShopShipAllFragment.newInstance(ShopTabManagerPositionEnum.SHIPPING.getPosition());
                    }
                    break;
                case R.id.shop_ship_menu_end:
                    if(mLastFragment instanceof ShopShipAllFragment){
                        ShopShipAllFragment.ChangeCurrentPager(ShopTabManagerPositionEnum.END.getPosition());
                    }else {
                        fragment = ShopShipAllFragment.newInstance(ShopTabManagerPositionEnum.END.getPosition());
                    }
                    break;
                case R.id.shop_ship_menu_complete:
                    if(mLastFragment instanceof ShopShipAllFragment){
                        ShopShipAllFragment.ChangeCurrentPager(ShopTabManagerPositionEnum.COMPLETE.getPosition());
                    }else {
                        fragment = ShopShipAllFragment.newInstance(ShopTabManagerPositionEnum.COMPLETE.getPosition());
                    }
                    break;
                case R.id.shop_ship_menu_cancel:
                    if(mLastFragment instanceof ShopShipAllFragment){
                        ShopShipAllFragment.ChangeCurrentPager(ShopTabManagerPositionEnum.CANCEL.getPosition());
                    }else {
                        fragment = ShopShipAllFragment.newInstance(ShopTabManagerPositionEnum.CANCEL.getPosition());
                    }
                    break;
                case R.id.shop_ship_menu_shipper_manager:
                    fragment = ShopShipperManagerFragment.newInstance();
                    break;
                //case R.id.shop_ship_menu_info:
                //fragment = CustomerInfoFragment.newInstance();
                //break;
                case R.id.menu_support:
                    Utils.callPhone(getActivity(), getString(R.string.hot_line_number));
                    break;
                case R.id.menu_referral_code:
                    fragment = ReferralCodeFragment.newInstance();
                    break;
                case R.id.menu_news:
                    fragment = NewsFragment.newInstance();
                    break;
                case R.id.logout:
                    sessionManager.logoutUser();
            }
            if (fragment != null) {

                mLastFragment = fragment;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                FragmentManager fragmentManager = mContext.getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.shop_layout_container, fragment).commit();
                    }
                }, 300);

            }
        }

        if (v.getId() != R.id.logout)
        {
            mNavigatorSelectListener.onNavigatorSelected();
        }

    }

    @Override
    public void onTabChanged(int position) {
        int viewId = 0;
        switch (position){
            case 1:
                viewId = R.id.shop_ship_menu_wait;
                break;
            case 2:
                viewId = R.id.shop_ship_menu_receive;
                break;
            case 3:
                viewId = R.id.shop_ship_menu_shipping;
                break;
            case 4:
                viewId = R.id.shop_ship_menu_end;
                break;
            case 5:
                viewId = R.id.shop_ship_menu_complete;
                break;
            case 6:
                viewId = R.id.shop_ship_menu_cancel;
                break;
            default:
                viewId = R.id.shop_ship_menu_all;
                break;
        }
        prevMenuSelected.setSelected(false);
        rootView.findViewById(viewId).setSelected(true);
        prevMenuSelected = rootView.findViewById(viewId);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = (FragmentActivity)context;
        if(mContext instanceof OnNavigatorSelected){
            mNavigatorSelectListener = (OnNavigatorSelected) mContext;
        }
    }
}
