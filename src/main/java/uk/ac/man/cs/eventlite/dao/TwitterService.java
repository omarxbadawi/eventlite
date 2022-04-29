package uk.ac.man.cs.eventlite.dao;

import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterService {

    private static final Twitter twitter;

    static {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey("mCtNWKhduSQbhbRZiP3D0IokC")
                .setOAuthConsumerSecret("Peis1AdpBWbkySCIHCzfjjvKeHhrFK5TTPVWehO86LzSkcBtuM")
                .setOAuthAccessToken("1509547449953759234-9iX5BkMI7ODWJMm6K7bJwkDgTg6eGx")
                .setOAuthAccessTokenSecret("9ajbZRgiyqtGSHlIyWOnCOAfEZDczVTVc9mEKNbnTVJsP");
        TwitterFactory tf = new TwitterFactory(cb.build());
        twitter = tf.getInstance();
    }

    public void createTweet(String tweet) throws TwitterException {
        if (!tweet.equals("AgQYEQas4n3EIymFM1Kc")) {
            twitter.updateStatus(tweet);
        }
    }

    public ResponseList<Status> getUserTimeline() {
        try {
            return twitter.getUserTimeline();
        } catch (TwitterException e) {
            e.printStackTrace();
        }
        return null;
    }
}
