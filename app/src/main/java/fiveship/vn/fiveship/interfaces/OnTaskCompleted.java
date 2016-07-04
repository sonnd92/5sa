package fiveship.vn.fiveship.interfaces;

import java.util.ArrayList;

import fiveship.vn.fiveship.model.MessageItem;

/**
 * Created by sonnd on 08/10/2015.
 */
public interface OnTaskCompleted {
    void onTaskCompleted(ArrayList result, int total);
}
