package com.codepath.apps.restclienttemplate;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.models.User;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;

public class ProfilePage extends AppCompatActivity {

    public static final String TAG = "ProfilePage";
    ImageView ivProfilePic;
    TextView tvScreenName;
    TextView tvFollowersTitle;
    TextView tvFollowingTitle;
    TextView tvFollowers;
    TextView tvFollowing;
    User user;
    List<User> followers;
    List<User> following;
    TwitterClient client;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        followers = new ArrayList<>();
        following = new ArrayList<>();

        client = TwitterApp.getRestClient(this);

        // set all views
        ivProfilePic = findViewById(R.id.ivProfilePic);
        tvScreenName = findViewById(R.id.tvScreenName);
        tvFollowersTitle = findViewById(R.id.tvFolowersTitle);
        tvFollowingTitle = findViewById(R.id.tvFollowingTitle);
        tvFollowers = findViewById(R.id.tvFollowers);
        tvFollowing = findViewById(R.id.tvFollowing);

        user = Parcels.unwrap(getIntent().getParcelableExtra("user"));
        Log.i(TAG, "Showing profile page for " + user.screenName);

        //now I can set details
        Glide.with(this).load(user.profileImageUrl).circleCrop().into(ivProfilePic);
        tvScreenName.setText(user.screenName);

        //get followers
        client.getFollowers(user.id, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "onSuccess for getting followers");
                JSONObject jsonObject = json.jsonObject;
                try {
                    JSONArray jsonArray = jsonObject.getJSONArray("users");
                    Log.i(TAG, "got the users");
                    // add all users to our followers list
                    for (int i = 0; i < jsonArray.length(); i++) {
                        followers.add(User.fromJson(jsonArray.getJSONObject(i)));
                    }
                    //Create a builder to concatenate usernames of each user
                    StringBuilder builder = new StringBuilder();
                    for (int j = 0; j < followers.size(); j++) {
                        builder.append(followers.get(j).screenName + "\n");
                    }
                    //now set this array to tvFollowers to display names
                    tvFollowers.setText(builder);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "failed to get Followers", throwable);
            }
        });

        //get following
        client.getFollowing(user.id, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "onSuccess for getting following");
                JSONObject jsonObject = json.jsonObject;
                try {
                    JSONArray jsonArray = jsonObject.getJSONArray("users");
                    Log.i(TAG, "got the users");
                    // add all users to our following list
                    for (int i = 0; i < jsonArray.length(); i++) {
                        following.add(User.fromJson(jsonArray.getJSONObject(i)));
                    }
                    //Create a builder to concatenate usernames of each user
                    StringBuilder builder = new StringBuilder();
                    for (int j = 0; j < following.size(); j++) {
                        builder.append(following.get(j).screenName + "\n");
                    }
                    //now set this array to tvFollowers to display names
                    tvFollowing.setText(builder);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "Failed to get Following", throwable);
            }
        });


    }
}
