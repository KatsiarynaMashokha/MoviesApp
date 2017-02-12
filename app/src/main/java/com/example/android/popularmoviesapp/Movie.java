package com.example.android.popularmoviesapp;

import java.io.Serializable;

/**
 * Created by katsiarynamashokha on 9/17/16.
 */
public class Movie implements Serializable {
    private final long movieId;
    private final String imageUrl;
    private final String title;
    private final String overview;
    private double voteAverage;
    private final String releaseDate;
    private final String trailerPath;
    private final String reviewPath;
    private String youtubePath;


    public Movie(long movieId, String imageUrl, String title, String overview, double voteAverage,
                 String releaseDate, String trailerPath, String reviewPath) {
        this.movieId = movieId;
        this.imageUrl = imageUrl;
        this.title = title;
        this.overview = overview;
        this.voteAverage = voteAverage;
        this.releaseDate = releaseDate;
        this.trailerPath = trailerPath;
        this.reviewPath = reviewPath;

    }

    public long getMovieId() {
        return movieId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getOverview() {
        return overview;
    }

    public double getVoteAverage() {
        return voteAverage;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public String getTrailerPath() {
        return trailerPath;
    }

    public String getReviewPath() {
        return reviewPath;
    }

    public String getYoutubePath() {
        return youtubePath;
    }

    public void setYoutubePath(String youtubePath) {
        this.youtubePath = youtubePath;
    }
}


