package fiveship.vn.fiveship.model;

import java.io.Serializable;

/**
 * Created by sonnd on 13/10/2015.
 */
public class ShipperItem implements Serializable {

    private int Id;
    private String MotorId;
    private String XPoint;
    private String YPoint;
    private String NumberShip;
    private String NumberShipSuccess;
    private String ShipStatistic;
    private String Name;
    private String AvatarUrl;
    private String AvatarLabelUrl;
    private boolean IsBlock;
    private boolean IsFavorite;
    private boolean IsConfirm;


    public ShipperItem(){}

    public String getAvatarUrl() {
        return AvatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        AvatarUrl = avatarUrl;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public String getMotorId() {
        return MotorId;
    }

    public void setMotorId(String motorId) {
        MotorId = motorId;
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

    public String getNumberShip() {
        return NumberShip;
    }

    public void setNumberShip(String numberShip) {
        NumberShip = numberShip;
    }

    public String getNumberShipSuccess() {
        return NumberShipSuccess;
    }

    public void setNumberShipSuccess(String numberShipSuccess) {
        NumberShipSuccess = numberShipSuccess;
    }

    public String getShipStatistic() {
        return ShipStatistic;
    }

    public void setShipStatistic(String shipStatistic) {
        ShipStatistic = shipStatistic;
    }

    public boolean isBlock() {
        return IsBlock;
    }

    public void setIsBlock(boolean isBlock) {
        IsBlock = isBlock;
    }

    public boolean isFavorite() {
        return IsFavorite;
    }

    public void setIsFavorite(boolean isFavorite) {
        IsFavorite = isFavorite;
    }

    public boolean isConfirm() {
        return IsConfirm;
    }

    public void setIsConfirm(boolean isConfirm) {
        IsConfirm = isConfirm;
    }

    public String getAvatarLabelUrl() {
        return AvatarLabelUrl;
    }

    public void setAvatarLabelUrl(String avatarLabelUrl) {
        AvatarLabelUrl = avatarLabelUrl;
    }
}
