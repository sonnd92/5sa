package fiveship.vn.fiveship.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by sonnd on 21/10/2015.
 */
public class LocationItem implements Serializable, Parcelable {
    private int Id;
    private String Name;
    private String Phone;
    private String Address;
    private String Latitude;
    private String Longitude;
    private static final long serialVersionUID = -7060210544600464481L;

    public LocationItem() {
    }

    public LocationItem(String latitude, String longitude) {
        Latitude = latitude;
        Longitude = longitude;
    }

    public LocationItem(int id, String name, String phone, String latitude, String longitude, String address) {
        Id = id;
        Name = name;
        Phone = phone;
        Latitude = latitude;
        Longitude = longitude;
        Address = address;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public String getLatitude() {
        return Latitude;
    }

    public void setLatitude(String latitude) {
        Latitude = latitude;
    }

    public String getLongitude() {
        return Longitude;
    }

    public void setLongitude(String longitude) {
        Longitude = longitude;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(Id);
        dest.writeString(Name);
        dest.writeString(Phone);
        dest.writeString(Address);
        dest.writeString(Latitude);
        dest.writeString(Longitude);
    }
    public static final Parcelable.Creator<LocationItem> CREATOR
            = new Parcelable.Creator<LocationItem>() {
        public LocationItem createFromParcel(Parcel in) {
            return new LocationItem(in);
        }

        public LocationItem[] newArray(int size) {
            return new LocationItem[size];
        }
    };

    private LocationItem(Parcel in) {
        Id = in.readInt();
        Name = in.readString();
        Phone = in.readString();
        Address = in.readString();
        Latitude = in.readString();
        Longitude = in.readString();
    }
}
