package fiveship.vn.fiveship.activity.shipper;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.adapter.NotificationAdapter;
import fiveship.vn.fiveship.adapter.QuestAdapter;
import fiveship.vn.fiveship.model.NotificationItem;
import fiveship.vn.fiveship.service.NotificationService;
import fiveship.vn.fiveship.service.QuestService;
import fiveship.vn.fiveship.utility.SessionManager;

public class QuestListActivity extends ActionBarActivity {

    private SessionManager sessionManager;

    private QuestService questService;
    private QuestAdapter questAdapter;
    private ListView lvDataBinding;
    private View footerLoading;
    private ViewStub vs_noResult;
    private ArrayList<JSONObject> data = new ArrayList();
    boolean isDataLoaded = false;
    private Animation fadeIn, fadeOut;

    public QuestListActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quest);

        setupToolBar();

        questService = QuestService.get_instance(this);

        sessionManager = new SessionManager(getApplication());

        lvDataBinding = (ListView) findViewById(R.id.quest_list);

        new GetListQuest().execute();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void bindData()
    {
        questAdapter = new QuestAdapter(this, data);
        //lvDataBinding.setOnScrollListener(this);
        //lvDataBinding.setOnItemClickListener(this);
        // setup loader
        vs_noResult = (ViewStub) findViewById(R.id.vs_no_result);
        //footerLoading =  inflater.inflate(R.layout.listview_footer_loadmore, lvDataBinding, false);
        //fade anim
        fadeIn = AnimationUtils.loadAnimation(this, R.anim.abc_fade_in);
        fadeOut = AnimationUtils.loadAnimation(this, R.anim.abc_fade_out);
        if(data.size() > 0) {
            //show list rootView when data is loaded
            lvDataBinding.setAdapter(questAdapter);
            lvDataBinding.startAnimation(fadeIn);
            findViewById(R.id.layout_loading).setAnimation(fadeOut);
            findViewById(R.id.layout_loading).setVisibility(View.GONE);
        }else {
            findViewById(R.id.layout_loading).setAnimation(fadeOut);
            findViewById(R.id.layout_loading).setVisibility(View.GONE);
            vs_noResult.inflate();
        }
    }

    public void setupToolBar()
    {
        Toolbar toolbar = (Toolbar) findViewById(R.id.quest_toolbar);
        setSupportActionBar(toolbar);
    }

    private class GetListQuest extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {
            return questService.getListQuest(sessionManager.isShipper() ? sessionManager.getShipperId() : sessionManager.getShopId(), !sessionManager.isShipper(), 0, 1000);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            try {
                if (result != null && result.length() > 0 && result.has("error") && !result.getBoolean("error")) {

                    JSONArray listJO = result.getJSONArray("data");

                    for (int i = 0; i < listJO.length(); i++){
                        data.add(listJO.getJSONObject(i));
                    }

                    bindData();

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
