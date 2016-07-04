package fiveship.vn.fiveship.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.widget.TextView;

import org.json.JSONObject;

import fiveship.vn.fiveship.R;


public class AlertAppDialogFragment extends DialogFragment {


    private AlertDialog mDialog;
    private int mLayoutId;
    private String mContent;
    private OnDialogButtonClick mListener;
    private OnDialogClose mCloseListener;
    private boolean mCloseWhenClickPositive = true;

    public AlertAppDialogFragment() {
        mLayoutId = R.layout.dialog_alert_app;
    }

    public static AlertAppDialogFragment getInstance() {
        AlertAppDialogFragment instance = new AlertAppDialogFragment();
        Bundle bundle = new Bundle();
        instance.setArguments(bundle);

        return instance;
    }

    public AlertAppDialogFragment setLayout(int layoutId) {
        if (layoutId > 0) {
            this.mLayoutId = layoutId;
        }
        return this;
    }

    public AlertAppDialogFragment setContent(String content) {
        this.mContent = content;
        return this;
    }

    public AlertAppDialogFragment setOnDialogButtonClick(OnDialogButtonClick listener) {
        this.mListener = listener;
        return this;
    }

    public AlertAppDialogFragment setOnDismiss(OnDialogClose listener) {
        this.mCloseListener = listener;
        return this;
    }

    public AlertAppDialogFragment setDismissPositive(boolean dismissPositive){
        this.mCloseWhenClickPositive = dismissPositive;
        return this;
    }

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Build the dialog and set up the button click handlers

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final View viewAlert = getActivity().getLayoutInflater().inflate(mLayoutId, null);
        builder.setView(viewAlert);

        TextView txtContent = (TextView) viewAlert.findViewById(R.id.text);

        if (txtContent != null && mContent != null && !mContent.isEmpty()) {

            Spanned sp = Html.fromHtml(mContent, null, null);

            txtContent.setText(sp);
        }

        // Set the dialog title
        View mBtnYes = viewAlert.findViewById(R.id.btn_yes);

        if (mBtnYes != null) {

            mBtnYes.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            if(mCloseWhenClickPositive)
                                mDialog.dismiss();

                            if (mListener != null) {
                                mListener.onPositiveButtonClicked();
                            }
                        }
                    });
        }
        View mBtnNo = viewAlert.findViewById(R.id.btn_no);

        if (mBtnNo != null) {

            mBtnNo.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            mDialog.dismiss();

                            if (mListener != null) {
                                mListener.onNegativeButtonClicked();
                            }
                        }
                    });
        }

        mDialog = builder.create();

        return mDialog;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mCloseListener != null) {
            mCloseListener.onDialogClose();
        }
    }

    public interface OnDialogButtonClick {
        void onPositiveButtonClicked();
        void onNegativeButtonClicked();
    }

    public interface OnDialogClose{
        void onDialogClose();
    }

}
