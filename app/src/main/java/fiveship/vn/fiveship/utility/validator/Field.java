package fiveship.vn.fiveship.utility.validator;

/**
 * Created by sonnd on 19/10/2015.
 */
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import java.util.LinkedList;
import java.util.List;

public class Field {
    private List<Validation> mValidations = new LinkedList<Validation>();
    private EditText mEditText;

    private Field(EditText editText) {
        this.mEditText = editText;
    }

    public static Field using(EditText editText) {
        return new Field(editText);

    }

    public Field validate(Validation what) {
        mValidations.add(what);
        return this;
    }

    public EditText getEditText() {
        return mEditText;
    }

    public boolean isValid() throws FieldValidationException {
        for (Validation validation : mValidations) {
            if (mEditText != null && isVisible(mEditText)) {
                String content = null;
                if(mEditText instanceof EditText ){
                    content = mEditText.getText().toString();
                }
                if(mEditText instanceof AutoCompleteTextView){
                    content = mEditText.getText().toString();
                }
                if(!validation.isValid(content)){
                    String errorMessage = validation.getErrorMessage();
                    throw new FieldValidationException(errorMessage, mEditText);
                }
            }
        }
        return true;
    }


    private boolean isVisible(View myView) {
        if(myView.getVisibility() != View.VISIBLE){
            return false;
        }
        if (myView.getParent() == myView.getRootView())
            return myView.getVisibility() == View.VISIBLE;
        else
            return isVisible((View)myView.getParent());
    }
}
