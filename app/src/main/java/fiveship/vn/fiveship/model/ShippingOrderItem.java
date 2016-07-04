package fiveship.vn.fiveship.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by sonnd on 13/10/2015.
 */
public class ShippingOrderItem implements Serializable{
    private int Id;
    private int StatusId;
    private int ShopId;
    private int ShipperId;
    private int CustomerId;
    private double CostShip;
    private double Prepay;
    private String ShopName;
    private String Name;
    private String Summary;
    private String Details;
    private String DateStart;
    private String DateEnd;
    private String DateCreated;
    private String DateUpdated;
    private String UrlPicture;
    private String UrlPictureLabel;
    private String Distance;
    private String ShipperNote;
    private String ShipperName;
    private String ShipperPhone;
    private String ShipperLabel;
    private String ShopLabel;
    private String StatusName;
    private String StatusNote;
    private String StrTotalValue;
    private String StrCostShip;
    private String StrProperty;
    private String Message;
    private boolean IsTemplate;
    private boolean IsLight;
    private boolean IsHeavy;
    private boolean IsBig;
    private boolean IsBreak;
    private boolean IsFood;
    private boolean IsAssign;
    private boolean IsExpired;
    private boolean IsShowMessage;
    private LocationItem OrderFrom;
    private LocationItem ShippingTo;
    private StatusItem Status;
    private int TotalShipper;
    private boolean IsGroup;
    private ArrayList<DeliveryToItem> ListShipToOfGroup;
    private boolean IsUrgent;
    private boolean IsShopReliable;
    private boolean HasPromotionCode;
    private String MinRecommendShippingCost;

    public ShippingOrderItem() {
    }

    public int getTotalShipper() {
        return TotalShipper;
    }

    public void setTotalShipper(int totalShipper) {
        TotalShipper = totalShipper;
    }

    public StatusItem getStatus() {
        return Status;
    }

    public void setStatus(StatusItem status) {
        Status = status;
    }

    public String getStatusName() {
        return StatusName;
    }

    public void setStatusName(String statusName) {
        StatusName = statusName;
    }

    public String getStatusNote() {
        return StatusNote;
    }

    public void setStatusNote(String statusNote) {
        StatusNote = statusNote;
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

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public int getStatusId() {
        return StatusId;
    }

    public void setStatusId(int statusId) {
        StatusId = statusId;
    }

    public String getShopName() {
        return ShopName;
    }

    public void setShopName(String shopName) {
        ShopName = shopName;
    }

    public int getShopId() {
        return ShopId;
    }

    public void setShopId(int shopId) {
        ShopId = shopId;
    }

    public int getShipperId() {
        return ShipperId;
    }

    public void setShipperId(int shipperId) {
        ShipperId = shipperId;
    }

    public int getCustomerId() {
        return CustomerId;
    }

    public void setCustomerId(int customerId) {
        CustomerId = customerId;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getSummary() {
        return Summary;
    }

    public void setSummary(String summary) {
        Summary = summary;
    }

    public String getDetails() {
        return Details;
    }

    public void setDetails(String details) {
        Details = details;
    }

    public String getDistance() {
        return Distance;
    }

    public void setDistance(String distance) {
        Distance = distance;
    }

    public double getCostShip() {
        return CostShip;
    }

    public void setCostShip(double costShip) {
        CostShip = costShip;
    }

    public double getPrepay() {
        return Prepay;
    }

    public void setPrepay(double prepay) {
        Prepay = prepay;
    }

    public String getShipperNote() {
        return ShipperNote;
    }

    public void setShipperNote(String shipperNote) {
        ShipperNote = shipperNote;
    }

    public String getDateStart() {
        return DateStart;
    }

    public void setDateStart(String dateStart) {
        DateStart = dateStart;
    }

    public String getDateEnd() {
        return DateEnd;
    }

    public void setDateEnd(String dateEnd) {
        DateEnd = dateEnd;
    }

    public String getDateCreated() {
        return DateCreated;
    }

    public void setDateCreated(String dateCreated) {
        DateCreated = dateCreated;
    }

    public String getDateUpdated() {
        return DateUpdated;
    }

    public void setDateUpdated(String dateUpdated) {
        DateUpdated = dateUpdated;
    }

    public boolean isTemplate() {
        return IsTemplate;
    }

    public void setIsTemplate(boolean isTemplate) {
        IsTemplate = isTemplate;
    }

    public String getUrlPicture() {
        return UrlPicture;
    }

    public void setUrlPicture(String urlPicture) {
        UrlPicture = urlPicture;
    }

    public String getShipperName() {
        return ShipperName;
    }

    public void setShipperName(String shipperName) {
        ShipperName = shipperName;
    }

    public boolean getIsBreak() {
        return IsBreak;
    }

    public void setIsBreak(boolean isBreak) {
        IsBreak = isBreak;
    }

    public boolean getIsBig() {
        return IsBig;
    }

    public void setIsBig(boolean isBig) {
        IsBig = isBig;
    }

    public boolean getIsAssign() {
        return IsAssign;
    }

    public void setIsAssign(boolean isAssign) {
        IsAssign = isAssign;
    }

    public boolean getIsExpired() {
        return IsExpired;
    }

    public void setIsExpired(boolean isExpired) {
        IsExpired = isExpired;
    }

    public boolean getIsFood() {
        return IsFood;
    }

    public void setIsFood(boolean isFood) {
        IsFood = isFood;
    }

    public String getStrCostShip() {
        return StrCostShip;
    }

    public void setStrCostShip(String strCostShip) {
        StrCostShip = strCostShip;
    }

    public String getStrTotalValue() {
        return StrTotalValue;
    }

    public void setStrTotalValue(String strTotalValue) {
        StrTotalValue = strTotalValue;
    }

    public boolean isHeavy() {
        return IsHeavy;
    }

    public void setIsHeavy(boolean isHeavy) {
        IsHeavy = isHeavy;
    }

    public boolean isLight() {
        return IsLight;
    }

    public void setIsLight(boolean isLight) {
        IsLight = isLight;
    }

    public String getStrProperty() {
        return StrProperty;
    }

    public void setStrProperty(String strProperty) {
        StrProperty = strProperty;
    }

    public String getShipperPhone() {
        return ShipperPhone;
    }

    public void setShipperPhone(String shipperPhone) {
        ShipperPhone = shipperPhone;
    }

    public String getShipperLabel() {
        return ShipperLabel;
    }

    public void setShipperLabel(String shipperLabel) {
        ShipperLabel = shipperLabel;
    }

    public String getShopLabel() {
        return ShopLabel;
    }

    public void setShopLabel(String shopLabel) {
        ShopLabel = shopLabel;
    }

    public String getUrlPictureLabel() {
        return UrlPictureLabel;
    }

    public void setUrlPictureLabel(String urlPictureLabel) {
        UrlPictureLabel = urlPictureLabel;
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }

    public boolean isShowMessage() {
        return IsShowMessage;
    }

    public void setIsShowMessage(boolean isShowMessage) {
        IsShowMessage = isShowMessage;
    }

    public boolean isGroup() {
        return IsGroup;
    }

    public void setIsGroup(boolean isGroup) {
        IsGroup = isGroup;
    }

    public ArrayList<DeliveryToItem> getListShipToOfGroup() {
        return ListShipToOfGroup;
    }

    public void setListShipToOfGroup(ArrayList<DeliveryToItem> listShipToOfGroup) {
        ListShipToOfGroup = listShipToOfGroup;
    }

    public boolean isUrgent() {
        return IsUrgent;
    }

    public void setIsUrgent(boolean isUrgent) {
        IsUrgent = isUrgent;
    }

    public boolean isShopReliable() {
        return IsShopReliable;
    }

    public void setIsShopReliable(boolean isShopReliable) {
        IsShopReliable = isShopReliable;
    }

    public String getMinRecommendShippingCost() {
        return MinRecommendShippingCost;
    }

    public void setMinRecommendShippingCost(String minRecommendShippingCost) {
        MinRecommendShippingCost = minRecommendShippingCost;
    }

    public boolean isHasPromotionCode() {
        return HasPromotionCode;
    }

    public void setHasPromotionCode(boolean hasPromotionCode) {
        HasPromotionCode = hasPromotionCode;
    }
}
