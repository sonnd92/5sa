package fiveship.vn.fiveship.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.FacebookSdk;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.utility.SessionManager;

public class ReferralCodeFragment extends Fragment {

    private Context mContext;
    private SessionManager sessionManager;
    //private CallbackManager callbackManager;
    private ShareDialog shareDialog;

    public ReferralCodeFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static ReferralCodeFragment newInstance() {
        ReferralCodeFragment fragment = new ReferralCodeFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_referral_code, container, false);
        FacebookSdk.sdkInitialize(mContext.getApplicationContext());
        //callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(this);
        // this part is optional
       /* shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>(){

            @Override
            public void onSuccess(Sharer.Result result) {

            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });
        */
        sessionManager = new SessionManager(mContext);

        ((TextView) rootView.findViewById(R.id.txv_referral_code)).setText(sessionManager.getReferralCode());

        final String SHARE_TITLE = "5SHIP - ỨNG DỤNG VẬN CHUYỂN";
        final String SHARE_CONTENT =
                "Nhập mã " + sessionManager.getReferralCode() + " của tôi khi đăng ký Ứng dụng Vận Chuyển 5SHIP để nhận nhiều tặng phẩm hấp dẫn.";
        final String SHARE_IMAGE_URL = getString(R.string.icon_launcher_url);

        rootView.findViewById(R.id.invite_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TITLE, SHARE_TITLE);
                shareIntent.putExtra(Intent.EXTRA_SUBJECT , SHARE_TITLE);
                shareIntent.putExtra(Intent.EXTRA_TEXT, SHARE_CONTENT +
                        "\nỨng dụng 5Ship: " + getString(R.string.smart_share_link));
                startActivity(Intent.createChooser(shareIntent, "Chia sẻ mã giới thiệu qua"));
            }
        });


        rootView.findViewById(R.id.invite_via_fb_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ShareDialog.canShow(ShareLinkContent.class)) {
                    ShareLinkContent linkContent = new ShareLinkContent.Builder()
                            .setContentTitle(SHARE_TITLE)
                            .setImageUrl(Uri.parse(SHARE_IMAGE_URL))
                            .setContentDescription(SHARE_CONTENT)
                            .setContentUrl(Uri.parse(mContext.getString(R.string.smart_share_link)))
                            .build();

                    shareDialog.show(linkContent);
                }
            }
        });

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //callbackManager.onActivityResult(requestCode, resultCode, data);
    }

}
