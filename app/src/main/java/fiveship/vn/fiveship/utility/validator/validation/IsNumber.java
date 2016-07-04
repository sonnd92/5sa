package fiveship.vn.fiveship.utility.validator.validation;

import android.content.Context;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.utility.validator.Validation;

/**
 * Created by sonnd on 13/11/2015.
 */
public class IsNumber extends BaseValidation {

    private static final String NUMBER_PATTERN =
            "^[0-9]+$";

    private IsNumber(Context context) {
        super(context);
    }

    public static Validation build(Context context) {
        return new IsNumber(context);
    }

    @Override
    public String getErrorMessage() {
        return mContext.getString(R.string.validation_number);
    }

    @Override
    public boolean isValid(String text) {
        return text.matches(NUMBER_PATTERN);
    }
}
