package fiveship.vn.fiveship.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.utility.SessionManager;
import fiveship.vn.fiveship.utility.Utils;

public class WarningActivity extends AppCompatActivity {

    Bundle info;
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_warning);

        info = getIntent().getExtras();

        sessionManager = new SessionManager(this);

        Utils.setImageToImageViewNoDefault(this, info.getCharSequence("img").toString(), (ImageView) findViewById(R.id.img_warning));

        ((TextView)findViewById(R.id.message_warning)).setText(info.getCharSequence("message"));

        findViewById(R.id.img_warning).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("market://details?id=fiveship.vn.fiveship"));
                startActivity(intent);
            }
        });

        findViewById(R.id.btn_exit_warning).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backWarning();
            }
        });

    }

    private void backWarning(){
        sessionManager.logoutUser();
    }

    @Override
    public void onBackPressed() {
        backWarning();
    }
}
