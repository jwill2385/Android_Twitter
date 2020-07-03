package com.codepath.apps.restclienttemplate;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.codepath.apps.restclienttemplate.databinding.ActivityTimelineBinding;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.models.TweetDao;
import com.codepath.apps.restclienttemplate.models.TweetWithUser;
import com.codepath.apps.restclienttemplate.models.User;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;

public class TimelineActivity extends AppCompatActivity {

    public static final String TAG = "Timeline Activity";
    //send this code to compose activity to request result back
    private final int REQUEST_CODE = 20;
    TwitterClient client;
    RecyclerView rvTweets;
    List<Tweet> tweets;
    TweetsAdapter adapter;
    SwipeRefreshLayout swipeContainer;
    EndlessRecyclerViewScrollListener scrollListener;
    TweetDao tweetDao;
    MenuItem miActionProgressItem; // define progress bar

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityTimelineBinding binding = ActivityTimelineBinding.inflate(getLayoutInflater());
        // layout of activity is stored in a special property called root
        View view = binding.getRoot();
        setContentView(view);

        client = TwitterApp.getRestClient(this);
        tweetDao = ((TwitterApp) getApplicationContext()).getMyDatabase().tweetDao();


        swipeContainer = findViewById(R.id.swipeContainer);
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.i(TAG, "fetching new data!");
                populateHomeTimeline();
            }
        });

        //find the recycler view
        rvTweets = findViewById(R.id.rvTweets);
        //Init the list of tweets and adapter
        tweets = new ArrayList<>();
        adapter = new TweetsAdapter(this, tweets);
        LinearLayoutManager layoutManager =  new LinearLayoutManager(this);
        //Recycler view set up: layout manager and the adapter
        rvTweets.setLayoutManager(layoutManager);
        rvTweets.setAdapter(adapter);
        //must define scrollListener after you set layout manager
        scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                Log.i(TAG, "on load more " + page);
                loadMoreDate();
            }
        };
        // Adds the scroll listener to RecyclerView
        rvTweets.addOnScrollListener(scrollListener);

        // Query for existing tweets in the DB
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "Showing data from database");
                List<TweetWithUser> tweetWithUsers =  tweetDao.recentItems();
                List<Tweet> tweetsFromDB = TweetWithUser.getTweetList(tweetWithUsers);
                adapter.clear();
                adapter.addAll(tweetsFromDB);
            }
        });
        //populateHomeTimeline();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu: this dds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Store instance of the menu item containing progress
        miActionProgressItem = menu.findItem(R.id.miActionProgress);
        // now populate the timeline here since we have access to the progress bar.
        populateHomeTimeline();
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.compose){
            //This means we selected compose icon and should navigate to compose activity
            Intent i = new Intent(TimelineActivity.this, ComposeActivity.class);
            startActivityForResult(i, REQUEST_CODE);
            return true;
        }
        if(item.getItemId() == R.id.profile){
            //This means we selected profile icon
            Toast.makeText(this, "Profile Page", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    public void showProgressBar() {
        // Show progress item
        miActionProgressItem.setVisible(true);
    }

    public void hideProgressBar() {
        // Hide progress item
        miActionProgressItem.setVisible(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //check if requestCode sent back is same as one that was originally sent
        if(requestCode == REQUEST_CODE && resultCode == RESULT_OK ){
            //Get data from intent
            Tweet tweet =  Parcels.unwrap(data.getParcelableExtra("tweet"));
            //Update RecyclerView
            //Modify data source of tweets. position 0 because we want to add to top of RV
            tweets.add(0,tweet);
            //update adapter of changes
            adapter.notifyItemInserted(0);
            //Scroll up to top of Recycler view
            rvTweets.smoothScrollToPosition(0);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void loadMoreDate() {
        // Send an API request to retrieve appropriate paginated data
        client.getNextPageOfTweets(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "onSuccess for load more data" + json.toString());
                //  --> Send the request including an offset value (i.e `page`) as a query parameter.
                //  --> Deserialize and construct new model objects from the API response
                JSONArray jsonArray = json.jsonArray;
                try {
                    List<Tweet> tweets = Tweet.fromJsonArray(jsonArray);
                    //  --> Append the new data objects to the existing set of items inside the array of items
                    //  --> Notify the adapter of the new items made with `notifyItemRangeInserted()`
                    adapter.addAll(tweets);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "onFailure for load more data", throwable);
            }
        }, tweets.get(tweets.size() - 1).id);
    }

    private void populateHomeTimeline() {
        // show progress bar
        showProgressBar();
        client.getHomeTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "onSuccess" + json.toString());
                JSONArray jsonArray = json.jsonArray;
                try {
                    /*
                    tweets.addAll(Tweet.fromJsonArray(jsonArray));
                    //always notify adapter after you modify dataset
                    adapter.notifyDataSetChanged();
                    */
                    final List<Tweet> tweetsFromNetwork =  Tweet.fromJsonArray(jsonArray);
                    adapter.clear();
                    adapter.addAll(tweetsFromNetwork);
                    // Now we call setRefreshing(false) to signal refresh has finished
                    swipeContainer.setRefreshing(false);
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG, "Saving data into database");
                            // Insert Users first
                            List<User> usersFromNetwork = User.fromJsonTweetArray(tweetsFromNetwork);
                            tweetDao.insertModel(usersFromNetwork.toArray(new  User[0]));
                            // insert Tweets next
                            tweetDao.insertModel(tweetsFromNetwork.toArray(new Tweet[0]));

                        }
                    });
                    // remove progress bar
                    hideProgressBar();
                } catch (JSONException e) {
                   Log.e(TAG, "Json exception", e);
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "onFailure! : " + response, throwable);
            }
        });
    }
}
