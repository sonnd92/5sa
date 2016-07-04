package fiveship.vn.fiveship.utility.validator;

/**
 * Created by sonnd on 19/10/2015.
 */

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;

import java.util.ArrayList;
import java.util.List;

import fiveship.vn.fiveship.R;


public class Form {

    private List<Field> mFields = new ArrayList<Field>();
    private Context mContext;

    public Form(Context activity) {
        this.mContext = activity;
    }

    public void addField(Field field) {
        mFields.add(field);
    }

    public boolean isValid() {
        boolean result = true;
        try {
            for (Field field : mFields) {
                result &= field.isValid();
                clearError(field.getEditText());
            }
        } catch (FieldValidationException e) {
            result = false;

            showError(e.getEditText(), e.getMessage());

            if (e.getEditText().isEnabled()) {

                e.getEditText().requestFocus();

            }
        }
        return result;
    }

    protected void showError(final EditText editText, final String message) {
        if (editText != null) {
            final ScrollView scrollView = (ScrollView) editText.getRootView().findViewById(R.id.fiveship_form_input);
            final TextInputLayout ly = (TextInputLayout) editText.getParent();
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    if (scrollView != null) {
                        scrollView.smoothScrollTo(0, getRelativeTop(editText) - editText.getHeight() - 200);
                    }

                    editText.requestFocus();
                    ly.setErrorEnabled(true);
                    ly.setError(message);
                }
            });
        }
    }

    protected void clearError(EditText editText) {
        if (editText != null) {
            TextInputLayout ly = (TextInputLayout) editText.getParent();
            if (ly != null) {
                ly.setErrorEnabled(false);
                ly.setError(null);
            }
        }
    }

    private int getRelativeTop(View myView) {
        if (myView.getParent() == myView.getRootView())
            return myView.getTop();
        else
            return myView.getTop() + getRelativeTop((View) myView.getParent());
    }
}
