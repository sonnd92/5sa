package fiveship.vn.fiveship.utility.enums;

/**
 * Created by sonnd on 30/10/2015.
 */
public enum OrderStatusEnum {
    PENDING(1),
    RECEIVED(2),
    SHIPPING(3),
    END(4),
    COMPLETED(5),
    CANCEL(6);


    private int statusCode;

    OrderStatusEnum(int c){
        statusCode = c;
    }

    public int getStatusCode(){
        return statusCode;
    }
}
