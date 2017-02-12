package com.example.android.popularmoviesapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.example.android.popularmoviesapp.data.MovieContract;
import com.example.android.popularmoviesapp.data.MovieProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


/**
 * Created by katsiarynamashokha on 9/14/16.
 */
public class PosterFragment extends Fragment {
    public static final String LOG_TAG = PosterFragment.class.getSimpleName();
    public static final String MOVIE_KEY = "movies";
    private static ArrayAdapter<Movie> mGridViewAdapter;
    private TextView emptyStateTextView;
    ArrayList<Movie> movies;
    GridView gridView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.poster_fragment_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            updateMovie();
            return true;
        }
        if (id == R.id.settings_favorite) {
            mGridViewAdapter.clear();
            movies = getFavoriteMovies();
            mGridViewAdapter.addAll(movies);
            mGridViewAdapter.notifyDataSetChanged();
        }
        return super.onOptionsItemSelected(item);
    }

    private ArrayList<Movie> getFavoriteMovies() {
        movies = new ArrayList<>();
        MovieProvider provider = new MovieProvider();
        Cursor cursor = provider.query(MovieContract.MovieEntry.CONTENT_URI, null, null, null, null);
        while (cursor.moveToNext()) {
            int indexId = cursor.getColumnIndex(MovieContract.MovieEntry._ID);
            int indexTitle = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_TITLE);
            int indexOverview = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_DESCRIPTION);
            int indexRating = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_RATING);
            int indexReleaseDate = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_RELEASE_DATE);
            int indexPoster = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_POSTER_PATH);
            int indexReview = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_REVIEW_PATH);
            int indexTrailer = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_TRAILER_PATH);


            long movieId = cursor.getLong(indexId);
            String movieTitle = cursor.getString(indexTitle);
            String movieOverview = cursor.getString(indexOverview);
            double movieRating = cursor.getDouble(indexRating);
            String movieReleaseDate = cursor.getString(indexReleaseDate);
            String posterPath = cursor.getString(indexPoster);
            String reviewPath = cursor.getString(indexReview);
            String trailerPath = cursor.getString(indexTrailer);

            movies.add(new Movie(movieId,
                    posterPath,
                    movieTitle,
                    movieOverview,
                    movieRating,
                    movieReleaseDate,
                    trailerPath,
                    reviewPath));
        }

        return movies;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(LOG_TAG, "onCreateview is called");
        mGridViewAdapter = new GridViewAdapter(getActivity(), R.layout.grid_view_poster_item, new ArrayList<Movie>());
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        GridView gridView = (GridView) rootView.findViewById(R.id.main_gridview);
        gridView.setAdapter(mGridViewAdapter);


        emptyStateTextView = (TextView) rootView.findViewById(R.id.empty_state_text_view);
        gridView.setEmptyView(emptyStateTextView);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Movie singleMovie = mGridViewAdapter.getItem(i);
                Intent intent = new Intent(getActivity(), MovieDetailActivity.class).
                        putExtra(MOVIE_KEY, singleMovie);
                startActivity(intent);
            }
        });

        return rootView;
    }


    @Override
    public void onStart() {
        super.onStart();
        Log.i(LOG_TAG, "onStart is called");
        if (isNetworkAvailable()) {
            updateMovie();
        } else {
            emptyStateTextView.setText(R.string.no_internet_message);
        }
    }

    private void updateMovie() {
        DownloadPosterTask posterTask = new DownloadPosterTask();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortingParameter = sharedPreferences.getString(getString(R.string.pref_sort_key),
                getString(R.string.pref_sort_default));
        posterTask.execute(sortingParameter);
    }

    private ArrayList<Movie> getDataFromJson(String posterJson) throws JSONException, MalformedURLException {

        ArrayList<Movie> movies = new ArrayList<>();

        final String MDB_RESULTS = "results";
        final String MDB_TITLE = "original_title";
        final String MDB_OVERVIEW = "overview";
        final String MDB_VOTE_AVERAGE = "vote_average";
        final String MDB_RELEASE_DATE = "release_date";
        final String MDB_POSTER_PATH = "poster_path";
        final String MDB_MOVIE_ID = "id";

        String posterPath = "";
        String originalTitle = "";
        String overview = "";
        double voteAverage;
        String releaseDate;
        int movieId;


        JSONObject posterJsonObject = new JSONObject(posterJson);
        JSONArray resultsArray = posterJsonObject.getJSONArray(MDB_RESULTS);


        for (int i = 0; i < resultsArray.length(); i++) {

            JSONObject currentMovieInfo = resultsArray.getJSONObject(i);

            posterPath = currentMovieInfo.getString(MDB_POSTER_PATH);
            originalTitle = currentMovieInfo.getString(MDB_TITLE);
            overview = currentMovieInfo.getString(MDB_OVERVIEW);
            voteAverage = currentMovieInfo.getDouble(MDB_VOTE_AVERAGE);
            releaseDate = currentMovieInfo.getString(MDB_RELEASE_DATE);
            movieId = currentMovieInfo.getInt(MDB_MOVIE_ID);

            String posterPathFinal = getUrlPosterPath(posterPath);
            String trailerPath = getTrailerPath(movieId);
            String reviewPath = getReviewsPath(movieId);
            Log.i(LOG_TAG, "The trailer path is: " + trailerPath);
            Log.i(LOG_TAG, "The review path is: " + reviewPath);

            Movie movie = new Movie(movieId, posterPathFinal, originalTitle, overview, voteAverage,
                    releaseDate, trailerPath, reviewPath);
            movies.add(movie);
        }

        return movies;
    }

    private String getUrlPosterPath(String singlePosterPath) throws MalformedURLException {
        final String BASE_URL = "http://image.tmdb.org/t/p/";
        final String SIZE = "w185/";
        StringBuilder stringBuilder = new StringBuilder(BASE_URL);
        stringBuilder.append(SIZE);
        stringBuilder.append(singlePosterPath);
        String posterStringUrl = stringBuilder.toString();
        return posterStringUrl;
    }
    private String getTrailerPath(double movieId) {
        final String BASE_URL = "http://api.themoviedb.org/3/movie/";
        final String VIDEOS_PARAMETER = "videos";
        final String API_KEY_PARAMETER = "api_key";
        Uri builtUri;
        builtUri = Uri.parse(BASE_URL).buildUpon()
                .appendPath(Double.toString(movieId))
                .appendPath(VIDEOS_PARAMETER)
                .appendQueryParameter(API_KEY_PARAMETER, BuildConfig.THE_MOVIE_DB_API_KEY)
                .build();
        String trailerPath = builtUri.toString();
        return trailerPath;
    }

    private String getReviewsPath(double movieId) {
        final String BASE_URL = "http://api.themoviedb.org/3/movie/";
        final String REVIEW_PARAMETER = "reviews";
        final String API_KEY_PARAMETER = "api_key";
        Uri builtUri;
        builtUri = Uri.parse(BASE_URL).buildUpon()
                .appendPath(Double.toString(movieId))
                .appendPath(REVIEW_PARAMETER)
                .appendQueryParameter(API_KEY_PARAMETER, BuildConfig.THE_MOVIE_DB_API_KEY)
                .build();
        String reviewPath = builtUri.toString();
        return reviewPath;
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public class DownloadPosterTask extends AsyncTask<String, Void, ArrayList<Movie>> {
        private final String LOG_TAG = DownloadPosterTask.class.getSimpleName();

        @Override
        protected ArrayList<Movie> doInBackground(String... params) {
            BufferedReader reader = null;
            HttpURLConnection urlConnection = null;
            String posterJson = null;


            try {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                String sortParameter = sharedPreferences.getString(getString(R.string.pref_sort_key),
                        getString(R.string.pref_sort_popular));


                final String POSTER_BASE_URL_POPULAR = "http://api.themoviedb.org/3/movie/popular?";
                final String POSTER_BASE_URL_RATING = "http://api.themoviedb.org/3/movie/top_rated?";
                final String API_KEY_PARAMETER = "api_key";
                Uri builtUri;

                if (sortParameter.equals(getString(R.string.pref_sort_rating))) {
                    builtUri = Uri.parse(POSTER_BASE_URL_RATING).buildUpon()
                            .appendQueryParameter(API_KEY_PARAMETER, BuildConfig.THE_MOVIE_DB_API_KEY)
                            .build();
                } else {
                    builtUri = Uri.parse(POSTER_BASE_URL_POPULAR).buildUpon()
                            .appendQueryParameter(API_KEY_PARAMETER, BuildConfig.THE_MOVIE_DB_API_KEY)
                            .build();
                }
                URL url = new URL(builtUri.toString());
                Log.v(LOG_TAG, "The Url is " + url);

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
                posterJson = buffer.toString();
                Log.v(LOG_TAG, "Poster Json string is " + posterJson);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error: " + e);
                return null;
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
                return getDataFromJson(posterJson);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            return null;

        }

        @Override
        protected void onPostExecute(ArrayList<Movie> movies) {
            if (movies != null) {
                mGridViewAdapter.clear();
            }
            for (Movie movie : movies) {
                mGridViewAdapter.add(movie);
            }
            super.onPostExecute(movies);
        }
    }

}
