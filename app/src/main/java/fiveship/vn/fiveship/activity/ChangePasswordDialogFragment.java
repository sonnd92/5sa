package fiveship.vn.fiveship.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONObject;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.service.CustomerService;
import fiveship.vn.fiveship.utility.Utils;
import fiveship.vn.fiveship.utility.validator.Field;
import fiveship.vn.fiveship.utility.validator.Form;
import fiveship.vn.fiveship.utility.validator.validation.Equal;
import fiveship.vn.fiveship.utility.validator.validation.InRange;
import fiveship.vn.fiveship.utility.validator.validation.NotEmpty;


public class ChangePasswordDialogFragment extends DialogFragment {

    private CustomerService customerService;

    private int id;
    private EditText currentPass;
    private EditText newPass;
    private EditText newPass2;
    private Dialog loadingDlg;
    private Dialog alertDlg;
    private AlertDialog dialogChangePass;

    public ChangePasswordDialogFragment() {
    }

    public static ChangePasswordDialogFragment getInstance(int id) {

        ChangePasswordDialogFragment instance = new ChangePasswordDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("accId", id);
        instance.setArguments(bundle);
        return instance;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        id = getArguments().getInt("accId");
        customerService = CustomerService.get_instance(getActivity());

        // Build the dialog and set up the button click handlers
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final Form form = new Form(getActivity());

        final View viewChangePassword = getActivity().getLayoutInflater().inflate(R.layout.dialog_change_password, null);
        builder.setView(viewChangePassword);

        loadingDlg = Utils.setupLoadingDialog(getActivity());
        alertDlg = Utils.setupAlertDialog(getActivity());

        currentPass = (EditText) viewChangePassword.findViewById(R.id.txt_current_pass);

        newPass = (EditText) viewChangePassword.findViewById(R.id.txt_new_pass);

        newPass2 = (EditText) viewChangePassword.findViewById(R.id.txt_new_pass2);

        viewChangePassword.findViewById(R.id.btn_close_change_pass).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogChangePass.dismiss();
            }
        });

        form.addField(Field.using(currentPass)
                .validate(NotEmpty.build(getActivity(), getString(R.string.password_field))));

        form.addField(Field.using(newPass)
                .validate(NotEmpty.build(getActivity(), getString(R.string.new_password_field)))
                .validate(InRange.build(getActivity(), getString(R.string.password_field), 6, 26)));

        form.addField(Field.using(newPass2)
                .validate(NotEmpty.build(getActivity(), getString(R.string.re_password_field)))
                .validate(Equal.EqualTo(getActivity(), newPass, getString(R.string.password_field))));

        viewChangePassword.findViewById(R.id.btn_change_pass).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (form.isValid()) {
                    loadingDlg.show();
                    new ChangePassword().execute(currentPass.getText().toString(), newPass.getText().toString());
                }
            }
        });

        // Set the dialog title
        builder.setTitle("Đổi mật khẩu").setCancelable(false);

        dialogChangePass = builder.create();

        return dialogChangePass;
    }

    private class ChangePassword extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {
            return customerService.changePassword(id, params[0], params[1]);
        }

        @Override
        protected void onPreExecute() {
            loadingDlg.show();
        }

        @Override
        protected void onPostExecute(JSONObject result) {

            loadingDlg.dismiss();

            try {
                ((TextView) alertDlg.findViewById(R.id.tv_content)).setText(result.getString("message"));

            } catch (Exception e) {
                e.printStackTrace();
                ((TextView) alertDlg.findViewById(R.id.tv_content)).setText(getActivity().getString(R.string.exception_message));
            }

            alertDlg.show();
            alertDlg.findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDlg.dismiss();
                }
            });
            dialogChangePass.dismiss();
        }

    }

}
