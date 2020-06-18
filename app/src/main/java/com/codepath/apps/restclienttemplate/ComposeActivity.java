package com.codepath.apps.restclienttemplate;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.codepath.apps.restclienttemplate.databinding.ActivityComposeBinding;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONException;
import org.parceler.Parcels;

import okhttp3.Headers;

public class ComposeActivity extends AppCompatActivity {

    public static final String TAG = "ComposeActivity";
    EditText etCompose;
    Button btnTweet;

    TwitterClient client;
    long status_id;
    Tweet replyTweet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Binding is used instead of setting content view with findViewById
        ActivityComposeBinding binding = ActivityComposeBinding.inflate(getLayoutInflater());
        // layout of activity is stored in a special property called root
        View view = binding.getRoot();
        setContentView(view);

        client = TwitterApp.getRestClient(this);
        // set variables
        etCompose = findViewById(R.id.etCompose);
        btnTweet = findViewById(R.id.btnTweet);
        status_id = -1;

        // check if we are getting a reply tweet
        if(getIntent().hasExtra(Tweet.class.getName())){
            //We have to unwrap the parcel to make it a Tweet object
            replyTweet = Parcels.unwrap(getIntent().getParcelableExtra(Tweet.class.getName()));
            //pre set edit text to @ user
            etCompose.setText("@" + replyTweet.user.screenName);
            Log.i(TAG, "here");
        }

        //Set click listener on Button
        btnTweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String tweetContent = etCompose.getText().toString();
                if(tweetContent.isEmpty()){
                    //we dont want to return an empty string
                    Toast.makeText(ComposeActivity.this, "Type some text", Toast.LENGTH_SHORT).show();
                    return;
                }

                //check if tweet is in reply
                if(replyTweet != null){
                    status_id = replyTweet.id;
                }
                //otherwise send the tweet to Twitter to publish
                Toast.makeText(ComposeActivity.this, "Tweet sent", Toast.LENGTH_SHORT).show();
                client.publishTweet(tweetContent, status_id,  new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        Log.i(TAG, "onSuccess to publish tweet");
                        try {
                            Tweet tweet = Tweet.fromJson(json.jsonObject);
                            Log.i(TAG, "Published tweet says: " + tweet);
                            //Create Intent to send data back to Timeline
                            Intent intent = new Intent();
                            intent.putExtra("tweet", Parcels.wrap(tweet));
                            //set result code and bundle data for response
                            setResult(RESULT_OK, intent);
                            // close the activity and pass data to parent
                            finish();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                        Log.e(TAG, "onFailue to publish tweet", throwable );

                    }
                });
            }
        });
    }
}
