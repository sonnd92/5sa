package fiveship.vn.fiveship.utility.validator.validation;

import android.content.Context;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.utility.validator.Validation;

/**
 * Created by sonnd on 20/10/2015.
 */
public class NotEmpty extends BaseValidation {
    String fieldName;
    private NotEmpty(Context context, String name) {
        super(context);
        fieldName = name;
    }
    public static Validation build(Context context, String name) {
        return new NotEmpty(context, name);
    }
    @Override
    public String getErrorMessage() {
        return mContext.getString(R.string.validation_not_empty, fieldName.toLowerCase());
    }

    @Override
    public boolean isValid(String text) {
        return text != null && !text.trim().equals("");
    }
}
