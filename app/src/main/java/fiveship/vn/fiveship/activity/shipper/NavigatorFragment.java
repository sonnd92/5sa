package fiveship.vn.fiveship.activity.shipper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
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
import fiveship.vn.fiveship.utility.enums.ShipperTabManagerPositionEnum;

public class NavigatorFragment extends Fragment implements View.OnClickListener, MainActivity.OnNavigatorChanged {
    private View prevMenuSelected;
    private SessionManager sessionManager;
    private ImageView avatar;
    private TextView name;
    private View rootView;

    private Fragment fragment;
    private Fragment lastFragment;
    private OnNavigatorSelected mNavigatorSelectListener;

    public NavigatorFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_navigator, container, false);
        View home = rootView.findViewById(R.id.menu_ship_near);

        sessionManager = new SessionManager(getActivity());

        // set menu item on click
        prevMenuSelected = home;

        home.setSelected(true);
        home.setOnClickListener(this);

        rootView.findViewById(R.id.cus_avatar).setOnClickListener(this);
        rootView.findViewById(R.id.menu_ship_all).setOnClickListener(this);
        rootView.findViewById(R.id.logout).setOnClickListener(this);
        rootView.findViewById(R.id.menu_support).setOnClickListener(this);
        rootView.findViewById(R.id.menu_ship_direction).setOnClickListener(this);
        rootView.findViewById(R.id.menu_ship_near).setOnClickListener(this);
        rootView.findViewById(R.id.menu_ship_account).setOnClickListener(this);
        rootView.findViewById(R.id.menu_ship_wait).setOnClickListener(this);
        rootView.findViewById(R.id.menu_ship_receive).setOnClickListener(this);
        rootView.findViewById(R.id.menu_ship_shipping).setOnClickListener(this);
        rootView.findViewById(R.id.menu_ship_end).setOnClickListener(this);
        rootView.findViewById(R.id.menu_ship_complete).setOnClickListener(this);
        rootView.findViewById(R.id.menu_ship_cancel).setOnClickListener(this);
        rootView.findViewById(R.id.menu_referral_code).setOnClickListener(this);
        rootView.findViewById(R.id.menu_facebook_crawler_feed).setOnClickListener(this);
        rootView.findViewById(R.id.menu_news).setOnClickListener(this);

        rootView.findViewById(R.id.menu_ship_box_menu).setOnClickListener(new View.OnClickListener() {
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
            }
        };

        LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(getContext());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Config.RECEIVE_UPDATE_NAVIGATOR);
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

    @Override
    public void onClick(View v) {
        if (!v.isSelected()) {

            fragment = null;

            if (v.getId() != R.id.menu_support) {
                prevMenuSelected.setSelected(false);
                v.setSelected(true);
                prevMenuSelected = v;
            }

            switch (v.getId()) {
                case R.id.menu_ship_near:
                    fragment = ShippingOrderFragment.newInstance();
                    break;
                case R.id.menu_ship_direction:
                    fragment = SamePathOrderFragment.newInstance();
                    break;
                case R.id.menu_ship_all:
                    if (lastFragment instanceof HistoryFragment) {
                        HistoryFragment.ChangeTab(ShipperTabManagerPositionEnum.ALL.getPosition());
                    } else {
                        fragment = HistoryFragment.newInstance(ShipperTabManagerPositionEnum.ALL.getPosition());
                    }
                    break;
                case R.id.menu_ship_wait:
                    if (lastFragment instanceof HistoryFragment) {
                        HistoryFragment.ChangeTab(ShipperTabManagerPositionEnum.WAIT.getPosition());
                    } else {
                        fragment = HistoryFragment.newInstance(ShipperTabManagerPositionEnum.WAIT.getPosition());
                    }
                    break;
                case R.id.menu_ship_receive:
                    if (lastFragment instanceof HistoryFragment) {
                        HistoryFragment.ChangeTab(ShipperTabManagerPositionEnum.RECEIVED.getPosition());
                    } else {
                        fragment = HistoryFragment.newInstance(ShipperTabManagerPositionEnum.RECEIVED.getPosition());
                    }
                    break;
                case R.id.menu_ship_shipping:
                    if (lastFragment instanceof HistoryFragment) {
                        HistoryFragment.ChangeTab(ShipperTabManagerPositionEnum.SHIPPING.getPosition());
                    } else {
                        fragment = HistoryFragment.newInstance(ShipperTabManagerPositionEnum.SHIPPING.getPosition());
                    }
                    break;
                case R.id.menu_ship_end:
                    if (lastFragment instanceof HistoryFragment) {
                        HistoryFragment.ChangeTab(ShipperTabManagerPositionEnum.END.getPosition());
                    } else {
                        fragment = HistoryFragment.newInstance(ShipperTabManagerPositionEnum.END.getPosition());
                    }
                    break;
                case R.id.menu_ship_complete:
                    if (lastFragment instanceof HistoryFragment) {
                        HistoryFragment.ChangeTab(ShipperTabManagerPositionEnum.COMPLETE.getPosition());
                    } else {
                        fragment = HistoryFragment.newInstance(ShipperTabManagerPositionEnum.COMPLETE.getPosition());
                    }
                    break;
                case R.id.menu_ship_cancel:
                    if (lastFragment instanceof HistoryFragment) {
                        HistoryFragment.ChangeTab(ShipperTabManagerPositionEnum.CANCEL.getPosition());
                    } else {
                        fragment = HistoryFragment.newInstance(ShipperTabManagerPositionEnum.CANCEL.getPosition());
                    }
                    break;
                case R.id.menu_support:
                    Utils.callPhone(getActivity(), getActivity().getString(R.string.hot_line_number));
                    break;
                case R.id.menu_ship_account:
                    fragment = ShipHistoryTradeFragment.newInstance();
                    break;
                case R.id.menu_referral_code:
                    fragment = ReferralCodeFragment.newInstance();
                    break;
                case R.id.menu_facebook_crawler_feed:
                    fragment = FacebookCrawlerFragment.newInstance();
                    break;
                case R.id.menu_news:
                    fragment = NewsFragment.newInstance();
                    break;
                case R.id.logout:
                    sessionManager.logoutUser();
                    break;
            }

            if (fragment != null) {
                lastFragment = fragment;

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                        fragmentManager.beginTransaction()
                                .replace(R.id.layout_container, fragment).commit();
                    }
                }, 300);

            }
        }

        if(v.getId() != R.id.logout){
            mNavigatorSelectListener.onNavigatorSelected();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof OnNavigatorSelected){
            mNavigatorSelectListener = (OnNavigatorSelected) context;
        }
    }

    @Override
    public void onNavigatorChanged(int position) {
        int viewId = 0;
        switch (position) {
            case 1:
                viewId = R.id.menu_ship_wait;
                break;
            case 2:
                viewId = R.id.menu_ship_receive;
                break;
            case 3:
                viewId = R.id.menu_ship_shipping;
                break;
            case 4:
                viewId = R.id.menu_ship_end;
                break;
            case 5:
                viewId = R.id.menu_ship_complete;
                break;
            case 6:
                viewId = R.id.menu_ship_cancel;
                break;
            default:
                viewId = R.id.menu_ship_all;
                break;
        }
        prevMenuSelected.setSelected(false);
        rootView.findViewById(viewId).setSelected(true);
        prevMenuSelected = rootView.findViewById(viewId);
    }
}
