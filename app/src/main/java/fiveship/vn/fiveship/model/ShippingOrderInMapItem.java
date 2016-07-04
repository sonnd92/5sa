package fiveship.vn.fiveship.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sonnd on 05/11/2015.
 */
public class ShippingOrderInMapItem implements Parcelable {
    private int Id;
    private String DateCreate;
    private String Name;
    private double CostShip;
    private String UrlPicture;
    private String ShortInfo;
    private LocationItem OrderFrom;
    private LocationItem ShippingTo;
    private int TotalDuplicate;
    private ArrayList<ShippingOrderInMapItem> orderSameCdn;

    public ShippingOrderInMapItem() {
        TotalDuplicate = 1;
    }

    public ArrayList<ShippingOrderInMapItem> getOrderSameCdn() {
        return orderSameCdn;
    }

    public void addOrderSameCdn(ShippingOrderInMapItem orderSameCdn) {
        if(getOrderSameCdn() != null) {
            this.orderSameCdn.add(orderSameCdn);
        }else{
            this.orderSameCdn = new ArrayList<>();
            this.orderSameCdn.add(orderSameCdn);
        }
    }

    public int getTotalDuplicate() {
        return TotalDuplicate;
    }

    public void setTotalDuplicate(int totalDuplicate) {
        TotalDuplicate = totalDuplicate;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public String getDateCreate() {
        return DateCreate;
    }

    public void setDateCreate(String dateCreate) {
        DateCreate = dateCreate;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public double getCostShip() {
        return CostShip;
    }

    public void setCostShip(double costShip) {
        CostShip = costShip;
    }

    public String getUrlPicture() {
        return UrlPicture;
    }

    public void setUrlPicture(String urlPicture) {
        UrlPicture = urlPicture;
    }

    public String getShortInfo() {
        return ShortInfo;
    }

    public void setShortInfo(String shortInfo) {
        ShortInfo = shortInfo;
    }

    public LocationItem getOrderFrom() {
        return OrderFrom;
    }

    public void setOrderFrom(LocationItem orderFrom) {
        OrderFrom = orderFrom;
    }

    public LocationItem getShippingTo() {
        return ShippingTo;
    }

    public void setShippingTo(LocationItem shippingTo) {
        ShippingTo = shippingTo;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(Id);
        out.writeString(DateCreate);
        out.writeString(Name);
        out.writeDouble(CostShip);
        out.writeString(UrlPicture);
        out.writeParcelable(OrderFrom, flags);
        out.writeParcelable(ShippingTo, flags);
    }

    public static final Parcelable.Creator<ShippingOrderInMapItem> CREATOR
            = new Parcelable.Creator<ShippingOrderInMapItem>() {
        public ShippingOrderInMapItem createFromParcel(Parcel in) {
            return new ShippingOrderInMapItem(in);
        }

        public ShippingOrderInMapItem[] newArray(int size) {
            return new ShippingOrderInMapItem[size];
        }
    };

    private ShippingOrderInMapItem(Parcel in) {
        Id = in.readInt();
        DateCreate = in.readString();
        Name = in.readString();
        CostShip = in.readDouble();
        UrlPicture = in.readString();
        OrderFrom = in.readParcelable(LocationItem.class.getClassLoader());
        ShippingTo = in.readParcelable(LocationItem.class.getClassLoader());
    }
}
