package com.example.android.popularmoviesapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by kmashokha on 9/19/16.
 */
public class MovieDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_activity_frame_layout);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new MovieDetailFragment())
                    .commit();

        }
    }
}


