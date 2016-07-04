package fiveship.vn.fiveship.activity.shop;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.action.CancelShipAction;
import fiveship.vn.fiveship.action.ChangeStatusShipAction;
import fiveship.vn.fiveship.action.FeedbackAction;
import fiveship.vn.fiveship.action.ResetShipAction;
import fiveship.vn.fiveship.activity.GalleryActivity;
import fiveship.vn.fiveship.activity.shipper.ShipperInfoDialogFragment;
import fiveship.vn.fiveship.interfaces.OnSendRequestCompleted;
import fiveship.vn.fiveship.model.DeliveryToItem;
import fiveship.vn.fiveship.model.LocationItem;
import fiveship.vn.fiveship.model.NotificationItem;
import fiveship.vn.fiveship.model.ShipperItem;
import fiveship.vn.fiveship.model.ShippingOrderItem;
import fiveship.vn.fiveship.service.ShipperService;
import fiveship.vn.fiveship.service.ShippingOrderService;
import fiveship.vn.fiveship.service.ShopService;
import fiveship.vn.fiveship.service.adapter.ChangeShippingCost;
import fiveship.vn.fiveship.service.adapter.GetShipperAssigned;
import fiveship.vn.fiveship.utility.enums.OrderStatusEnum;
import fiveship.vn.fiveship.utility.SessionManager;
import fiveship.vn.fiveship.utility.Utils;

public class ShopShipDetailActivity extends AppCompatActivity {

    private int id;
    private int secondsLeft;
    private SessionManager sessionManager;

    private ShippingOrderItem model;

    private Animation fadeIn;
    private Animation fadeOut;
    private View vs_noResult;
    private SwipeRefreshLayout swipeRefreshLayout;
    private AlertDialog dialogCall;
    private Dialog loadingDialog;
    private String mLinkShare;

    onGetDetailCompleted callback;

    private Runnable r;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.shop_ship_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.order_detail_toolbar);

        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            getSupportActionBar().setHomeButtonEnabled(true);
        }

        sessionManager = new SessionManager(this);

        Intent detail = getIntent();

        id = detail.getIntExtra("id", 0);

        loadingDialog = Utils.setupLoadingDialog(this);
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

        getData();

        switch (detail.getIntExtra("NotifyType", 0)) {
            case 10:
                long diffTime = new Date().getTime() - detail.getLongExtra("NotifyTime", 0);
                secondsLeft = detail.getIntExtra("SecondsLeft", 0) - Math.round(diffTime / 1000);
                AlertDialog.Builder rBuilder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.NonActivityDialog));
                LayoutInflater _inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View rView = _inflater.inflate(R.layout.count_down_for_auto_choose_shipper_popup, null);
                rBuilder.setView(rView);

                final AlertDialog dialog = rBuilder.create();
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

                rView.findViewById(R.id.layout_loading).setVisibility(View.GONE);

                final TextView tvCountDown = (TextView) rView.findViewById(R.id.count_down_seconds);
                ((TextView) rView.findViewById(R.id.tv_count_down_message)).setText(detail.getStringExtra("Content"));
                ((TextView) rView.findViewById(R.id.number_shipper_register)).setText(String.valueOf(detail.getIntExtra("NumberShipper", 0)));
                ((TextView) rView.findViewById(R.id.tv_shipping_order_name)).setText(detail.getStringExtra("ShipName"));

                if (secondsLeft > 0) {
                    rView.findViewById(R.id.count_down_box).setVisibility(View.VISIBLE);

                    tvCountDown.setText(secondsLeft + "s");
                    r = new Runnable() {
                        @Override
                        public void run() {
                            if (secondsLeft > 0) {
                                secondsLeft--;
                                tvCountDown.setText(secondsLeft + "s");
                                new Handler().postDelayed(r, 1000);
                            } else {
                                getShipperAssigned(rView, dialog);
                            }
                        }
                    };

                    new Handler().postDelayed(r, 1000);
                } else {
                    getShipperAssigned(rView, dialog);
                }

                rView.findViewById(R.id.btn_yes).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent listShipperAssign = new Intent(ShopShipDetailActivity.this, ShopListShipperAssignShip.class);
                        listShipperAssign.putExtra("id", id);
                        startActivity(listShipperAssign);
                        dialog.dismiss();
                    }
                });
                rView.findViewById(R.id.btn_no).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                dialog.show();
                break;
        }
    }

    private void getData() {
        new GetDetail(callback, id).execute();
    }


    private void getShipperAssigned(final View rView, final Dialog dialog) {

        rView.findViewById(R.id.layout_loading).setVisibility(View.VISIBLE);
        rView.findViewById(R.id.count_down_box).setVisibility(View.GONE);
        rView.findViewById(R.id.btn_yes).setVisibility(View.GONE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                new GetShipperAssigned(ShipperService.get_instance(ShopShipDetailActivity.this), sessionManager.getShopId(), id, new GetShipperAssigned.OnGetShipperCompleted() {
                    @Override
                    public void onGetShipperCompleted(ShipperItem result, String message) {
                        if (result != null) {

                            ((TextView) rView.findViewById(R.id.tv_shipper_name)).setText(result.getName());
                            ((TextView) rView.findViewById(R.id.tv_motor_license)).setText(result.getMotorId());
                            ((TextView) rView.findViewById(R.id.tv_order_rate)).setText(result.getShipStatistic());
                            ((TextView) rView.findViewById(R.id.tv_message)).setText(message);

                            Utils.setImageToImageView(ShopShipDetailActivity.this, result.getAvatarUrl(), ((ImageView) rView.findViewById(R.id.cus_image)));
                            Utils.setImageToImageView(ShopShipDetailActivity.this, result.getAvatarLabelUrl(), ((ImageView) rView.findViewById(R.id.cus_image_label)));

                            rView.findViewById(R.id.layout_loading).setAnimation(fadeOut);
                            rView.findViewById(R.id.layout_loading).setVisibility(View.GONE);
                            rView.findViewById(R.id.shipper_info_box).setAnimation(fadeIn);
                            rView.findViewById(R.id.shipper_info_box).setVisibility(View.VISIBLE);

                        } else {
                            dialog.dismiss();
                        }
                    }
                }).execute();
            }
        }, 1000);
    }

    public void initView() {

        fadeIn = AnimationUtils.loadAnimation(this, R.anim.abc_fade_in);

        fadeOut = AnimationUtils.loadAnimation(this, R.anim.abc_fade_out);

        vs_noResult = findViewById(R.id.vs_no_result);

        findViewById(R.id.btn_try).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getData();
            }
        });

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));
                getData();
            }
        });

        findViewById(R.id.btn_order_ship_detail_call).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Use the Builder class for convenient dialog construction
                AlertDialog.Builder builder = new AlertDialog.Builder(ShopShipDetailActivity.this);

                View viewCall = getLayoutInflater().inflate(R.layout.dialog_shop_call, null);

                builder.setView(viewCall);

                viewCall.findViewById(R.id.btn_shipper_call_shipper).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Utils.callPhone(getApplicationContext(), model.getShipperPhone());
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

        findViewById(R.id.btn_order_ship_detail_tracking).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent tracking = new Intent(getApplication(), ShopTrackingShipperActivity.class);
                tracking.putExtra("id", model.getId());
                tracking.putExtra("shopId", model.getShopId());
                startActivity(tracking);
            }
        });
        findViewById(R.id.btn_share_jouney).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.shareJourneyDialog(ShopShipDetailActivity.this, mLinkShare).show();
            }
        });

        findViewById(R.id.btn_order_ship_detail_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelShipOrder();
            }
        });

        findViewById(R.id.btn_order_ship_detail_reset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ResetShipAction resetShipAction = new ResetShipAction(ShopShipDetailActivity.this, model.getId(), model.getShopId());

                resetShipAction.reset();

                resetShipAction.setResetShipActionCallback(new ResetShipAction.ResetShipActionCallback() {
                    @Override
                    public void callBackSuccess(String date) {
                        model.setIsExpired(false);
                        model.setDateEnd(date);
                        getData();
                    }
                });
            }
        });

        findViewById(R.id.btn_order_ship_detail_done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                completeShipOrder();
            }
        });

        findViewById(R.id.order_dt_detail_shipper).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showShipperInfo();
            }
        });

        findViewById(R.id.btn_order_ship_detail_shipper).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent listShipper = new Intent(getApplication(), ShopListShipperAssignShip.class);
                listShipper.putExtra("id", model.getId());
                startActivity(listShipper);
            }
        });
    }

    public void changeShippingCost(View view) {
        final AlertDialog.Builder rBuilder = new AlertDialog.Builder(new ContextThemeWrapper(ShopShipDetailActivity.this, R.style.NonActivityDialog));

        final View dialogView = LayoutInflater.from(ShopShipDetailActivity.this)
                .inflate(R.layout.change_shipping_cost_popup_layout, null);

        final EditText et = (EditText) dialogView.findViewById(R.id.tv_change_shipping_cost);
        rBuilder.setView(dialogView);
        final AlertDialog dialog = rBuilder.create();
        dialogView.findViewById(R.id.submit_change_shipping_cost).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!et.getText().toString().isEmpty()) {
                    loadingDialog.show();
                    new ChangeShippingCost(new OnSendRequestCompleted() {
                        @Override
                        public void onSendRequestCompleted(boolean error, String message) {
                            loadingDialog.dismiss();
                            Toast.makeText(ShopShipDetailActivity.this, message, Toast.LENGTH_SHORT).show();
                            if (!error) {
                                ((TextView) findViewById(R.id.order_dt_detail_cost_ship)).setText(et.getText().toString() + "k");
                                ((TextView) findViewById(R.id.group_detail_shipping_cost)).setText(et.getText().toString() + "k");
                            }
                            dialog.dismiss();
                        }
                    },
                            ShopService.get_instance(ShopShipDetailActivity.this),
                            ShopShipDetailActivity.this, sessionManager.getShopId(),
                            id,
                            Integer.parseInt(et.getText().toString())).execute();
                } else {
                    dialog.dismiss();
                }
            }
        });

        dialog.show();
    }

    public void bindingData() {

        if (model.isUrgent()) {
            findViewById(R.id.urgent_banner).setVisibility(View.VISIBLE);
        }
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(model.getName());

        Utils.setImageToImageView(this, model.getUrlPicture(), (ImageView) findViewById(R.id.order_image));

        Utils.setImageToImageViewNoDefault(this, model.getShipperLabel(), (ImageView) findViewById(R.id.order_dt_detail_shipper_label));

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

        ((TextView) findViewById(R.id.order_dt_detail_shipper)).setText(model.getShipperName());
        ((TextView) findViewById(R.id.order_dt_from_address)).setText(model.getOrderFrom().getAddress());

        branching();

        findViewById(R.id.btn_order_ship_detail_reset).setVisibility(View.GONE);

        findViewById(R.id.btn_order_ship_detail_shipper).setVisibility(View.GONE);

        findViewById(R.id.btn_order_ship_detail_shipper_empty).setVisibility(View.GONE);

        findViewById(R.id.btn_order_ship_detail_call).setVisibility(View.GONE);

        findViewById(R.id.btn_order_ship_detail_cancel).setVisibility(View.GONE);

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
            findViewById(R.id.box_step_ship_detail).setVisibility(View.GONE);
            ((TextView) findViewById(R.id.message_ship_detail)).setText(model.getMessage());
        } else {
            findViewById(R.id.box_step_ship_detail).setVisibility(View.VISIBLE);
            findViewById(R.id.box_message_ship_detail).setVisibility(View.GONE);
        }

        if (model.getStatusId() == OrderStatusEnum.PENDING.getStatusCode() && model.getIsExpired()) {
            findViewById(R.id.btn_order_ship_detail_reset).setVisibility(View.VISIBLE);
            findViewById(R.id.btn_order_ship_detail_cancel).setVisibility(View.VISIBLE);
            findViewById(R.id.btn_change_group_shipping_cost).setVisibility(View.VISIBLE);
            findViewById(R.id.btn_change_shipping_cost).setVisibility(View.VISIBLE);
        }

        if (model.getStatusId() == OrderStatusEnum.PENDING.getStatusCode() && !model.getIsExpired() && model.getTotalShipper() > 0) {
            ((Button) findViewById(R.id.btn_order_ship_detail_shipper)).setText("Có " + model.getTotalShipper() + " shipper đăng ký");
            findViewById(R.id.btn_order_ship_detail_shipper).setVisibility(View.VISIBLE);
            findViewById(R.id.btn_order_ship_detail_cancel).setVisibility(View.VISIBLE);
        }

        if (model.getStatusId() == OrderStatusEnum.PENDING.getStatusCode() && !model.getIsExpired() && model.getTotalShipper() == 0) {
            findViewById(R.id.btn_order_ship_detail_shipper_empty).setVisibility(View.VISIBLE);
            findViewById(R.id.btn_order_ship_detail_cancel).setVisibility(View.VISIBLE);
            findViewById(R.id.btn_change_shipping_cost).setVisibility(View.VISIBLE);
            findViewById(R.id.btn_change_group_shipping_cost).setVisibility(View.VISIBLE);
        }

        if (model.getStatusId() == OrderStatusEnum.RECEIVED.getStatusCode()) {
            findViewById(R.id.btn_order_ship_detail_call).setVisibility(View.VISIBLE);
            findViewById(R.id.btn_order_ship_detail_cancel).setVisibility(View.VISIBLE);

            ((ImageView) findViewById(R.id.step_ship_detail1)).setImageResource(R.drawable.bg_ds_receive);
        }

        if (model.getStatusId() == OrderStatusEnum.SHIPPING.getStatusCode()) {
            findViewById(R.id.btn_order_ship_detail_call).setVisibility(View.VISIBLE);
            findViewById(R.id.btn_order_ship_detail_cancel).setVisibility(View.VISIBLE);

            findViewById(R.id.step_ship_detail_line1).setBackgroundColor(Color.parseColor("#5FCBAB"));
            ((ImageView) findViewById(R.id.step_ship_detail1)).setImageResource(R.drawable.bg_ds_receive);
            ((ImageView) findViewById(R.id.step_ship_detail2)).setImageResource(R.drawable.bg_ds_shipping);
        }

        if (model.getStatusId() == OrderStatusEnum.END.getStatusCode()) {
            findViewById(R.id.btn_order_ship_detail_call).setVisibility(View.VISIBLE);
            findViewById(R.id.btn_order_ship_detail_cancel).setVisibility(View.VISIBLE);
            findViewById(R.id.btn_order_ship_detail_done).setVisibility(View.VISIBLE);

            findViewById(R.id.step_ship_detail_line1).setBackgroundColor(Color.parseColor("#5FCBAB"));
            findViewById(R.id.step_ship_detail_line2).setBackgroundColor(Color.parseColor("#5FCBAB"));
            ((ImageView) findViewById(R.id.step_ship_detail1)).setImageResource(R.drawable.bg_ds_receive);
            ((ImageView) findViewById(R.id.step_ship_detail2)).setImageResource(R.drawable.bg_ds_shipping);
            ((ImageView) findViewById(R.id.step_ship_detail3)).setImageResource(R.drawable.bg_ds_finish);
        }

        if (model.getStatusId() == OrderStatusEnum.COMPLETED.getStatusCode()) {
            findViewById(R.id.btn_order_ship_detail_call).setVisibility(View.VISIBLE);

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

        if (model.getStatusId() == 1) {
            findViewById(R.id.action_box).setVisibility(View.GONE);
        }

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

    private void completeShipOrder() {

        ChangeStatusShipAction changeStatus = new ChangeStatusShipAction(
                ShopShipDetailActivity.this,
                ShippingOrderService.get_instance(this),
                model.getId(),
                sessionManager.getShopId(),
                sessionManager.getShipperId(),
                model.getStatusId(),
                model.getStatusNote());

        changeStatus.update();

        changeStatus.setChangeStatusShipActionCallback(new ChangeStatusShipAction.ChangeStatusShipActionCallback() {
            @Override
            public void callBackSuccess() {
                model.setStatusId(OrderStatusEnum.COMPLETED.getStatusCode());
                getData();
            }
        });

    }

    private void cancelShipOrder() {

        CancelShipAction cancelShip = new CancelShipAction(ShopShipDetailActivity.this, model.getId(), sessionManager.getShopId(), model.getName());

        cancelShip.cancel();

        cancelShip.setCancelShipActionCallback(new CancelShipAction.CancelShipActionCallback() {
            @Override
            public void callBackSuccess() {

                model.setStatusId(OrderStatusEnum.CANCEL.getStatusCode());
                getData();
            }
        });

    }

    private void showShipperInfo() {
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = new ShipperInfoDialogFragment(model.getShipperId());
        dialog.show(getSupportFragmentManager(), "ShipperInfoDialogFragment");
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

        switch (menuId) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.menu_contact_and_support:
                Utils.callPhone(this, getString(R.string.hot_line_number));
                break;
            case R.id.menu_feedback:
                new FeedbackAction(this, id, sessionManager.getShopId(), 0, model.getName()).show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private class GetDetail extends AsyncTask<String, Void, JSONObject> {
        private onGetDetailCompleted listener;
        private ShippingOrderService service;

        int id;

        public GetDetail(onGetDetailCompleted _listener, int _id) {
            listener = _listener;
            id = _id;
            service = ShippingOrderService.get_instance(ShopShipDetailActivity.this);
        }

        @Override
        protected JSONObject doInBackground(String... params) {
            return service.GetShippingOrderDetail(id, 0);
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
                    item.setShopId(result.getJSONObject("Shop").getInt("Id"));
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
                    item.setIsExpired(result.has("IsExpired") && result.getBoolean("IsExpired"));
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

                    item.setIsGroup(result.has("IsGroup") && result.getBoolean("IsGroup"));
                    item.setIsUrgent(result.has("IsUrgent") && result.getBoolean("IsUrgent"));
                    item.setIsShopReliable(result.has("IsShopConfirm") && result.getBoolean("IsShopConfirm"));

                    mLinkShare = result.has("LinkShare") ? result.getString("LinkShare") : "";

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
                    item.setTotalShipper(result.has("TotalShipper") ? result.getInt("TotalShipper") : 0);

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
