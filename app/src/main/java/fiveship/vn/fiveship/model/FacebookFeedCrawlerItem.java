package fiveship.vn.fiveship.model;

import java.util.ArrayList;

/**
 * Created by Unstoppable on 5/12/2016.
 */
public class FacebookFeedCrawlerItem {
    private int Id;
    private String CreatorName;
    private String CreatorId;
    private String FeedId;
    private String Content;
    private String TimeCreated;
    private boolean IsPinned;
    private String Distance;
    private ArrayList<String> PhoneNumbers;
    private boolean ShowPinnedIcon;

    public FacebookFeedCrawlerItem() {
    }

    public String getCreatorId() {
        return CreatorId;
    }

    public void setCreatorId(String creatorId) {
        CreatorId = creatorId;
    }

    public String getFeedId() {
        return FeedId;
    }

    public void setFeedId(String feedId) {
        FeedId = feedId;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public String getCreatorName() {
        return CreatorName;
    }

    public void setCreatorName(String creatorName) {
        CreatorName = creatorName;
    }

    public String getContent() {
        return Content;
    }

    public void setContent(String content) {
        Content = content;
    }

    public String getTimeCreated() {
        return TimeCreated;
    }

    public void setTimeCreated(String timeCreated) {
        TimeCreated = timeCreated;
    }

    public boolean isPinned() {
        return IsPinned;
    }

    public void setPinned(boolean pinned) {
        IsPinned = pinned;
    }

    public String getDistance() {
        return Distance;
    }

    public void setDistance(String distance) {
        Distance = distance;
    }

    public ArrayList<String> getPhoneNumbers() {
        return PhoneNumbers;
    }

    public void setPhoneNumbers(ArrayList<String> phoneNumbers) {
        PhoneNumbers = phoneNumbers;
    }

    public boolean isShowPinnedIcon() {
        return ShowPinnedIcon;
    }

    public void setShowPinnedIcon(boolean showPinnedIcon) {
        ShowPinnedIcon = showPinnedIcon;
    }
}
