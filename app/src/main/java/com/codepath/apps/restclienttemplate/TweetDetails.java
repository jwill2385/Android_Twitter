package com.codepath.apps.restclienttemplate;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.codepath.apps.restclienttemplate.databinding.ActivityTweetDetailsBinding;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.parceler.Parcels;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import okhttp3.Headers;

public class TweetDetails extends AppCompatActivity {

    public static final String TAG = "TweetDetailsActivity";

    //the specific tweet that I am displaying
    Tweet tweet;

    Context context;
    //get the client
    TwitterClient client;
    // Add all view objects
    TextView tvScreenName;
    TextView tvBody;
    TextView tvTimeStamp;
    TextView tvNumRetweets;
    TextView tvNumFavorites;
    ImageView ivProfilePic;
    ImageView ivRetweet;
    ImageView ivFavorite;
    ImageView ivReply;

    Boolean retweeted;
    Boolean favorited;
    int retweet_count;
    int favorite_count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Binding is used instead of setting content view with findViewById
        ActivityTweetDetailsBinding binding = ActivityTweetDetailsBinding.inflate(getLayoutInflater());
        // layout of activity is stored in a special property called root
        View view = binding.getRoot();
        setContentView(view);

        //initialize client
        client = TwitterApp.getRestClient(this);
        //set all the view objects in this layout view
        tvScreenName = findViewById(R.id.tvScreenName);
        tvBody = findViewById(R.id.tvBody);
        tvTimeStamp = findViewById(R.id.tvTimeStamp);
        tvNumRetweets = findViewById(R.id.tvNumRetweets);
        tvNumFavorites = findViewById(R.id.tvNumFavorites);
        ivProfilePic = findViewById(R.id.ivProfilePic);
        ivRetweet = findViewById(R.id.ivRetweet);
        ivFavorite = findViewById(R.id.ivFavorite);
        ivReply = findViewById(R.id.ivReply);




        // unwrap Tweet passed in via intent. using simple name as the key
        tweet = Parcels.unwrap(getIntent().getParcelableExtra(Tweet.class.getSimpleName()));
        Log.d(TAG, String.format("Showing details for '%s' ", tweet.user));

        //set all the info for detailed view
        tvScreenName.setText(tweet.user.screenName);
        tvNumRetweets.setText(String.valueOf(tweet.numRetweets));
        tvBody.setText(tweet.body);
        tvTimeStamp.setText(getRelativeTimeAgo(tweet.createdAt));
        tvNumFavorites.setText(String.valueOf(tweet.numFavorites));

        //set retweeted value
        retweeted = tweet.retweeted;
        retweet_count = tweet.numRetweets;
        //set favorited value
        favorited = tweet.favorited;
        favorite_count = tweet.numFavorites;

        // if retweeted is true already set retweet color icon
        if(retweeted){
            ivRetweet.setColorFilter(getResources().getColor(R.color.blue));
        }
        // if favorited is true already set favorite color icon
        if(favorited){
            ivFavorite.setColorFilter(getResources().getColor(R.color.colorRed));
        }

        //set pictures
        Glide.with(this).load(tweet.user.profileImageUrl).transform(new RoundedCorners(25)).into(ivProfilePic);

        ivRetweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Send a retweet
                if(!retweeted){
                    ivRetweet.setColorFilter(getResources().getColor(R.color.blue));
                    client.retweetTweet(tweet.id, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            Log.i(TAG, "OnSuccess to retweet");
                            retweeted = true;
                            //also increase tweet count by 1 since we haven't requested updated tweet from twitter
                            retweet_count += 1;
                            tvNumRetweets.setText(String.valueOf(retweet_count));

                        }

                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            Log.e(TAG, "onFailure to retweet tweet", throwable );
                        }
                    });
                }
                else{
                    // you already have retweeted this
                    Log.i(TAG, "you've already retweeted");
                }


            }
        });
        ivRetweet.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                //send an unRetweet
                if(retweeted) {
                    ivRetweet.setColorFilter(getResources().getColor(R.color.black));
                    client.unretweetTweet(tweet.id, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            Log.i(TAG, "OnSuccess to unretweet");
                            retweeted = false;
                            // Also reset num of retweets
                            retweet_count -= 1;
                            tvNumRetweets.setText(String.valueOf(retweet_count));

                        }

                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            Log.i(TAG, "onFailure to unretweet");
                        }
                    });
                } else{
                    // You haven't retweeted
                    Log.i(TAG, "You haven't retweeted this");
                }
                return true;
            }
        });

        //set listeners to favorite and unfavorite tweets
        ivFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Send a favorite
                if(!favorited) {
                    ivFavorite.setColorFilter(getResources().getColor(R.color.colorRed));
                    client.favoriteTweet(tweet.id, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            Log.i(TAG, "OnSuccess to Favorite");
                            favorited = true;
                            // increment favorite count
                            favorite_count += 1;
                            tvNumFavorites.setText(String.valueOf(favorite_count));
                        }

                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            Log.e(TAG, "onFailure to favorite tweet", throwable );
                        }
                    });

                }
            }
        });
        ivFavorite.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                //send an unFavorite
                if(favorited) {
                    ivFavorite.setColorFilter(getResources().getColor(R.color.black));
                    client.unfavoriteTweet(tweet.id, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            Log.i(TAG, "OnSuccess to unFavorite");
                            favorited = false;
                            //decrement favorite count
                            favorite_count -= 1;
                            tvNumFavorites.setText(String.valueOf(favorite_count));
                        }

                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            Log.e(TAG, "onFailure to unFavorite tweet", throwable );
                        }
                    });
                } else {
                    Log.i(TAG, "You haven't favorited");
                }
                return true;
            }
        });

        //if you click on reply go to compose
        ivReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TweetDetails.this, ComposeActivity.class);
                Log.i(TAG, "Pressed Reply");
                //send the tweet i'm replying to
                intent.putExtra(Tweet.class.getName(), Parcels.wrap(tweet));
                startActivity(intent);

            }
        });

        tvScreenName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(TweetDetails.this, ProfilePage.class);
                i.putExtra("user", Parcels.wrap(tweet.user));
                startActivity(i);
            }
        });
    }

    // getRelativeTimeAgo("Mon Apr 01 21:16:23 +0000 2014");
    public String getRelativeTimeAgo(String rawJsonDate) {
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);

        String relativeDate = "";
        try {
            long dateMillis = sf.parse(rawJsonDate).getTime();
            relativeDate = DateUtils.getRelativeTimeSpanString(dateMillis,
                    System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE).toString();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return relativeDate;
    }
}
