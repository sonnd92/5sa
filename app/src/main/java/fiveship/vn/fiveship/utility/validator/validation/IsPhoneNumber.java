package fiveship.vn.fiveship.utility.validator.validation;

import android.content.Context;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.utility.validator.Validation;

/**
 * Created by sonnd on 20/10/2015.
 */
public class IsPhoneNumber extends BaseValidation {

    private static final String PHONE_PATTERN = "^0[0-9]{8,10}$";
    protected IsPhoneNumber(Context context) {
        super(context);
    }

    public static Validation build(Context context) {
        return new IsPhoneNumber(context);
    }

    @Override
    public String getErrorMessage() {
        return mContext.getString(R.string.validations_phone_number);
    }

    @Override
    public boolean isValid(String text) {
        return text.matches(PHONE_PATTERN);
    }
}
