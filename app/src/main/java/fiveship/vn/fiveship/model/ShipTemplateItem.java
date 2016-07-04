package fiveship.vn.fiveship.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by sonnd on 13/10/2015.
 */
public class ShipTemplateItem{

    private int Id;
    private int ShopId;
    private String Name;
    private String Note;
    private String CostShip;
    private String PrepayShip;
    private String Date;
    private String StrProperty;
    private boolean IsLight;
    private boolean IsHeavy;
    private boolean IsBig;
    private boolean IsBreak;
    private boolean IsFood;
    private ArrayList<DeliveryToItem> ShipInfoItem;
    private DeliveryToItem ShipFrom;
    private String Image;
    private boolean IsNear;
    private boolean IsSafe;
    private boolean IsGroup;
    private String PromotionCode;

    public DeliveryToItem getShipFrom() {
        return ShipFrom;
    }

    public void setShipFrom(DeliveryToItem shipFrom) {
        ShipFrom = shipFrom;
    }

    public boolean isGroup() {
        return IsGroup;
    }

    public void setIsGroup(boolean isGroup) {
        IsGroup = isGroup;
    }

    public boolean isNear() {
        return IsNear;
    }

    public void setIsNear(boolean isNear) {
        IsNear = isNear;
    }

    public boolean isSafe() {
        return IsSafe;
    }

    public void setIsSafe(boolean isSafe) {
        IsSafe = isSafe;
    }

    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        Image = image;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public int getShopId() {
        return ShopId;
    }

    public void setShopId(int shopId) {
        ShopId = shopId;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getNote() {
        return Note;
    }

    public void setNote(String note) {
        Note = note;
    }

    public String getCostShip() {
        return CostShip;
    }

    public void setCostShip(String costShip) {
        CostShip = costShip;
    }

    public String getPrepayShip() {
        return PrepayShip;
    }

    public void setPrepayShip(String prepayShip) {
        PrepayShip = prepayShip;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getStrProperty() {
        return StrProperty;
    }

    public void setStrProperty(String strProperty) {
        StrProperty = strProperty;
    }

    public boolean isLight() {
        return IsLight;
    }

    public void setIsLight(boolean isLight) {
        IsLight = isLight;
    }

    public boolean isHeavy() {
        return IsHeavy;
    }

    public void setIsHeavy(boolean isHeavy) {
        IsHeavy = isHeavy;
    }

    public boolean isBig() {
        return IsBig;
    }

    public void setIsBig(boolean isBig) {
        IsBig = isBig;
    }

    public boolean isBreak() {
        return IsBreak;
    }

    public void setIsBreak(boolean isBreak) {
        IsBreak = isBreak;
    }

    public boolean isFood() {
        return IsFood;
    }

    public void setIsFood(boolean isFood) {
        IsFood = isFood;
    }

    public String getPromotionCode() {
        return PromotionCode;
    }

    public void setPromotionCode(String promotionCode) {
        PromotionCode = promotionCode;
    }

    public ArrayList<DeliveryToItem> getShipInfoItem() {
        return ShipInfoItem;
    }

    public void setShipInfoItem(ArrayList<DeliveryToItem> shipInfoItem) {
        ShipInfoItem = shipInfoItem;
    }
}
