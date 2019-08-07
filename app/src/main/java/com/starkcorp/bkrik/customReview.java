package com.starkcorp.bkrik;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

/**
 * Created by anirudh on 6/10/2017.
 */
class customReview extends ArrayAdapter<Reviews> {
    Context context;
    private Reviews detail;
    public customReview(Context context, List<Reviews> details)
    {
        super(context,R.layout.customreviews,details);
        this.context=context;


    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater= LayoutInflater.from(getContext());

        View view=convertView;
        detail=getItem(position);
        if(view==null){
            view=inflater.inflate(R.layout.customreviews,parent,false);
            ViewHolder holder = createViewHolderFrom(view,detail);
            view.setTag(holder);

        }

        ViewHolder holder= (ViewHolder) view.getTag();

        holder.UserName.setText(detail.getUserName());
        holder.UserReview.setText(detail.getReview());
        Drawable drawable=holder.Ratings.getProgressDrawable();
        drawable.setColorFilter(context.getResources().getColor(R.color.red), PorterDuff.Mode.SRC_ATOP);
        holder.Ratings.setNumStars((int)(Math.round(Double.parseDouble(detail.getRatings()))));
        return view;
    }



    private ViewHolder createViewHolderFrom(View view,Reviews review) {
        RatingBar Ratings=(RatingBar) view.findViewById(R.id.RatingsByUser);
        TextView userName=(TextView)view.findViewById(R.id.userName2);
        TextView UserReview=(TextView)view.findViewById(R.id.ReviewByUser);
        return new ViewHolder(UserReview, userName,Ratings,review);
    }

    private static class ViewHolder {
        final TextView UserReview;
        final TextView UserName;
        final RatingBar Ratings;
        final Reviews review;

        public ViewHolder(TextView userReview, TextView userName, RatingBar ratings, Reviews review) {
            UserReview = userReview;
            UserName = userName;
            Ratings = ratings;
            this.review = review;
        }
    }
}

