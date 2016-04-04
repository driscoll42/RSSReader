package com.example.android.rssreader;

/**
 * Created by Administrator on 4/1/2016.
 */
public class Application {

    private String title;
    private String link;
    private String description;
    private String pubDate;


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


    @Override
    public String toString() {
        return "Title: " + getTitle() + "\n" +
                "Link: " + getLink() + "\n" +
                "Description: " + getDescription() + "\n" +
                "Publication Date: " + getPubDate();

    }
}
