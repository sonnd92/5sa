package fiveship.vn.fiveship.model;

/**
 * Created by Unstopable on 6/7/2016.
 */
public class NewsItem {
    private int Id;
    private String Title;
    private String Content;
    private String Summary;
    private String Author;
    private String DateCreated;
    private String UrlImageCover;

    public NewsItem() {
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getContent() {
        return Content;
    }

    public void setContent(String content) {
        Content = content;
    }

    public String getAuthor() {
        return Author;
    }

    public void setAuthor(String author) {
        Author = author;
    }

    public String getDateCreated() {
        return DateCreated;
    }

    public void setDateCreated(String dateCreated) {
        DateCreated = dateCreated;
    }

    public String getSummary() {
        return Summary;
    }

    public void setSummary(String summary) {
        Summary = summary;
    }

    public String getUrlImageCover() {
        return UrlImageCover;
    }

    public void setUrlImageCover(String urlImageCover) {
        UrlImageCover = urlImageCover;
    }
}
