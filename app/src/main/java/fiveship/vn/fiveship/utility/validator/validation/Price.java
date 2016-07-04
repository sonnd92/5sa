package fiveship.vn.fiveship.utility.validator.validation;

import android.content.Context;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.utility.validator.Validation;

/**
 * Created by sonnd on 28/10/2015.
 */
public class Price extends BaseValidation {

    private static final String PRICE_PATTERN =
            "^[0-9]+[k|K]?$";

    private Price(Context context) {
        super(context);
    }

    public static Validation build(Context context) {
        return new Price(context);
    }

    @Override
    public String getErrorMessage() {
        return mContext.getString(R.string.validation_price);
    }

    @Override
    public boolean isValid(String text) {
        return text.matches(PRICE_PATTERN) || text.isEmpty();
    }
}
