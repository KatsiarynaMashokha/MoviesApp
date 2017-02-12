package com.example.android.popularmoviesapp;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by katsiarynamashokha on 1/30/17.
 */

public class MovieAdapter extends CursorAdapter {

    public static class ViewHolder {
        public final ImageView moviePoster;
        public final TextView movieTitle;
        public final TextView releaseDate;
        public final TextView movieRating;
        public final TextView movieOverview;

        public ViewHolder(View view) {
            moviePoster = (ImageView) view.findViewById(R.id.movie_poster);
            movieTitle = (TextView) view.findViewById(R.id.movie_title);
            releaseDate = (TextView) view.findViewById(R.id.actual_release_date);
            movieRating = (TextView) view.findViewById(R.id.actual_movie_rating);
            movieOverview = (TextView) view.findViewById(R.id.movie_description);

        }
    }

    public MovieAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
        }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return null;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

    }
}
