package fiveship.vn.fiveship.utility.validator.validation;

/**
 * Created by sonnd on 19/10/2015.
 */
import android.content.Context;

import fiveship.vn.fiveship.utility.validator.Validation;


abstract class BaseValidation implements Validation {

    protected Context mContext;

    protected BaseValidation(Context context) {
        mContext = context;
    }

}
