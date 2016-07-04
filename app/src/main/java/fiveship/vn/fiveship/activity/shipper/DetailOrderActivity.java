package fiveship.vn.fiveship.activity.shipper;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.action.AssignShipAction;
import fiveship.vn.fiveship.action.CancelAssignShipAction;
import fiveship.vn.fiveship.action.ChangeStatusShipAction;
import fiveship.vn.fiveship.action.FeedbackAction;
import fiveship.vn.fiveship.activity.GalleryActivity;
import fiveship.vn.fiveship.activity.shop.ShopInfoDialogFragment;
import fiveship.vn.fiveship.model.DeliveryToItem;
import fiveship.vn.fiveship.model.LocationItem;
import fiveship.vn.fiveship.model.NotificationItem;
import fiveship.vn.fiveship.model.ShippingOrderItem;
import fiveship.vn.fiveship.service.ShippingOrderService;
import fiveship.vn.fiveship.utility.SessionManager;
import fiveship.vn.fiveship.utility.Utils;
import fiveship.vn.fiveship.utility.enums.OrderStatusEnum;

public class DetailOrderActivity extends AppCompatActivity {

    private int id;

    private SessionManager sessionManager;
    private ShippingOrderItem model;
    private Animation fadeIn;
    private Animation fadeOut;
    private View vs_noResult;
    private SwipeRefreshLayout swipeRefreshLayout;
    private AlertDialog dialogCall;

    private onGetDetailCompleted callback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_detail_order);

        Toolbar toolbar = (Toolbar) findViewById(R.id.order_detail_toolbar);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            getSupportActionBar().setHomeButtonEnabled(true);
        }

        sessionManager = new SessionManager(this);

        Intent detail = getIntent();

        id = detail.getIntExtra("OrderId", 0);

        initView();

        callback = new onGetDetailCompleted() {
            @Override
            public void onGetDetailCompleted(boolean isNull, ShippingOrderItem item) {
                swipeRefreshLayout.setRefreshing(false);
                if (isNull) {
                    findViewById(R.id.layout_loading).setAnimation(fadeOut);
                    findViewById(R.id.layout_loading).setVisibility(View.GONE);
                    vs_noResult.setVisibility(View.VISIBLE);
                } else {
                    model = item;
                    bindingData();
                }
            }
        };

        loadData();

        switch (detail.getIntExtra("NotifyType", 0)) {
            case 5:
                if (detail.getSerializableExtra("NotifyItem") != null) {
                    Utils.CallToShopDialog(this, (NotificationItem) detail.getSerializableExtra("NotifyItem"), false).show();
                }
                break;
        }
    }

    private void loadData() {
        new GetDetail(callback).execute();
    }

    public void initView() {

        fadeIn = AnimationUtils.loadAnimation(this, R.anim.abc_fade_in);

        fadeOut = AnimationUtils.loadAnimation(this, R.anim.abc_fade_out);

        vs_noResult = findViewById(R.id.vs_no_result);

        findViewById(R.id.btn_try).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadData();
            }
        });

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                swipeRefreshLayout.setColorSchemeColors(ActivityCompat.getColor(DetailOrderActivity.this, R.color.colorPrimary));
                loadData();
            }
        });

        findViewById(R.id.btn_order_ship_detail_assign).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AssignShipAction assignShipAction = new AssignShipAction(DetailOrderActivity.this, model, sessionManager.getShipperId(), model.getStatusNote(), model.isShopReliable());

                assignShipAction.assign();

                assignShipAction.setAssignShipActionCallback(new AssignShipAction.AssignShipActionCallback() {
                    @Override
                    public void callBackSuccess() {
                        loadData();
                    }
                });
            }
        });

        findViewById(R.id.btn_order_ship_detail_cancel_assign).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CancelAssignShipAction cancelAssignShipAction = new CancelAssignShipAction(DetailOrderActivity.this, model.getId(), sessionManager.getShipperId(), model.getStatusNote());

                cancelAssignShipAction.cancel();

                cancelAssignShipAction.setCancelAssignShipActionCallback(new CancelAssignShipAction.CancelAssignShipActionCallback() {
                    @Override
                    public void callBackSuccess() {
                        loadData();
                    }
                });
            }
        });

        findViewById(R.id.btn_order_ship_detail_shipping).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateStatusShipOrder();
            }
        });

        findViewById(R.id.btn_order_ship_detail_done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateStatusShipOrder();
            }
        });

        findViewById(R.id.btn_order_ship_detail_direction).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<LatLng> locations = new ArrayList<>();
                Intent mapIntent = new Intent(DetailOrderActivity.this, DirectionActivity.class);
                Bundle locationsBnl = new Bundle();
                if (model.getStatusId() != OrderStatusEnum.SHIPPING.getStatusCode()) {

                    locations.add(new LatLng(Double.parseDouble(model.getOrderFrom().getLatitude())
                            , Double.parseDouble(model.getOrderFrom().getLongitude())));
                }
                if (!model.isGroup()) {
                    locations.add(new LatLng(Double.parseDouble(model.getShippingTo().getLatitude()),
                            Double.parseDouble(model.getShippingTo().getLongitude())));
                } else {
                    for (DeliveryToItem s : model.getListShipToOfGroup()) {
                        locations.add(new LatLng(s.getXPoint(), s.getYPoint()));
                    }
                }

                locationsBnl.putSerializable("locations", locations);
                mapIntent.putExtras(locationsBnl);
                startActivity(mapIntent);
            }
        });

        findViewById(R.id.btn_order_ship_detail_call).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Use the Builder class for convenient dialog construction
                AlertDialog.Builder builder = new AlertDialog.Builder(DetailOrderActivity.this);

                View viewCall = getLayoutInflater().inflate(R.layout.dialog_shipper_call, null);

                builder.setView(viewCall);

                viewCall.findViewById(R.id.btn_shipper_call_shop).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Utils.callPhone(getApplicationContext(), model.getOrderFrom().getPhone());
                        dialogCall.dismiss();
                    }
                });

                viewCall.findViewById(R.id.btn_shipper_call_cus).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Utils.callPhone(getApplicationContext(), model.getShippingTo().getPhone());
                        dialogCall.dismiss();
                    }
                });

                builder.create();

                dialogCall = builder.show();

            }
        });

        findViewById(R.id.order_dt_detail_shop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showShopInfo();
            }
        });


    }

    public void bindingData() {

        if (model.isUrgent()) {
            findViewById(R.id.urgent_banner).setVisibility(View.VISIBLE);
        }

        if (model.isHasPromotionCode()) {
            findViewById(R.id.iv_promotion_label).setVisibility(View.VISIBLE);
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(model.getName());
        }

        Utils.setImageToImageView(this, model.getUrlPicture(), (ImageView) findViewById(R.id.order_image));

        Utils.setImageToImageViewNoDefault(this, model.getShopLabel(), (ImageView) findViewById(R.id.order_dt_detail_shipper_label));

        findViewById(R.id.order_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> listImage = new ArrayList();
                listImage.add(model.getUrlPicture());
                Intent gallery = new Intent(getApplication(), GalleryActivity.class);
                gallery.putStringArrayListExtra("list", listImage);
                startActivity(gallery);
            }
        });

        ((TextView) findViewById(R.id.order_dt_detail_shop)).setText(model.getOrderFrom().getName());
        ((TextView) findViewById(R.id.order_dt_from_address)).setText(model.getOrderFrom().getAddress());
        //handler view for two case (group or not)
        branching();

        findViewById(R.id.btn_order_ship_detail_assign).setVisibility(View.GONE);

        findViewById(R.id.btn_order_ship_detail_cancel_assign).setVisibility(View.GONE);

        findViewById(R.id.btn_order_ship_detail_call).setVisibility(View.GONE);

        findViewById(R.id.btn_order_ship_detail_shipping).setVisibility(View.GONE);

        findViewById(R.id.btn_order_ship_detail_done).setVisibility(View.GONE);

        findViewById(R.id.step_ship_detail_line1).setBackgroundColor(Color.parseColor("#D3D3D3"));
        findViewById(R.id.step_ship_detail_line2).setBackgroundColor(Color.parseColor("#D3D3D3"));
        findViewById(R.id.step_ship_detail_line3).setBackgroundColor(Color.parseColor("#D3D3D3"));
        ((ImageView) findViewById(R.id.step_ship_detail1)).setImageResource(R.drawable.bg_ds_receive_blank);
        ((ImageView) findViewById(R.id.step_ship_detail2)).setImageResource(R.drawable.bg_ds_shipping_blank);
        ((ImageView) findViewById(R.id.step_ship_detail3)).setImageResource(R.drawable.bg_ds_finish_blank);
        ((ImageView) findViewById(R.id.step_ship_detail4)).setImageResource(R.drawable.bg_ds_complete_blank);

        if (model.isShowMessage()) {
            findViewById(R.id.box_message_ship_detail).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.message_ship_detail)).setText(model.getMessage());
            findViewById(R.id.box_step_ship_detail).setVisibility(View.GONE);
        } else {
            findViewById(R.id.box_step_ship_detail).setVisibility(View.VISIBLE);
            findViewById(R.id.box_message_ship_detail).setVisibility(View.GONE);
        }

        if (model.getStatusId() == OrderStatusEnum.PENDING.getStatusCode() && !model.getIsAssign()) {
            findViewById(R.id.btn_order_ship_detail_assign).setVisibility(View.VISIBLE);
        }

        if (model.getStatusId() == OrderStatusEnum.PENDING.getStatusCode() && model.getIsAssign()) {
            findViewById(R.id.btn_order_ship_detail_cancel_assign).setVisibility(View.VISIBLE);
        }

        if (model.getStatusId() == OrderStatusEnum.RECEIVED.getStatusCode()) {
            findViewById(R.id.btn_order_ship_detail_shipping).setVisibility(View.VISIBLE);
            findViewById(R.id.btn_order_ship_detail_call).setVisibility(View.VISIBLE);

            ((ImageView) findViewById(R.id.step_ship_detail1)).setImageResource(R.drawable.bg_ds_receive);
        }

        if (model.getStatusId() == OrderStatusEnum.SHIPPING.getStatusCode()) {
            findViewById(R.id.btn_order_ship_detail_done).setVisibility(View.VISIBLE);
            findViewById(R.id.btn_order_ship_detail_call).setVisibility(View.VISIBLE);

            findViewById(R.id.step_ship_detail_line1).setBackgroundColor(Color.parseColor("#5FCBAB"));
            ((ImageView) findViewById(R.id.step_ship_detail1)).setImageResource(R.drawable.bg_ds_receive);
            ((ImageView) findViewById(R.id.step_ship_detail2)).setImageResource(R.drawable.bg_ds_shipping);
        }

        if (model.getStatusId() == OrderStatusEnum.END.getStatusCode()) {
            findViewById(R.id.btn_order_ship_detail_call).setVisibility(View.VISIBLE);

            findViewById(R.id.step_ship_detail_line1).setBackgroundColor(Color.parseColor("#5FCBAB"));
            findViewById(R.id.step_ship_detail_line2).setBackgroundColor(Color.parseColor("#5FCBAB"));
            ((ImageView) findViewById(R.id.step_ship_detail1)).setImageResource(R.drawable.bg_ds_receive);
            ((ImageView) findViewById(R.id.step_ship_detail2)).setImageResource(R.drawable.bg_ds_shipping);
            ((ImageView) findViewById(R.id.step_ship_detail3)).setImageResource(R.drawable.bg_ds_finish);
        }

        if (model.getStatusId() == OrderStatusEnum.COMPLETED.getStatusCode()) {
            findViewById(R.id.step_ship_detail_line1).setBackgroundColor(Color.parseColor("#5FCBAB"));
            findViewById(R.id.step_ship_detail_line2).setBackgroundColor(Color.parseColor("#5FCBAB"));
            findViewById(R.id.step_ship_detail_line3).setBackgroundColor(Color.parseColor("#5FCBAB"));
            ((ImageView) findViewById(R.id.step_ship_detail1)).setImageResource(R.drawable.bg_ds_receive);
            ((ImageView) findViewById(R.id.step_ship_detail2)).setImageResource(R.drawable.bg_ds_shipping);
            ((ImageView) findViewById(R.id.step_ship_detail3)).setImageResource(R.drawable.bg_ds_finish);
            ((ImageView) findViewById(R.id.step_ship_detail4)).setImageResource(R.drawable.bg_ds_complete);
        }

        findViewById(R.id.layout_loading).setAnimation(fadeOut);

        findViewById(R.id.layout_loading).setVisibility(View.GONE);

        findViewById(R.id.order_detail_container).setAnimation(fadeIn);


    }

    public void branching() {
        LinearLayout addressBox = (LinearLayout) findViewById(R.id.address_box);
        LinearLayout detailInfoContainer = (LinearLayout) findViewById(R.id.order_detail_info_layout_container);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        String DELIVERY_LAYOUT_TAG = "deliveryTag";
        if (model.isGroup()) {

            findViewById(R.id.order_detail_info_box).setVisibility(View.GONE);
            findViewById(R.id.order_info_title).setVisibility(View.GONE);
            findViewById(R.id.order_summary_info_layout_container).setVisibility(View.VISIBLE);
            findViewById(R.id.order_summary_info_title_layout).setVisibility(View.VISIBLE);

            for (int i = 0; i < model.getListShipToOfGroup().size(); i++) {
                DeliveryToItem item = model.getListShipToOfGroup().get(i);
                //delivery address view
                View deliveryView = addressBox.findViewWithTag(DELIVERY_LAYOUT_TAG + String.valueOf(item.getId()));
                if (deliveryView != null) {
                    addressBox.removeView(deliveryView);
                }
                deliveryView = layoutInflater.inflate(R.layout.delivery_address_info_layout_item, null);
                deliveryView.setTag(DELIVERY_LAYOUT_TAG + String.valueOf(item.getId()));

                ((TextView) deliveryView.findViewById(R.id.order_dt_to_address)).setText(item.getAddress());
                ((TextView) deliveryView.findViewById(R.id.add_index)).setText(String.valueOf(i + 1));
                if (i == model.getListShipToOfGroup().size() - 1) {
                    deliveryView.findViewById(R.id.dead_line).setVisibility(View.INVISIBLE);
                }
                addressBox.addView(deliveryView);
                //delivery info view
                if (!item.getNote().isEmpty() || !item.getDateEnd().isEmpty()) {
                    String DELIVERY_DETAIL_LAYOUT_TAG = "deliveryDetailTag";
                    View infoView = detailInfoContainer.findViewWithTag(DELIVERY_DETAIL_LAYOUT_TAG + String.valueOf(item.getId()));
                    if (infoView != null) {
                        detailInfoContainer.removeView(infoView);
                    }
                    infoView = layoutInflater.inflate(R.layout.delivery_detail_info_layout_item, null);
                    infoView.setTag(DELIVERY_DETAIL_LAYOUT_TAG + String.valueOf(item.getId()));
                    ((TextView) infoView.findViewById(R.id.delivery_detail_note)).setText(item.getNote());
                    //((TextView) infoView.findViewById(R.id.delivery_detail_property)).setText(item.get());
                    ((TextView) infoView.findViewById(R.id.info_view_index)).setText(String.format(String.valueOf(i + 1), "%02d"));
                    ((TextView) infoView.findViewById(R.id.delivery_detail_note)).setText(item.getNote());
                    ((TextView) infoView.findViewById(R.id.delivery_detail_date_end)).setText(item.getDateEnd());
                    detailInfoContainer.addView(infoView);
                }

                //summary
                ((TextView) findViewById(R.id.group_detail_name)).setText(model.getName());
                ((TextView) findViewById(R.id.group_detail_pre_pay)).setText(model.getStrTotalValue());
                ((TextView) findViewById(R.id.group_detail_shipping_cost)).setText(model.getStrCostShip());
            }
        } else {
            View deliveryView = addressBox.findViewWithTag(DELIVERY_LAYOUT_TAG);
            if (deliveryView != null) {
                addressBox.removeView(deliveryView);
            }
            deliveryView = layoutInflater.inflate(R.layout.delivery_address_info_layout_item, null);
            deliveryView.setTag(DELIVERY_LAYOUT_TAG);
            ((TextView) deliveryView.findViewById(R.id.order_dt_to_address)).setText(model.getShippingTo().getAddress());
            deliveryView.findViewById(R.id.dead_line).setVisibility(View.INVISIBLE);
            addressBox.addView(deliveryView);

            //order_detail_info_box

            ((TextView) findViewById(R.id.order_dt_detail_date)).setText(model.getDateEnd());

            ((TextView) findViewById(R.id.order_dt_detail_name)).setText(model.getName());

            ((TextView) findViewById(R.id.order_dt_detail_note)).setText(model.getDetails());

            ((TextView) findViewById(R.id.order_dt_detail_cost)).setText(model.getStrTotalValue());

            ((TextView) findViewById(R.id.order_dt_detail_cost_ship)).setText(model.getStrCostShip());

            ((TextView) findViewById(R.id.order_dt_detail_property)).setText(model.getStrProperty());

        }
    }

    private void updateStatusShipOrder() {

        ChangeStatusShipAction changeStatus = new ChangeStatusShipAction(DetailOrderActivity.this, ShippingOrderService.get_instance(this), model.getId(), sessionManager.getShopId(), sessionManager.getShipperId(), model.getStatusId(), model.getStatusNote());

        changeStatus.update();

        changeStatus.setChangeStatusShipActionCallback(new ChangeStatusShipAction.ChangeStatusShipActionCallback() {
            @Override
            public void callBackSuccess() {
                loadData();
            }
        });

    }

    private void showShopInfo() {
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = new ShopInfoDialogFragment(model.getShopId());
        dialog.show(getSupportFragmentManager(), "ShopInfoDialogFragment");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail_order, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int menuId = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (menuId) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.menu_contact_and_support:
                Utils.callPhone(this, getString(R.string.hot_line_number));
                break;
            case R.id.menu_feedback:
                
                new FeedbackAction(this, id, 0, sessionManager.getShipperId(), model.getName()).show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private class GetDetail extends AsyncTask<String, Void, JSONObject> {
        private onGetDetailCompleted listener;
        private ShippingOrderService service;

        public GetDetail(onGetDetailCompleted _listener) {
            listener = _listener;
            service = ShippingOrderService.get_instance(DetailOrderActivity.this);
        }

        @Override
        protected JSONObject doInBackground(String... params) {
            return service.GetShippingOrderDetail(id, sessionManager.getShipperId());
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            ShippingOrderItem item = new ShippingOrderItem();
            try {
                if (result != null && result.length() > 0 && result.has("data") && !result.getString("data").isEmpty()) {

                    result = new JSONObject(result.getString("data"));
                    JSONObject from;
                    JSONObject to;
                    item.setId(result.getInt("Id"));
                    item.setStatusId(result.getInt("StatusId"));
                    item.setStatusName(result.getJSONObject("Status").getString("Name"));
                    item.setStatusNote(result.getJSONObject("Status").getString("Note"));
                    item.setUrlPicture(result.getString("UrlPicture"));
                    item.setName(result.getString("Name"));
                    item.setCostShip(result.getInt("CostShip"));
                    item.setPrepay(result.getInt("Prepay"));
                    item.setDateCreated(result.getString("DateCreated"));
                    item.setDateEnd(result.getString("DateEnd"));
                    item.setDetails(result.getString("Details"));
                    item.setIsBig(result.getBoolean("IsBig"));
                    item.setIsBreak(result.getBoolean("IsBreak"));
                    item.setIsAssign(result.getBoolean("IsAssign"));
                    item.setIsLight(result.has("IsLight") && result.getBoolean("IsLight"));
                    item.setIsHeavy(result.has("IsHeavy") && result.getBoolean("IsHeavy"));
                    item.setIsFood(result.has("IsFood") && result.getBoolean("IsFood"));
                    item.setStrCostShip(result.has("StrCostShip") ? result.getString("StrCostShip") : "");
                    item.setStrTotalValue(result.has("StrTotalValue") ? result.getString("StrTotalValue") : "");
                    item.setStrProperty(result.has("StrProperty") ? result.getString("StrProperty") : "");
                    item.setMessage(result.has("Message") ? result.getString("Message") : "");
                    item.setIsShowMessage(result.has("IsShowMessage") && result.getBoolean("IsShowMessage"));
                    item.setShipperId(result.getJSONObject("Shipper").getInt("Id"));
                    item.setShipperName(result.getJSONObject("Shipper").getString("Name"));
                    item.setShipperPhone(result.getJSONObject("Shipper").getString("Phone"));
                    item.setShipperLabel(result.getJSONObject("Shipper").getString("AvatarLabel"));
                    item.setShopLabel(result.getJSONObject("Shop").getString("AvatarLabel"));
                    item.setIsShopReliable(result.getJSONObject("Shop").has("IsConfirm") && result.getJSONObject("Shop").getBoolean("IsConfirm"));
                    item.setShopId(result.getJSONObject("Shop").getInt("Id"));
                    item.setIsGroup(result.has("IsGroup") && result.getBoolean("IsGroup"));
                    item.setIsUrgent(result.has("IsUrgent") && result.getBoolean("IsUrgent"));
                    item.setHasPromotionCode(result.has("IsPromotionCode") && result.getBoolean("IsPromotionCode"));
                    item.setDetails(result.getString("Details"));

                    if (item.isGroup()) {
                        JSONArray deliveryJSList = result.getJSONArray("ListShipToOfGroup");
                        ArrayList deliveryList = new ArrayList();
                        for (int i = 0; i < deliveryJSList.length(); i++) {
                            JSONObject o = deliveryJSList.getJSONObject(i);
                            DeliveryToItem obj = new DeliveryToItem();
                            obj.setId(o.getInt("Id"));
                            obj.setName(o.getString("Name"));
                            obj.setPhone(o.getString("Phone"));
                            obj.setXPoint(o.getDouble("XPoint"));
                            obj.setYPoint(o.getDouble("YPoint"));
                            obj.setAddress(o.getString("Address"));
                            obj.setNote(o.getString("Note"));
                            obj.setDateEnd(o.getString("DateEnd"));
                            deliveryList.add(obj);
                        }
                        item.setListShipToOfGroup(deliveryList);
                    }

                    from = result.getJSONObject("ShipFrom");
                    item.setOrderFrom(new LocationItem(from.getInt("Id"), from.getString("Name"), from.getString("Phone"), from.getString("XPoint"), from.getString("YPoint"), from.getString("Address")));
                    to = result.getJSONObject("ShipTo");
                    item.setShippingTo(new LocationItem(to.getInt("Id"), to.getString("Name"), to.getString("Phone"), to.getString("XPoint"), to.getString("YPoint"), to.getString("Address")));
                    listener.onGetDetailCompleted(false, item);
                } else {
                    listener.onGetDetailCompleted(true, null);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                listener.onGetDetailCompleted(true, null);
            }
        }
    }

    interface onGetDetailCompleted {
        void onGetDetailCompleted(boolean isNull, ShippingOrderItem item);
    }
}


