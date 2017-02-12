package com.example.android.popularmoviesapp;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.example.android.popularmoviesapp.data.MovieContract;
import com.squareup.picasso.Picasso;

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

/**
 * Created by katsiarynamashokha on 1/21/17.
 */

public class MovieDetailFragment extends Fragment {
    public static final String LOG_TAG = MovieDetailFragment.class.getSimpleName();
    public static final String MOVIE_KEY = "movies";
    public static final String REVIEW_KEY = "reviews";
    private Uri mYoutubeUri;
    ToggleButton mButton;
    private Movie movieInfo;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle SavedInstanceState) {

        final View rootView = inflater.inflate(R.layout.detail_movie_activity, container, false);
        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(MOVIE_KEY)) {
            movieInfo = (Movie) intent.getSerializableExtra(MOVIE_KEY);

            ((TextView) rootView.findViewById(R.id.movie_title)).setText(movieInfo.getTitle());
            ((TextView) rootView.findViewById(R.id.actual_release_date))
                    .setText(movieInfo.getReleaseDate());
            ((TextView) rootView.findViewById(R.id.actual_movie_rating))
                    .setText(Double.toString(movieInfo.getVoteAverage()));
            ((TextView) rootView.findViewById(R.id.movie_description)).setText(movieInfo.getOverview());

            String posterPath = movieInfo.getImageUrl();

            Log.i(LOG_TAG, "The image path is: " + posterPath);
            Picasso.with(getActivity()).load(posterPath).into((ImageView) rootView
                    .findViewById(R.id.movie_poster));

            String trailerPath = movieInfo.getTrailerPath();

            GetMovieDetailsTask movieDetailsTask = new GetMovieDetailsTask();

            movieDetailsTask.execute(trailerPath);

            Button reviewButton = (Button) rootView.findViewById(R.id.review_button);
            reviewButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(getActivity(), ReviewsActivity.class);
                    intent.putExtra(REVIEW_KEY, movieInfo.getReviewPath());

                    startActivity(intent);
                }
            });
            ImageView playImage = (ImageView) rootView.findViewById(R.id.play_icon);
            playImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(mYoutubeUri);
                    startActivity(intent);
                    Log.i(LOG_TAG, "Youtube Uri is " + mYoutubeUri);
                }
            });
            mButton = (ToggleButton) rootView.findViewById(R.id.favorite_button);
            Uri movieUri = MovieContract.MovieEntry.buildMovieUri(movieInfo.getMovieId());
            Cursor cursor = getContext().getContentResolver().query(movieUri, null, null, null, null);
            Log.v(LOG_TAG, "cursor " + cursor.getCount());
            if (cursor.getCount() == 1) {
                mButton.setChecked(true);
            }
            mButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        ContentValues values = new ContentValues();
                        values.put(MovieContract.MovieEntry.MOVIE_ID, movieInfo.getMovieId());
                        values.put(MovieContract.MovieEntry.COLUMN_MOVIE_TITLE, movieInfo.getTitle());
                        values.put(MovieContract.MovieEntry.COLUMN_RATING, movieInfo.getVoteAverage());
                        values.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, movieInfo.getReleaseDate());
                        values.put(MovieContract.MovieEntry.COLUMN_DESCRIPTION, movieInfo.getOverview());
                        values.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH, movieInfo.getImageUrl());
                        values.put(MovieContract.MovieEntry.COLUMN_TRAILER_PATH, movieInfo.getTrailerPath());
                        values.put(MovieContract.MovieEntry.COLUMN_REVIEW_PATH, movieInfo.getReviewPath());
                        Uri insertUri = getContext().getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI, values);
                        Log.v(LOG_TAG, "Uri for insert " + insertUri);
                    }
                    else {
                        int rowsDeleted = getContext().getContentResolver().delete(
                                MovieContract.MovieEntry.buildMovieUri(movieInfo.getMovieId()), null, null);
                        Log.v(LOG_TAG, "number of rows deleted: " + rowsDeleted);
                    }
                }
            });
        }
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    private String getDataFromJson(String trailerJson) throws JSONException, MalformedURLException {
        final String MDB_RESULTS = "results";
        final String MDB_KEY = "key";

        String youtubeKey = "";

        JSONObject posterJsonObject = new JSONObject(trailerJson);
        JSONArray resultsArray = posterJsonObject.getJSONArray(MDB_RESULTS);
        JSONObject currentObject = resultsArray.getJSONObject(0);
        youtubeKey = currentObject.getString(MDB_KEY);
        return youtubeKey;

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(LOG_TAG, "onResume is called");
    }

    private class GetMovieDetailsTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            BufferedReader reader = null;
            HttpURLConnection urlConnection = null;
            String trailerJson = null;
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
                trailerJson = buffer.toString();
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
                return getDataFromJson(trailerJson);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            final String BASE_YOUTUBE_URL = "https://www.youtube.com/watch?";
            final String V_PARAMETER = "v";
            mYoutubeUri = Uri.parse(BASE_YOUTUBE_URL).buildUpon()
                    .appendQueryParameter(V_PARAMETER, s)
                    .build();
            movieInfo.setYoutubePath(mYoutubeUri.toString());
        }
    }
}
