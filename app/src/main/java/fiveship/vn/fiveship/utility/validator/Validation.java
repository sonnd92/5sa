package fiveship.vn.fiveship.utility.validator;

/**
 * Created by sonnd on 19/10/2015.
 */
public interface Validation {
    String getErrorMessage();
    boolean isValid(String text);
}
