package fiveship.vn.fiveship.utility.enums;

/**
 * Created by Unstoppable on 4/15/2016.
 */
public enum ShipperTabManagerPositionEnum {
    ALL(0),
    WAIT(1),
    RECEIVED(2),
    SHIPPING(3),
    END(4),
    COMPLETE(5),
    CANCEL(6);


    private int position;

    ShipperTabManagerPositionEnum(int c){
        position = c;
    }

    public int getPosition(){
        return position;
    }
}
