package com.starkcorp.bkrik;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.Rating;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.starkcorp.bkrik.Items;
import com.starkcorp.bkrik.R;
import com.starkcorp.bkrik.User;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class cartview extends RecyclerView.Adapter<cartview.MyViewHolder> {
    String username;
    Context context;
    private User user;
    private Items detail;
    private String UserLocation;
    private LocationCustom location;
    private int positionG;
    private List<Items> details;
    private StorageReference storageRef;
   public static List<Items> details1;

    public cartview(Context context, List<Items> details)
    {
        this.context=context;
        user=new User(context);
        this.details=details;
        location=new LocationCustom(context);
        storageRef= FirebaseStorage.getInstance().getReference();
     details1=new ArrayList<>();


    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView ProductName;
        public TextView ShopName;
        public RatingBar Ratings;
        public TextView Price;
        public Button BuyButton;
        public ImageView ProductImage;
        public TextView Unit;
        public TextView Quantity;
        public Button AddQuantity;
        public Button Remove;
        public Button SubQuantity;
        public MyViewHolder(View view) {
            super(view);
             ProductName=(TextView)view.findViewById(R.id.ProductName);
             ShopName=(TextView)view.findViewById(R.id.ShopName);
             ProductImage=(ImageView)view.findViewById(R.id.ProductImage);
             BuyButton=(Button) view.findViewById(R.id.BuyNow);
             Ratings=(RatingBar)view.findViewById(R.id.Rating);
             Price=(TextView)view.findViewById(R.id.Price);
             Unit=(TextView)view.findViewById(R.id.Unit);
             Quantity=(TextView) view.findViewById(R.id.Quantity);
             AddQuantity=(Button) view.findViewById(R.id.add);
             Remove=(Button)view.findViewById(R.id.remove);
             SubQuantity=(Button)view.findViewById(R.id.sub);

        }
    }
    @Override
    public cartview.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                      int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cartview, parent, false);

        MyViewHolder vh = new MyViewHolder(v);
//        detail=details.get(vh.getAdapterPosition());
        return vh;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        detail=details.get(holder.getAdapterPosition());

        holder.ProductName.setText(detail.getProduct());
        holder.ShopName.setText(detail.getShop());
        Drawable drawable=holder.Ratings.getProgressDrawable();
        drawable.setColorFilter(context.getResources().getColor(R.color.red), PorterDuff.Mode.SRC_ATOP);
        holder.Ratings.setNumStars((int)(Math.round(Double.parseDouble(detail.getRatings()))));
        holder.Price.setText(detail.getPrice()+" Rs");  //error opposite values price and ratings
        holder.Unit.setText(detail.getUnit());
        holder.Remove.setTag(Integer.valueOf(position));
        holder.Remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                detail=details.get(holder.getAdapterPosition());
                user.removeFromCart(detail);
            }
        });
        holder.AddQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                detail=details.get(holder.getAdapterPosition());
                String prevquan=holder.Quantity.getText().toString();
                String newQuan=Integer.toString(Integer.parseInt(prevquan)+1);
                detail.setQuantity(newQuan);
                holder.Quantity.setText(newQuan);
                int i=0;
                if(details1.size()!=0){
                    for(Items item:details1) {
                        if (item.getProduct().equals(detail.getProduct()) && item.getShop().equals(detail.getShop())) {
                            item.setQuantity(newQuan);
                            break;
                        }
                        i++;
                        Log.i("aaaa", i + " " + details1.size());
                        if (i == details1.size()) {
                            details1.add(detail);
                            break;
                        }
                    }
                }else{
                    details1.add(detail);
                }

                holder.Quantity.setEnabled(false);
                TextView txtview=((Activity)context).findViewById(R.id.totalprice);
                int price=Integer.parseInt(txtview.getText().toString().split(" ")[0]);
                txtview.setText(Integer.toString(price + Integer.parseInt(detail.getPrice())) + " Rs");

            }
        });
        holder.SubQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                detail=details.get(holder.getAdapterPosition());
                String prevquan=holder.Quantity.getText().toString();
                if(!prevquan.equals("0")){
                    String newQuan = Integer.toString(Integer.parseInt(prevquan) - 1);
                    detail.setQuantity(newQuan);
                    holder.Quantity.setText(newQuan);
                    TextView txtview = (TextView) ((Activity) context).findViewById(R.id.totalprice);

                    for(Items item:details1){
                        if(item.getProduct().equals(detail.getProduct())&&item.getProduct().equals(detail.getShop())){
                            item.setQuantity(newQuan);
                            break;
                        }

                    }
                    if(Integer.parseInt(newQuan)==0){
                        details1.remove(detail);
                    }
                    int price = Integer.parseInt(txtview.getText().toString().split(" ")[0]);
                    txtview.setText(Integer.toString(price -  Integer.parseInt(detail.getPrice())) + " Rs");
                }else{
                    Toast.makeText(context,"Quantity in Zero",Toast.LENGTH_LONG).show();
                }
            }
        });

        Runnable runnable=new Runnable() {
            @Override
            public void run() {
                detail=details.get(holder.getAdapterPosition());
                final long ONE_MEGABYTE = 1024 * 1024;
                StorageReference pathReference = storageRef.child(detail.getImageurl());
                pathReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        // Data for "images/island.jpg" is returns, use this as needed
                        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        holder.ProductImage.setImageBitmap(bmp);
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

        //Bitmap bmp= BitmapFactory.decodeFile(detail.get_ImageURI()); To do
        //ProductImage.setImageBitmap(bmp); to do




        holder.BuyButton.setTag(Integer.valueOf(position));
        holder.BuyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detail=details.get(holder.getAdapterPosition());
                if(android.text.TextUtils.isDigitsOnly(holder.Quantity.getText())) {
                    user.Bought(detail,holder.Quantity.getText().toString());

                }}});

    }






    @Override
    public int getItemCount() {
        return details.size();
    }


   static List<Items> getDetails1() {
        return details1;
    }
}