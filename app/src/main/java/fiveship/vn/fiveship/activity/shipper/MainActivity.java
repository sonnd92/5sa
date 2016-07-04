package fiveship.vn.fiveship.activity.shipper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.action.AcceptAppAction;
import fiveship.vn.fiveship.activity.FShipService;
import fiveship.vn.fiveship.activity.NotificationActivity;
import fiveship.vn.fiveship.interfaces.OnNavigatorSelected;
import fiveship.vn.fiveship.interfaces.OnTabChanged;
import fiveship.vn.fiveship.utility.Config;
import fiveship.vn.fiveship.utility.SessionManager;
import fiveship.vn.fiveship.utility.Utils;


public class MainActivity extends AppCompatActivity implements ShipperHistoryTab.OnShipTabTitleChanged, OnTabChanged, OnNavigatorSelected {
    SessionManager sessionManager;
    Intent mapIntent;

    /*Navigation drawer layout*/
    public DrawerLayout mDrawerLayout;

    private ActionBarDrawerToggle mDrawerToggle;

    public FrameLayout mFrameLayout;
    private Toolbar mToolbar;
    private Boolean exit = false;
    private OnShipTabTitleChanged mTitleChangeListener;
    private OnNavigatorChanged mOnNavigatorChangedListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(this);
        setContentView(R.layout.activity_main);

        setupToolBar();

        setupDrawerContent();

        sessionManager = new SessionManager(getApplicationContext());

        AcceptAppAction acceptAppAction = new AcceptAppAction(MainActivity.this, sessionManager);

        acceptAppAction.post();

        acceptAppAction.setAcceptAppActionCallback(new AcceptAppAction.AcceptAppActionCallback() {
            @Override
            public void callBackSuccess() {
                updateNotificationsBadge();
            }
        });

        mapIntent = new Intent(this, MapFragment.class);

        Bundle locations = getIntent().getExtras();
        if (locations != null && locations.containsKey("locations")) {
            mapIntent.putExtras(locations);
        }

        BroadcastReceiver bReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Config.RECEIVE_JSON_SHIPPER)) {
                    //String strData = intent.getStringExtra("json");
                    sessionManager.setNumberNotify(sessionManager.getNumberNotify() + 1);
                    updateNotificationsBadge();
                }
            }
        };

        LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Config.RECEIVE_JSON_SHIPPER);
        bManager.registerReceiver(bReceiver, intentFilter);

        if(sessionManager.isNotificationOn()) {
            Intent intent = new Intent(this, FShipService.class);
            startService(intent);
        }

    }

    public void setupToolBar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapIntent = new Intent(this, MapFragment.class);
        Bundle locations = getIntent().getExtras();
        if (locations != null && locations.containsKey("locations")) {
            mapIntent.putExtras(locations);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_shipping_order, menu);

        // Get the notifications MenuItem and
        // its LayerDrawable (layer-list)
        MenuItem item = menu.findItem(R.id.action_notification);
        LayerDrawable icon = (LayerDrawable) item.getIcon();

        // Update LayerDrawable's BadgeDrawable
        Utils.setBadgeCount(this, icon, sessionManager.getNumberNotify());

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case android.R.id.home:
                openNavigator();
                break;
            case R.id.action_quest:
                openQuest();
                break;
            case R.id.action_notification:
                sessionManager.setNumberNotify(0);
                updateNotificationsBadge();
                openNotification();
                break;
            /*case R.id.action_search:
                Intent searchIntent = new Intent(this, SearchActivity.class);
                startActivity(searchIntent);
                break;*/

        }

        return super.onOptionsItemSelected(item);
    }

    private void updateNotificationsBadge() {
        invalidateOptionsMenu();
    }

    public void openNavigator() {
        mDrawerLayout.openDrawer(mFrameLayout);

    }

    public void openNotification() {
        Intent navigationIntent = new Intent(MainActivity.this, NotificationActivity.class);
        startActivity(navigationIntent);
    }

    public void openQuest() {
        Intent quest = new Intent(MainActivity.this, QuestListActivity.class);
        startActivity(quest);
    }

    public void setupDrawerContent() {
        // find rootView by id
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mFrameLayout = (FrameLayout) findViewById(R.id.menu);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.menu, new NavigatorFragment()).commit();
        fragmentManager.beginTransaction().replace(R.id.layout_container, new ShippingOrderFragment()).commit();

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, 0, 0) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
            }
        };

        try {
            mDrawerLayout.addDrawerListener(mDrawerToggle);
            mDrawerLayout.post(new Runnable() {
                @Override
                public void run() {
                    mDrawerToggle.syncState();
                }
            });
        } catch (Exception ex) {
            reBuildDrawerLayout();
        }
    }

    void reBuildDrawerLayout() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    mDrawerLayout.addDrawerListener(mDrawerToggle);
                    mDrawerLayout.post(new Runnable() {
                        @Override
                        public void run() {
                            mDrawerToggle.syncState();
                        }
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }, 5 * 1000);

    }

    @Override
    public void onBackPressed() {
        if (exit) {
            finish();
        } else {
            Toast.makeText(this, getString(R.string.exit_message), Toast.LENGTH_SHORT).show();
            exit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit = false;
                }
            }, 2 * 1000);

        }
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        if (fragment instanceof OnShipTabTitleChanged) {
            mTitleChangeListener = (OnShipTabTitleChanged) fragment;
        }
        if (fragment instanceof OnNavigatorChanged) {
            mOnNavigatorChangedListener = (OnNavigatorChanged) fragment;
        }
    }

    @Override
    public void onShipTabTitleChanged(int idx, String title) {
        if (mTitleChangeListener != null)
            mTitleChangeListener.onShipTitleChanged(idx, title);
    }

    @Override
    public void onTabChanged(int position) {
        if (mOnNavigatorChangedListener != null)
            mOnNavigatorChangedListener.onNavigatorChanged(position);
    }

    @Override
    public void onNavigatorSelected() {
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    public interface OnShipTabTitleChanged {
        void onShipTitleChanged(int idx, String title);
    }

    public interface OnNavigatorChanged {
        void onNavigatorChanged(int position);
    }
}
