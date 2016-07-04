package fiveship.vn.fiveship.model;

import java.io.Serializable;

/**
 * Created by sonnd on 18/10/2015.
 */
public class NotificationItem implements Serializable {
    public int Id;
    public String Title;
    public String Name;
    public String UrlAvatar;
    public int TypeId;
    public int ShipId;
    public int ShopId;
    public int ShipperId;
    public int NotifyId;
    public String DateCreated;
    public boolean IsView;
    public String Data;

    public NotificationItem() {
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

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public int getTypeId() {
        return TypeId;
    }

    public void setTypeId(int typeId) {
        TypeId = typeId;
    }

    public int getShipId() {
        return ShipId;
    }

    public void setShipId(int shipId) {
        ShipId = shipId;
    }

    public int getShopId() {
        return ShopId;
    }

    public void setShopId(int shopId) {
        ShopId = shopId;
    }

    public int getNotifyId() {
        return NotifyId;
    }

    public void setNotifyId(int notifyId) {
        NotifyId = notifyId;
    }

    public int getShipperId() {
        return ShipperId;
    }

    public void setShipperId(int shipperId) {
        ShipperId = shipperId;
    }

    public String getDateCreated() {
        return DateCreated;
    }

    public void setDateCreated(String dateCreated) {
        DateCreated = dateCreated;
    }

    public boolean getIsView() {
        return IsView;
    }

    public void setIsView(boolean isView) {
        IsView = isView;
    }

    public String getUrlAvatar() {
        return UrlAvatar;
    }

    public void setUrlAvatar(String urlAvatar) {
        UrlAvatar = urlAvatar;
    }

    public String getData() {
        return Data;
    }

    public void setData(String data) {
        Data = data;
    }
}
