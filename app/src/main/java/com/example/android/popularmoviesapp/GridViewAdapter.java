package com.example.android.popularmoviesapp;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by katsiarynamashokha on 9/17/16.
 */

public class GridViewAdapter extends ArrayAdapter<Movie> {
    Context context;
    int layoutResourceId;
    ArrayList<Movie> movies = null;

    public GridViewAdapter(Context c, int layResId, ArrayList<Movie> movies1) {
        super(c, layResId, movies1);
        context = c;
        layoutResourceId = layResId;
        movies = movies1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            convertView = inflater.inflate(layoutResourceId, parent, false);
            holder = new ViewHolder();
            holder.posterImageView = (ImageView) convertView.findViewById(R.id.poster_image_view);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Movie currentMovie = movies.get(position);
        String posterUri = currentMovie.getImageUrl();
        Picasso.with(context).load(posterUri).into(holder.posterImageView);
        return convertView;

    }

    public class ViewHolder {
        private ImageView posterImageView;
    }
}