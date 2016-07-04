package fiveship.vn.fiveship.utility.enums;

/**
 * Created by Unstopable on 4/11/2016.
 */
public enum ShipperMngDataChangeEnum {
    BLOCK(1),
    REMOVE_BLOCK(2),
    FAVORITE(3),
    REMOVE_FAVORITE(4);

    private int statusCode;

    ShipperMngDataChangeEnum(int c){
        statusCode = c;
    }

    public int getStatusCode(){
        return statusCode;
    }
}
