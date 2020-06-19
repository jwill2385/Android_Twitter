package com.codepath.apps.restclienttemplate.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.List;

@Parcel
@Entity(foreignKeys = @ForeignKey(entity=User.class, parentColumns="id", childColumns="userId"))
public class Tweet {

    @PrimaryKey
    @ColumnInfo
    public long id;

    @ColumnInfo
    public String body;
    @ColumnInfo
    public String createdAt;
    @ColumnInfo
    public int numRetweets;
    @ColumnInfo
    public int numFavorites;
    @ColumnInfo
    public Boolean retweeted;
    @ColumnInfo
    public Boolean favorited;
    @ColumnInfo
    public String mediaURL;

    @ColumnInfo
    public long userId;

    @Ignore
    public User user;


    // empty constructor needed by Parceler Library
    public Tweet(){}

    //from our get tweet request assign local values for what we care about
    // When tweet was created, who was the user, what did it say ect.
    public static Tweet fromJson(JSONObject jsonObject) throws JSONException {
        Tweet tweet = new Tweet();
        tweet.body = jsonObject.getString("text");
        tweet.createdAt = jsonObject.getString("created_at");
        User user = User.fromJson(jsonObject.getJSONObject("user"));
        tweet.user = user;
        tweet.id = jsonObject.getLong("id");
        tweet.userId = user.id;
        tweet.numRetweets = jsonObject.getInt("retweet_count");
        tweet.numFavorites = jsonObject.getInt("favorite_count");
        tweet.retweeted = jsonObject.getBoolean("retweeted");
        tweet.favorited = jsonObject.getBoolean("favorited");
        JSONObject media = null;
        if(jsonObject.getJSONObject("entities").has("media")) {
          JSONArray mediaArray  = (jsonObject.getJSONObject("entities")).getJSONArray("media");
          //Get the media object from our JSON Array
          media = mediaArray.getJSONObject(0);
        }
        if(media != null){
            // this means we have an embedded image in tweet
            tweet.mediaURL = media.getString("media_url_https");
        } else {
            tweet.mediaURL = null;
        }
        return tweet;

    }

    public static List<Tweet> fromJsonArray(JSONArray jsonArray) throws JSONException {
        List<Tweet> tweets = new ArrayList<>();
        //For each tweet in the json array add to our list
        for (int i =0; i <jsonArray.length(); i++){
            tweets.add(fromJson(jsonArray.getJSONObject(i)));
        }
        return tweets;
    }
}
