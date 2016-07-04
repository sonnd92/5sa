package fiveship.vn.fiveship.model;

/**
 * Created by sonnd on 15/10/2015.
 */
public class ShopItem {
    public int Id;
    public int CustomerId;
    public String Name;
    public String Phone;
    public String AddressDetail;
    public String Address;
    public String TaxCode;
    public String Carrer;
    public String XPoint;
    public String YPoint;
    public CustomerItem Account;

    public ShopItem() {
    }

    public ShopItem(int id,int customerId, String name, String address, String taxCode, String carrer, String XPoint, String YPoint, CustomerItem account) {
        Id = id;
        CustomerId = customerId;
        Name = name;
        Address = address;
        TaxCode = taxCode;
        Carrer = carrer;
        this.XPoint = XPoint;
        this.YPoint = YPoint;
        Account = account;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
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

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public String getTaxCode() {
        return TaxCode;
    }

    public void setTaxCode(String taxCode) {
        TaxCode = taxCode;
    }

    public String getCarrer() {
        return Carrer;
    }

    public void setCarrer(String carrer) {
        Carrer = carrer;
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

    public CustomerItem getAccount() {
        return Account;
    }

    public void setAccount(CustomerItem account) {
        Account = account;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public String getAddressDetail() {
        return AddressDetail;
    }

    public void setAddressDetail(String addressDetail) {
        AddressDetail = addressDetail;
    }
}
