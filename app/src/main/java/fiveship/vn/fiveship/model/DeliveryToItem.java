package fiveship.vn.fiveship.model;

import java.io.Serializable;

/**
 * Created by Unstopable on 07/03/2016.
 */
public class DeliveryToItem implements Serializable {
    private int Id;
    private String Name;
    private String DetailAddress;
    private String Address;
    private int ShipCost;
    private int PrePay;
    private double XPoint;
    private double YPoint;
    private String Note;
    private String Phone;
    private boolean IsDeleted;
    private String DateEnd;
    private int ViewSelectDateEnd;// stored view's id of date end field
    private int RecommendShippingCost;
    private String Distance;

    public DeliveryToItem() {
    }

    public boolean isDeleted() {
        return IsDeleted;
    }

    public void setIsDeleted(boolean isDeleted) {
        IsDeleted = isDeleted;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
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

    public String getDetailAddress() {
        return DetailAddress;
    }

    public void setDetailAddress(String detailAddress) {
        DetailAddress = detailAddress;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public int getShipCost() {
        return ShipCost;
    }

    public void setShipCost(int shipCost) {
        ShipCost = shipCost;
    }

    public int getPrePay() {
        return PrePay;
    }

    public void setPrePay(int prePay) {
        PrePay = prePay;
    }

    public double getXPoint() {
        return XPoint;
    }

    public void setXPoint(double XPoint) {
        this.XPoint = XPoint;
    }

    public double getYPoint() {
        return YPoint;
    }

    public void setYPoint(double YPoint) {
        this.YPoint = YPoint;
    }

    public String getNote() {
        return Note;
    }

    public void setNote(String note) {
        Note = note;
    }

    public String getDateEnd() {
        return DateEnd;
    }

    public void setDateEnd(String dateEnd) {
        DateEnd = dateEnd;
    }

    public int getViewSelectDateEnd() {
        return ViewSelectDateEnd;
    }

    public void setViewSelectDateEnd(int viewSelectDateEnd) {
        ViewSelectDateEnd = viewSelectDateEnd;
    }

    public int getRecommendShippingCost() {
        return RecommendShippingCost;
    }

    public void setRecommendShippingCost(int recommendShippingCost) {
        RecommendShippingCost = recommendShippingCost;
    }

    public String getDistance() {
        return Distance;
    }

    public void setDistance(String distance) {
        Distance = distance;
    }
}
