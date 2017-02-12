package com.example.android.popularmoviesapp.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.example.android.popularmoviesapp.data.MovieContract.MovieEntry;

/**
 * Created by katsiarynamashokha on 1/22/17.
 */

public class MovieProvider extends ContentProvider {
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MovieDbHelper movieDbHelper;

    static final int MOVIES = 100;
    static final int MOVIE_WITH_ID = 101;

    private static final SQLiteQueryBuilder sMovieBuilder;
    static {
        sMovieBuilder = new SQLiteQueryBuilder();
        sMovieBuilder.setTables(MovieEntry.TABLE_NAME);
    }


    @Override
    public boolean onCreate() {
        movieDbHelper = new MovieDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            case MOVIES: {
                retCursor = movieDbHelper.getReadableDatabase().query(
                        MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
        }
                default:
                    throw new UnsupportedOperationException("Unknown uri: " + uri);

            }

    retCursor.setNotificationUri(getContext().getContentResolver(), uri);
    return retCursor;
    }


    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case MOVIES:
                return MovieEntry.CONTENT_TYPE;
            case MOVIE_WITH_ID:
                return MovieEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);

        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = movieDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case MOVIES: {
                long _id = db.insert(MovieEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = MovieEntry.buildMovieUri(_id);
                else
                    throw new android.database.SQLException("Failed to inset a row into " + uri);
                break;
                }
            default:
                throw new UnsupportedOperationException("Unknown Uri " + uri);
            }
        getContext().getContentResolver().notifyChange(uri, null);
        db.close();
        return returnUri;
        }


//        values = new ContentValues();
//
//        values.put(MovieEntry.COLUMN_MOVIE_TITLE, movie.getTitle());
//        values.put(MovieEntry.COLUMN_RELEASE_DATE, movie.getReleaseDate());
//        values.put(MovieEntry.COLUMN_RATING, movie.getVoteAverage());
//        values.put(MovieEntry.COLUMN_DESCRIPTION, movie.getOverview());
//        values.put(MovieEntry.COLUMN_POSTER_PATH, movie.getImageUrl());
//        values.put(MovieEntry.COLUMN_TRAILER_PATH, movie.getYoutubePath());
//        values.put(MovieEntry.COLUMN_REVIEW_PATH, movie.getReviewPath());
//
//        db.insert(MovieEntry.TABLE_NAME, null, values);
//        db.close();

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = movieDbHelper.getWritableDatabase();
        int rowsDeleted = db.delete(MovieEntry.TABLE_NAME, selection, selectionArgs);
        if (rowsDeleted != 0 ) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MovieContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, MovieContract.PATH_MOVIES, MOVIES);
        matcher.addURI(authority, MovieContract.PATH_MOVIES + "/#", MOVIE_WITH_ID);
        return matcher;
    }
}
