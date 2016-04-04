package com.example.android.rssreader;

import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.StringReader;
import java.util.ArrayList;

/**
 * Created by Administrator on 4/1/2016.
 */
public class ParseApplications {
    private String xmlData;
    private ArrayList<Application> applications;

    public ParseApplications(String xmlData) {
        this.xmlData = xmlData;
        applications = new ArrayList<Application>();
    }

    public ArrayList<Application> getApplications() {
        return applications;
    }

    public boolean process() {
        boolean status = true;
        Application currentRecord = null;
        boolean inItem = false;
        String textValue = "";

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new StringReader(this.xmlData));
            int eventType = xpp.getEventType();

            while(eventType != XmlPullParser.END_DOCUMENT){
                String tagName = xpp.getName();
                switch(eventType) {
                    case XmlPullParser.START_TAG:
                       // Log.d("ParseApplications", "Starting tag for " + tagName);
                        if(tagName.equalsIgnoreCase("item")) {
                            inItem = true;
                            currentRecord = new Application();
                            break;
                        }
                    case XmlPullParser.TEXT:
                        textValue = xpp.getText();
                        break;

                    case XmlPullParser.END_TAG:
                 //       Log.d("ParseApplications", "Ending tag for " + tagName);
                        if(inItem) {
                            if(tagName.equalsIgnoreCase("item")) {
                                applications.add(currentRecord);
                                inItem = false;

                            } else if(tagName.equalsIgnoreCase("title")) {
                                currentRecord.setTitle(textValue);
                            } else if(tagName.equalsIgnoreCase("link")) {
                                currentRecord.setLink(textValue);
                            } else if(tagName.equalsIgnoreCase("description")) {
                                currentRecord.setDescription(textValue);
                            } else if(tagName.equalsIgnoreCase("pubDate")) {
                                currentRecord.setPubDate(textValue);
                            }
                        }
                        break;
                    default:
                        //

                }
                eventType = xpp.next();
            }

        } catch (Exception e){
            status = false;
            e.printStackTrace();
        }

        for(Application app : applications) {
            Log.d("ParseApplications", "*************");
            Log.d("ParseApplications", "Title: " + app.getTitle());
            Log.d("ParseApplications", "Link: " + app.getLink());
            Log.d("ParseApplications", "Description: " + app.getDescription());
            Log.d("ParseApplications", "PubDate: " + app.getPubDate());
        }
        return true;
    }
}
