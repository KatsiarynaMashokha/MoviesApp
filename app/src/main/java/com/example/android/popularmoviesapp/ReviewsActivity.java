package com.example.android.popularmoviesapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class ReviewsActivity extends AppCompatActivity {
    public static final String LOG_TAG = ReviewsActivity.class.getSimpleName();
    public static final String REVIEW_KEY = "reviews";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_review);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container_fragment, new ReviewFragment())
                    .commit();

        }
    }
}
