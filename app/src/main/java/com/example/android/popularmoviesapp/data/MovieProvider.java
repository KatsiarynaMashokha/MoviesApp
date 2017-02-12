package com.example.android.popularmoviesapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import com.example.android.popularmoviesapp.data.MovieContract.MovieEntry;

/**
 * Created by katsiarynamashokha on 1/22/17.
 */

public class MovieProvider extends ContentProvider {
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MovieDbHelper movieDbHelper;
    private static final String LOG_TAG = MovieProvider.class.getSimpleName();

    static final int MOVIES = 100;
    static final int MOVIE_WITH_ID = 101;

    private static final SQLiteQueryBuilder sMovieBuilder;

    static {
        sMovieBuilder = new SQLiteQueryBuilder();
        sMovieBuilder.setTables(MovieEntry.TABLE_NAME);
    }

    @Override
    public boolean onCreate() {
        Log.v(LOG_TAG, "MovieDbHelper is being created");
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
            case MOVIE_WITH_ID: {
                selection = MovieEntry.MOVIE_ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
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
                    returnUri = MovieEntry.buildMovieUri(values.getAsLong(MovieEntry.MOVIE_ID));
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

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int rowsDeleted;
        final SQLiteDatabase db = movieDbHelper.getWritableDatabase();
        switch (sUriMatcher.match(uri)) {
            case MOVIE_WITH_ID: {
                selection = MovieEntry.MOVIE_ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = db.delete(MovieEntry.TABLE_NAME, selection, selectionArgs);
                if (rowsDeleted != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsDeleted;
            }
            default:
                throw new UnsupportedOperationException("Not valid uri " + uri);
        }
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
