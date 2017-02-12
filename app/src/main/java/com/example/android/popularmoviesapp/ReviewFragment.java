package com.example.android.popularmoviesapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by katsiarynamashokha on 1/22/17.
 */

public class ReviewFragment extends Fragment {

    public static final String LOG_TAG = ReviewsActivity.class.getSimpleName();
    public static final String REVIEW_KEY = "reviews";
    ArrayAdapter mAdapter;
    private ArrayList<String> mReviews;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Intent intent = getActivity().getIntent();
        String reviewPath = (String) intent.getSerializableExtra(REVIEW_KEY);

        GetMovieReviewTask reviewTask = new GetMovieReviewTask();
        reviewTask.execute(reviewPath);

        ArrayList<String> arrayList = new ArrayList<>();
        View view = inflater.inflate(R.layout.activity_reviews, container, false);
        ListView listView = (ListView) view.findViewById(R.id.activity_reviews);
        mAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, arrayList);
        listView.setAdapter(mAdapter);
        return view;
    }

    private ArrayList<String> getReviewDataFromJson(String reviewJson) throws JSONException, MalformedURLException {
        mReviews = new ArrayList<>();
        final String MDB_RESULTS = "results";
        final String MDB_CONTENT = "content";

        String currentReview = "";

        JSONObject posterJsonObject = new JSONObject(reviewJson);
        JSONArray resultsArray = posterJsonObject.getJSONArray(MDB_RESULTS);
        for (int i = 0; i < resultsArray.length(); i++) {
            JSONObject currentObject = resultsArray.getJSONObject(i);
            currentReview = currentObject.getString(MDB_CONTENT);
            mReviews.add(currentReview);
        }
        return mReviews;
    }

    private class GetMovieReviewTask extends AsyncTask<String, Void, ArrayList<String>> {
        @Override
        protected ArrayList<String> doInBackground(String... params) {
            BufferedReader reader = null;
            HttpURLConnection urlConnection = null;
            String reviewJson = null;
            final String urlString = params[0];
            try {
                URL url = new URL(urlString);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }
                if (buffer.length() == 0) {
                    return null;
                }
                reviewJson = buffer.toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }

                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.e(LOG_TAG, "Error closing stream " + e);
                    }
                }
            }
            try {
                Log.i(LOG_TAG, "The review JSON: " + reviewJson);
                return getReviewDataFromJson(reviewJson);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            return null;
        }


        @Override
        protected void onPostExecute(ArrayList<String> strings) {
            super.onPostExecute(strings);
            mAdapter.clear();
            mAdapter.addAll(strings);
            mAdapter.notifyDataSetChanged();
        }
    }
}



