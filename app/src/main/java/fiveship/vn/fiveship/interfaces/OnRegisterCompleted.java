package fiveship.vn.fiveship.interfaces;

import fiveship.vn.fiveship.model.CustomerItem;

/**
 * Created by sonnd on 08/01/2016.
 */
public interface OnRegisterCompleted {
    void onRegisterCompleted(boolean isNull, CustomerItem cus, String message);
}
