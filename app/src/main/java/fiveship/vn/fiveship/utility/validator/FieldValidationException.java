package fiveship.vn.fiveship.utility.validator;

/**
 * Created by sonnd on 19/10/2015.
 */

import android.text.InputType;
import android.view.View;
import android.widget.EditText;


public class FieldValidationException extends Exception {

    private EditText mEditText;

    public FieldValidationException(String message, EditText et) {
        super(message);
        mEditText = et;
    }

    public EditText getEditText() {
        return mEditText;
    }
}
