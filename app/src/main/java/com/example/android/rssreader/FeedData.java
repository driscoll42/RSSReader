package com.example.android.rssreader;

import io.realm.RealmObject;
import io.realm.annotations.Index;

/**
 * Created by Administrator on 4/1/2016.
 */
public class FeedData extends RealmObject {
    public static final String TITLE = "title";
    public static final String SAVED = "saved";
    public static final String CATEGORY = "category";
    public static final String FEEDLINK = "feedLink";


    @Index
    private String title;

    @Index
    private String category;

    @Index
    private boolean saved;

    @Index
    private String feedLink;

    private String link;
    private String description;
    private String pubDate;



    public FeedData(String title, String link, String description, String pubDate, String feedLink, String category, boolean saved) {
        this.title = title;
        this.link = link;
        this.description = description;
        this.pubDate = pubDate;
        this.feedLink = feedLink;
        this.category = category;
        this.saved = saved;
    }

    public FeedData() {
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getPubDate() {
        return pubDate;
    }

    public void setPubDate(String pubDate) {
        this.pubDate = pubDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFeedLink() {
        return feedLink;
    }

    public void setFeedLink(String feedLink) {
        this.feedLink = feedLink;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isSaved() {
        return saved;
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
    }

    @Override
    public String toString() {
        return "Title: " + getTitle() + "\n" +
                "Link: " + getLink() + "\n" +
                "Description: " + getDescription() + "\n" +
                "Publication Date: " + getPubDate() + "\n" +
                "Feed Link: " + getFeedLink() + "\n" +
                "Category: " + getCategory() + "\n" +
                "Saved: " + isSaved();

    }
}
