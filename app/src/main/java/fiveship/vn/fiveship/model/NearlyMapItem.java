package fiveship.vn.fiveship.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by sonnd on 11/12/2015.
 */
public class NearlyMapItem {

    public int Total;
    public String XPoint;
    public String YPoint;
    public String ListId;
    public ShippingOrderItem ShipItem;

    public ShippingOrderItem getShipItem() {
        return ShipItem;
    }

    public void setShipItem(ShippingOrderItem shipItem) {
        ShipItem = shipItem;
    }

    public int getTotal() {
        return Total;
    }

    public void setTotal(int total) {
        Total = total;
    }

    public String getXPoint() {
        return XPoint;
    }

    public void setXPoint(String XPoint) {
        this.XPoint = XPoint;
    }

    public String getYPoint() {
        return YPoint;
    }

    public void setYPoint(String YPoint) {
        this.YPoint = YPoint;
    }

    public String getListId() {
        return ListId;
    }

    public void setListId(String listId) {
        ListId = listId;
    }
}
