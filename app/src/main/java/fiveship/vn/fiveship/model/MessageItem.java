package fiveship.vn.fiveship.model;

/**
 * Created by sonnd on 08/10/2015.
 */
public class MessageItem {
    public int Id;
    public String Message;
    public boolean Error;

    public MessageItem() {
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }

    public boolean isError() {
        return Error;
    }

    public void setError(boolean error) {
        Error = error;
    }
}
