package com.codepath.apps.restclienttemplate;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.codepath.apps.restclienttemplate.models.Tweet;

import org.parceler.Parcels;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class TweetsAdapter extends RecyclerView.Adapter<TweetsAdapter.ViewHolder> {

    Context context;
    List<Tweet> tweets;
    public static final String TAG = "TweetAdapter";
    //Pass in the context and list of tweets
    public TweetsAdapter(Context context, List<Tweet> tweets) {
        this.context = context;
        this.tweets = tweets;
    }

    //For each row, inflate the layout
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(context).inflate(R.layout.item_tweet, parent, false);
        return new ViewHolder(view);
    }

    //Bind values based on the position of the element
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        //Get the data at position
        Tweet tweet = tweets.get(position);
        //Bind the tweet with the view holder
        holder.bind(tweet);
    }

    @Override
    public int getItemCount() {
        return tweets.size();
    }

    // Clear all elements of recycler so we can fetch fresh data
    public void clear(){
        tweets.clear();
        notifyDataSetChanged();
    }

    // Add a list of new tweets
    public void addAll(List<Tweet> tweetList) {
        tweets.addAll(tweetList);
        notifyDataSetChanged();
    }



    //Define the viewholder who is showing the view
    public class ViewHolder extends RecyclerView.ViewHolder implements  View.OnClickListener{

        ImageView ivProfileImage;
        TextView tvBody;
        TextView tvScreenName;
        TextView tvTimeStamp;
        ImageView ivEmbeddedImage;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            tvBody = itemView.findViewById(R.id.tvBody);
            tvScreenName = itemView.findViewById(R.id.tvScreenName);
            tvTimeStamp = itemView.findViewById(R.id.tvTimeStamp);
            ivEmbeddedImage = itemView.findViewById(R.id.ivEmbeddedImage);
            // I need to set itemView's onClickListener so that we can see each item
            itemView.setOnClickListener(this);
        }

        // take each value from tweet and fill corresponding views
        public void bind(Tweet tweet) {
            tvBody.setText(tweet.body);
            tvScreenName.setText(tweet.user.screenName);
            tvTimeStamp.setText(getRelativeTimeAgo(tweet.createdAt));
            Glide.with(context).load(tweet.user.profileImageUrl).transform(new RoundedCorners(25)).into(ivProfileImage);
            // Check if there is any embedded media. If not don't show anything
            if(tweet.mediaURL != null) {
                ivEmbeddedImage.setVisibility(View.VISIBLE);
                Glide.with(context).load(tweet.mediaURL).into(ivEmbeddedImage);
            } else{
                //don't show at all
                ivEmbeddedImage.setVisibility(View.GONE);
            }
        }


        // When a user clicks on a tweet. Show details of each tweet
        @Override
        public void onClick(View view) {
            // get the position
            int position = getAdapterPosition();
            Log.i(TAG, "I'm at position " + position);
            // make sure this position actually exists in the view
            if(position != RecyclerView.NO_POSITION){
                // get the tweet at this position. This will not work if class is static
                Tweet tweet = tweets.get(position);
                // create intent to go to details page
                Intent intent = new Intent(context, TweetDetails.class);
                // serialize the individual tweet i want to send using Parcel
                intent.putExtra(Tweet.class.getSimpleName(), Parcels.wrap(tweet));
                //show the activity
                context.startActivity(intent);
            }
        }
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
