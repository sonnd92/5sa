package fiveship.vn.fiveship.model;

/**
 * Created by Unstoppable on 6/15/2016.
 */
public class ReviewOfShipperItem {
    private int NumberRating;
    private String Reviewer;
    private String ReviewContent;
    private String DateTime;
    private String ReviewerAvatarUrl;

    public ReviewOfShipperItem() {

    }

    public int getNumberRating() {
        return NumberRating;
    }

    public void setNumberRating(int numberRating) {
        NumberRating = numberRating;
    }

    public String getReviewer() {
        return Reviewer;
    }

    public void setReviewer(String reviewer) {
        Reviewer = reviewer;
    }

    public String getReviewContent() {
        return ReviewContent;
    }

    public void setReviewContent(String reviewContent) {
        ReviewContent = reviewContent;
    }

    public String getDateTime() {
        return DateTime;
    }

    public void setDateTime(String dateTime) {
        DateTime = dateTime;
    }

    public String getReviewerAvatarUrl() {
        return ReviewerAvatarUrl;
    }

    public void setReviewerAvatarUrl(String reviewerAvatarUrl) {
        ReviewerAvatarUrl = reviewerAvatarUrl;
    }
}
