package com.example.android.rssreader;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by Administrator on 4/6/2016.
 */
public class FeedList extends RealmObject {

    //TODO: Figure out why the hell this isn't working
    private RealmList<FeedData> FeedLists;
    private String FeedType;

    public RealmList<FeedData> getFeedLists() {
        return FeedLists;
    }

    public void setFeedLists(RealmList<FeedData> feedLists) {
        this.FeedLists = feedLists;
    }

    public String getFeedType() {
        return FeedType;
    }

    public void setFeedType(String feedType) {
        FeedType = feedType;
    }
}
