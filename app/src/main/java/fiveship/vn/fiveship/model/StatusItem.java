package fiveship.vn.fiveship.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by sonnd on 29/10/2015.
 */
public class StatusItem implements Parcelable {
    private int Id;
    private String Name;

    public StatusItem(int id, String name) {
        Id = id;
        Name = name;
    }

    public StatusItem() {
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
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(Id);
        dest.writeString(Name);
    }
    public static final Parcelable.Creator<StatusItem> CREATOR
            = new Parcelable.Creator<StatusItem>() {
        public StatusItem createFromParcel(Parcel in) {
            return new StatusItem(in);
        }

        public StatusItem[] newArray(int size) {
            return new StatusItem[size];
        }
    };

    private StatusItem(Parcel in) {
        Id = in.readInt();
        Name = in.readString();
    }
}
