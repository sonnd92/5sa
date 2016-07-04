package fiveship.vn.fiveship.utility;

/**
 * Created by sonnd on 25/02/2016.
 */
public enum LocationStatus {
    SUCCESS(0),
    DISABLE(1),
    ENABLE(2),
    NULL(3),
    CHANGE(4);


    private int statusCode;

    LocationStatus(int c){
        statusCode = c;
    }

    public int getStatusCode(){
        return statusCode;
    }
}
