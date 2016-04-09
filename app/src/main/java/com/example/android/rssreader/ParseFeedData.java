package com.example.android.rssreader;

import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.StringReader;
import java.util.ArrayList;

import io.realm.Realm;

/**
 * Created by Administrator on 4/1/2016.
 */
public class ParseFeedData {
    private String xmlData;
    private String feedLink;
    private String feedCategory;
    private ArrayList<FeedData> feedDatas;


    public ParseFeedData(String xmlData) {
        this.xmlData = xmlData;
        feedDatas = new ArrayList<FeedData>();
    }

    public ParseFeedData(String xmlData, String feedLink, String feedCategory) {
        this.xmlData = xmlData;
        this.feedLink = feedLink;
        this.feedCategory = feedCategory;
        feedDatas = new ArrayList<FeedData>();
    }

    public ArrayList<FeedData> getFeedDatas() {
        return feedDatas;
    }

    public boolean process() {
        boolean status = true;
        FeedData currentRecord = null;
        boolean inItem = false;
        String textValue = "";
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        FeedData test = realm.createObject(FeedData.class);
        FeedList person = realm.createObject(FeedList.class);
        realm.commitTransaction();

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new StringReader(this.xmlData));
            int eventType = xpp.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tagName = xpp.getName();
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        // Log.d("ParseFeedData", "Starting tag for " + tagName);
                        if (tagName.equalsIgnoreCase("item")) {
                            inItem = true;
                            currentRecord = new FeedData();
                            if (this.feedLink != null) {
                                currentRecord.setCategory(this.feedCategory);
                                currentRecord.setFeedLink(this.feedLink);
                            }
                            realm.beginTransaction();
                            break;
                        }
                    case XmlPullParser.TEXT:
                        textValue = xpp.getText();
                        break;

                    case XmlPullParser.END_TAG:
                        //       Log.d("ParseFeedData", "Ending tag for " + tagName);
                        if (inItem) {
                            if (tagName.equalsIgnoreCase("item")) {
                                feedDatas.add(currentRecord);
                                person.getFeedLists().add(test);
                                realm.commitTransaction();
                                inItem = false;

                            } else if (tagName.equalsIgnoreCase("title")) {
                                currentRecord.setTitle(textValue);
                                test.setTitle(textValue);
                            } else if (tagName.equalsIgnoreCase("link")) {
                                currentRecord.setLink(textValue);
                                test.setLink(textValue);
                            } else if (tagName.equalsIgnoreCase("description")) {
                                currentRecord.setDescription(textValue);
                                test.setDescription(textValue);
                            } else if (tagName.equalsIgnoreCase("pubDate")) {
                                currentRecord.setPubDate(textValue);
                                test.setPubDate(textValue);
                            }
                        }

                        break;
                    default:
                        //

                }
                eventType = xpp.next();
            }

        } catch (Exception e) {
            status = false;
            e.printStackTrace();
        }

        for (FeedData app : feedDatas) {
            Log.d("ParseFeedData", "*************");
            Log.d("ParseFeedData", "Title: " + app.getTitle());
            Log.d("ParseFeedData", "Link: " + app.getLink());
            Log.d("ParseFeedData", "Description: " + app.getDescription());
            Log.d("ParseFeedData", "PubDate: " + app.getPubDate());
        }
        return true;
    }
}
