package fiveship.vn.fiveship.utility.validator.validation;

import android.content.Context;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.utility.validator.Validation;

/**
 * Created by sonnd on 20/10/2015.
 */
public class InRange extends BaseValidation {
    private int mMin;
    private int mMax;
    private String fieldName;

    private InRange(Context context,String name, int min, int max) {
        super(context);
        mMin = min;
        mMax = max;
        fieldName = name;
    }

    public static Validation build(Context context, String name, int min, int max) {
        return new InRange(context, name, min, max);
    }

    @Override
    public String getErrorMessage() {
        return mContext.getString(R.string.validations_not_in_range, fieldName, mMin, mMax);
    }

    @Override
    public boolean isValid(String text) {
        try {
            int length = text.length();
            if ((length >= mMin) && (length <= mMax)) {
                return true;
            }
        } catch (NumberFormatException ignored) {
        }
        return false;
    }
}
