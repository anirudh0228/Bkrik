package com.starkcorp.bkrik;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

class customview extends RecyclerView.Adapter<customview.MyViewHolder> {
    Context context;
    private User user;
    private List<Items> details;
    private Items detail;
    StorageReference storageRef;
    private Task<byte[]> pathref;
    public customview(Context context, List<Items> details)
    {
        this.context=context;
        user=new User(context);
        storageRef= FirebaseStorage.getInstance().getReference();
        this.details=details;
    }


    private void setRatingStarColor(Drawable drawable, @ColorInt int color)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            DrawableCompat.setTint(drawable, color);
        }
        else
        {
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        }
    }

    private void setCurrentRating(RatingBar ratingbar,float rating) {
        Drawable drawable = (Drawable)ratingbar.getProgressDrawable();
        if(context!=null) {
            switch (Math.round(rating)) {
                case 1:
                    drawable.setColorFilter(context.getResources().getColor(R.color.red),PorterDuff.Mode.SRC_ATOP);
                    break;
                case 2:
                    drawable.setColorFilter(context.getResources().getColor(R.color.orange),PorterDuff.Mode.SRC_ATOP);
                    break;
                case 3:
                    drawable.setColorFilter(context.getResources().getColor(R.color.yellow),PorterDuff.Mode.SRC_ATOP);
                    break;
                case 4:
                    drawable.setColorFilter(context.getResources().getColor(R.color.light_green_yellow),PorterDuff.Mode.SRC_ATOP);
                    break;
                case 5:
                    drawable.setColorFilter(context.getResources().getColor(R.color.green),PorterDuff.Mode.SRC_ATOP);

                    break;
            }

        }
    }
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        private TextView mTextView;
        private TextView Price;
        private TextView ProductName;
        private TextView ShopName;
        private   ImageView ProductImage;
        private Button BuyButton;
        private RatingBar Ratings;
        private Button AddToCartButton;
        public MyViewHolder(View view) {
            super(view);
            ProductName=(TextView)view.findViewById(R.id.ProductName);
            ShopName=(TextView)view.findViewById(R.id.ShopName);
            ProductImage=(ImageView)view.findViewById(R.id.ProductImage);
            BuyButton=(Button) view.findViewById(R.id.imageButton3);
            Ratings=(RatingBar) view.findViewById(R.id.Rating);
            Price=(TextView)view.findViewById(R.id.Price);
            AddToCartButton=(Button)view.findViewById(R.id.imageButton4);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)


    // Create new views (invoked by the layout manager)
    @Override
    public customview.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.customview, parent, false);

        MyViewHolder vh = new MyViewHolder(v);
//        detail=details.get(vh.getAdapterPosition());
        return vh;
    }




    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        final MyViewHolder viewHolder=holder;
        detail=details.get(viewHolder.getAdapterPosition());
        //Log.i("aaaaa",Integer.toString(holder.getAdapterPosition()));
        View.OnClickListener mMyButtonClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detail=details.get(viewHolder.getAdapterPosition());
          //      Log.i("aaaaa",Integer.toString(holder.getAdapterPosition()));

                switch(v.getId()){

                    case R.id.imageButton3:
                        Intent myintent=new Intent(context, PurchaseMenu.class);
                        myintent.putExtra("shop", detail.getShop());
                        myintent.putExtra("product",detail.getProduct());
                        myintent.putExtra("price",detail.getPrice());
                        myintent.putExtra("image",detail.getImageurl());
                        myintent.putExtra("ratings",detail.getRatings());
                        myintent.putExtra("unit",detail.getUnit());
                        myintent.putExtra("imageurl",detail.getImageurl());
                        context.startActivity(myintent);
                        break;
                    case R.id.imageButton4:
                        user.AddtoCart(detail);
                        break;

                }

            }
        };
        holder.ProductName.setText(detail.getProduct());
        holder.ShopName.setText(detail.getShop());
        Drawable drawable=holder.Ratings.getProgressDrawable();
        drawable.setColorFilter(context.getResources().getColor(R.color.red),PorterDuff.Mode.SRC_ATOP);
        holder.Ratings.setNumStars((int)(Math.round(Double.parseDouble(detail.getRatings()))));
        holder.Price.setText(detail.getPrice()+" Rs");
        holder.AddToCartButton.setTag(Integer.valueOf(position));
        holder.BuyButton.setTag(Integer.valueOf(position));
        holder.AddToCartButton.setOnClickListener(mMyButtonClickListener);
        holder.BuyButton.setOnClickListener(mMyButtonClickListener);
        //holder.ProductImage.setImageResource(R.color.white);
       final Runnable runnable=new Runnable() {
            @Override
            public void run() {
                detail=details.get(viewHolder.getAdapterPosition());
                final long ONE_MEGABYTE = 1024 * 1024;
                StorageReference pathReference = storageRef.child(detail.getImageurl());
                Log.i("image",detail.getImageurl());
                    pathReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            // Data for "images/island.jpg" is returns, use this as needed
                            Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            viewHolder.ProductImage.setImageBitmap(bmp);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle any errors
                        }
                    });

            }
        };
        runnable.run();


    }

    @Override
    public int getItemCount() {
        return details.size();
    }

}
