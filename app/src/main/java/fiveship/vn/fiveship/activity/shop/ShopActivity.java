package fiveship.vn.fiveship.activity.shop;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import org.json.JSONObject;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.action.AcceptAppAction;
import fiveship.vn.fiveship.activity.FShipService;
import fiveship.vn.fiveship.activity.NotificationActivity;
import fiveship.vn.fiveship.interfaces.OnNavigatorSelected;
import fiveship.vn.fiveship.interfaces.OnTabChanged;
import fiveship.vn.fiveship.service.ShopService;
import fiveship.vn.fiveship.utility.Config;
import fiveship.vn.fiveship.utility.ImageProcess;
import fiveship.vn.fiveship.utility.SessionManager;
import fiveship.vn.fiveship.utility.Utils;


public class ShopActivity extends AppCompatActivity implements ShopShipperManagerTab.OnTitleChanged, ShopHistoryTab.OnShopTabTitleChanged, OnTabChanged, OnNavigatorSelected {

    private DrawerLayout mDrawerLayout;
    private FrameLayout mFrameLayout;
    private Boolean exit = false;
    private SessionManager sessionManager;
    private ShopService shopService;
    private OnTitleChanged mTitleChangeListener;
    private OnShopTabTitleChanged mShopTabTitleChangeListener;
    private OnTabChangedFromActivity mNavigatorChangeListener;
    private Toolbar mToolbar;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int SELECT_FILE = 2;
    private String encodedImage;
    String mCurrentPhotoPath;

    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shop_activity);

        setupToolBar();
        setupDrawerContent();

        shopService = ShopService.get_instance(this);

        sessionManager = new SessionManager(getApplicationContext());

        AcceptAppAction acceptAppAction = new AcceptAppAction(ShopActivity.this, sessionManager);

        acceptAppAction.post();

        acceptAppAction.setAcceptAppActionCallback(new AcceptAppAction.AcceptAppActionCallback() {
            @Override
            public void callBackSuccess() {
                updateNotificationsBadge();
            }
        });

        BroadcastReceiver bReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Config.RECEIVE_JSON_SHOP)) {
                    sessionManager.setNumberNotify(sessionManager.getNumberNotify() + 1);
                    updateNotificationsBadge();
                    //String serviceJsonString = intent.getStringExtra("json");
                }
            }
        };

        LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Config.RECEIVE_JSON_SHOP);
        bManager.registerReceiver(bReceiver, intentFilter);

        if(sessionManager.isNotificationOn()) {
            Intent intent = new Intent(this, FShipService.class);
            startService(intent);
        }
    }

    public void setupToolBar()
    {
        mToolbar = (Toolbar) findViewById(R.id.shop_toolbar);
        setSupportActionBar(mToolbar);
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        if(fragment instanceof OnTitleChanged){
            mTitleChangeListener = (OnTitleChanged) fragment;
        }
        if(fragment instanceof OnShopTabTitleChanged){
            mShopTabTitleChangeListener = (OnShopTabTitleChanged) fragment;
        }
        if(fragment instanceof OnTabChangedFromActivity){
            mNavigatorChangeListener = (OnTabChangedFromActivity) fragment;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //getSupportActionBar().show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_shop, menu);

        // Get the notifications MenuItem and
        // its LayerDrawable (layer-list)
        MenuItem item = menu.findItem(R.id.shop_action_notification);
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
        switch (id){
            case android.R.id.home:
                openNavigator();
                break;
            case R.id.shop_action_notification:
                sessionManager.setNumberNotify(0);
                updateNotificationsBadge();
                openNotification();
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    private void updateNotificationsBadge() {
        invalidateOptionsMenu();
    }

    public void openNavigator(){
        mDrawerLayout.openDrawer(mFrameLayout);
    }

    public void openNotification(){
        Intent navigationIntent = new Intent(ShopActivity.this, NotificationActivity.class);
        startActivity(navigationIntent);
    }

    public void setupDrawerContent() {
        // find view by id
        mDrawerLayout = (DrawerLayout) findViewById(R.id.shop_drawer_layout);
        mFrameLayout = (FrameLayout) findViewById(R.id.shop_menu);

        FragmentManager fragmentManager = getSupportFragmentManager();

        ShopNavigatorFragment navLeft = new ShopNavigatorFragment();

        navLeft.setShopNavigator(new ShopNavigatorFragment.ShopNavigator() {
            @Override
            public void onCapture() {
                capturePictureProduct();
            }
        });

        fragmentManager.beginTransaction().replace(R.id.shop_menu, navLeft).commit();
        fragmentManager.beginTransaction().replace(R.id.shop_layout_container, new ShopShipFragment()).commit();

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

    public void reBuildDrawerLayout() {
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                encodedImage = ImageProcess.bitmapToBase64(ImageProcess.decodeResizeImage( 600), 100);//mCurrentPhotoPath,
            } else if (requestCode == SELECT_FILE) {
                Uri selectedImageUri = data.getData();
                String[] projection = {MediaStore.MediaColumns.DATA};
                Cursor cursor = managedQuery(selectedImageUri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                cursor.moveToFirst();
                mCurrentPhotoPath = cursor.getString(column_index);
                encodedImage = ImageProcess.bitmapToBase64(ImageProcess.decodeResizeImage(600), 100);//mCurrentPhotoPath
            }

            new CapturePictureForWebTask().execute();
        }
    }

    private void capturePictureProduct(){
        final CharSequence[] items = { "Chụp ảnh", "Chọn từ thư viện", "Thoát" };
        AlertDialog.Builder builder = new AlertDialog.Builder(ShopActivity.this);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Chụp ảnh")) {
                    dispatchTakePictureIntent();
                } else if (items[item].equals("Chọn từ thư viện")) {
                    Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(
                            Intent.createChooser(intent, "Chọn file"),
                            SELECT_FILE);
                } else if (items[item].equals("Thoát")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private File createImageFile() {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = null;
        try {
            image = File.createTempFile(
                    imageFileName,
                    ".jpg",
                    storageDir
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCurrentPhotoPath = image.getPath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = createImageFile();
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    public void inTitleChanged(int index, String title) {
        if(mTitleChangeListener != null)
            mTitleChangeListener.onTitleChanged(index, title);
    }

    @Override
    public void onShopTitleChanged(int i, String t) {
        if(mShopTabTitleChangeListener != null)
            mShopTabTitleChangeListener.onShopTabTitleChanged(i, t);
    }

    @Override
    public void onTabChanged(int position) {
        if(mNavigatorChangeListener != null){
            mNavigatorChangeListener.onTabChanged(position);
        }
    }

    @Override
    public void onNavigatorSelected() {
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    private class CapturePictureForWebTask extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {
            return shopService.capturePictureForWeb(sessionManager.getShopId(), encodedImage);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            try {
                if(result != null){
                    Toast.makeText(ShopActivity.this, result.getString("message"), Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public interface OnTitleChanged{
        void onTitleChanged(int i, String title);
    }

    public interface OnShopTabTitleChanged{
        void onShopTabTitleChanged(int i, String t);
    }
    public interface OnTabChangedFromActivity{
        void onTabChanged(int position);
    }
}
