package fiveship.vn.fiveship.utility.validator.validation;

import android.content.Context;
import android.widget.EditText;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.utility.validator.Validation;

/**
 * Created by sonnd on 08/01/2016.
 */
public class Equal extends BaseValidation {

    Context context;
    EditText compareTo;
    String fieldName;

    private Equal(Context context, EditText editText, String fieldName){
        super(context);
        this.context = context;
        this.compareTo = editText;
        this.fieldName = fieldName;

    }
    public static Validation EqualTo(Context context, EditText editText, String fieldName){
        return new Equal(context, editText, fieldName);
    }

    @Override
    public String getErrorMessage() {
        return context.getString(R.string.re_confirm, fieldName.toLowerCase());
    }

    @Override
    public boolean isValid(String text) {
        return text.equals(compareTo.getText().toString());
    }
}
