package fiveship.vn.fiveship.interfaces;

import android.location.Location;

/**
 * Created by sonnd on 23/02/2016.
 */
public interface LocationEventListener {
    void OnLocationStateChange(int status);
    void OnLocationChange(Location location);
}
