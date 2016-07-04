package fiveship.vn.fiveship.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by sonnd on 08/10/2015.
 */
public class CustomerItem implements Serializable {
    private int Id;
    private String FirstName;
    private String LastName;
    private boolean IsShipper;
    private boolean IsShop;
    private int AvatarId;
    private String Phone;
    private String Email;
    private Date BirthDay;
    private String AddressDetail;
    private String Address;
    private String DateCreated;
    private boolean IsShow;
    private int ShipperId;
    private int ShopId;
    private int ShipId;
    private String Avatar;
    private String AvatarLabel;
    private String XPoint;
    private String YPoint;
    private String MotorId;
    private String ShipStatistics;
    private String Distance;
    private String Note;
    private String ReferralCode;
    private boolean IsAccess;
    private boolean IsOnline;
    private String RecommendShippingCost;
    private boolean IsConfirm;
    private String NumberRating;
    private float NumberStar;

    public CustomerItem() {
    }

    public boolean isOnline() {
        return IsOnline;
    }

    public void setIsOnline(boolean isOnline) {
        IsOnline = isOnline;
    }

    public String getAvatar() {
        return Avatar;
    }

    public void setAvatar(String avatar) {
        Avatar = avatar;
    }

    public String getAvatarLabel() {
        return AvatarLabel;
    }

    public void setAvatarLabel(String avatarLabel) {
        AvatarLabel = avatarLabel;
    }

    public String getFullName() {
        return FirstName + LastName;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public String getFirstName() {
        return FirstName;
    }

    public void setFirstName(String firstName) {
        FirstName = firstName;
    }

    public String getLastName() {
        return LastName;
    }

    public void setLastName(String lastName) {
        LastName = lastName;
    }

    public boolean isShipper() {
        return IsShipper;
    }

    public void setIsShipper(boolean isShipper) {
        IsShipper = isShipper;
    }

    public boolean isShop() {
        return IsShop;
    }

    public void setIsShop(boolean isShop) {
        IsShop = isShop;
    }

    public int getAvatarId() {
        return AvatarId;
    }

    public void setAvatarId(int avatarId) {
        AvatarId = avatarId;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public Date getBirthDay() {
        return BirthDay;
    }

    public void setBirthDay(Date birthDay) {
        BirthDay = birthDay;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public String getAddressDetail() {
        return AddressDetail;
    }

    public void setAddressDetail(String addressDetail) {
        AddressDetail = addressDetail;
    }

    public String getDateCreated() {
        return DateCreated;
    }

    public void setDateCreated(String dateCreated) {
        DateCreated = dateCreated;
    }

    public boolean isShow() {
        return IsShow;
    }

    public int getShipperId() {
        return ShipperId;
    }

    public void setShipperId(int shipperId) {
        ShipperId = shipperId;
    }

    public int getShopId() {
        return ShopId;
    }

    public void setShopId(int shopId) {
        ShopId = shopId;
    }

    public int getShipId() {
        return ShipId;
    }

    public void setShipId(int shipId) {
        ShipId = shipId;
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

    public String getMotorId() {
        return MotorId;
    }

    public void setMotorId(String motorId) {
        MotorId = motorId;
    }

    public String getShipStatistics() {
        return ShipStatistics;
    }

    public void setShipStatistics(String shipStatistics) {
        ShipStatistics = shipStatistics;
    }

    public String getDistance() {
        return Distance;
    }

    public void setDistance(String distance) {
        Distance = distance;
    }

    public String getNote() {
        return Note;
    }

    public void setNote(String note) {
        Note = note;
    }

    public boolean isAccess() {
        return IsAccess;
    }

    public void setIsAccess(boolean isAccess) {
        IsAccess = isAccess;
    }

    public String getReferralCode() {
        return ReferralCode;
    }

    public void setReferralCode(String referralCode) {
        ReferralCode = referralCode;
    }

    public String getRecommendShippingCost() {
        return RecommendShippingCost;
    }

    public void setRecommendShippingCost(String recommendShippingCost) {
        RecommendShippingCost = recommendShippingCost;
    }

    public boolean isConfirm() {
        return IsConfirm;
    }

    public void setConfirm(boolean confirm) {
        IsConfirm = confirm;
    }

    public float getNumberStar() {
        return NumberStar;
    }

    public void setNumberStar(float numberStar) {
        NumberStar = numberStar;
    }

    public String getNumberRating() {
        return NumberRating;
    }

    public void setNumberRating(String numberRating) {
        NumberRating = numberRating;
    }
}
