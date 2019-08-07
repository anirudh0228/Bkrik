
package com.starkcorp.bkrik;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.NumberPicker;
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

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PurchaseMenu extends AppCompatActivity {
private ProgressDialog progressDialog;
    private DatabaseReference myRef;
    private  String uid;
    private Items item;
    private User user;
    private String shopname;
    private String productname;
    private String ratings;
    private String price;
    private String UserLocation;
    private String quantity;
    private LocationCustom location;
    private String unit;
    private String imageurl;
    private StorageReference storageRef;

    private TextView noReviewsField;

    private TextView quantityField;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase_menu);
        myRef = FirebaseDatabase.getInstance().getReference();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        location = new LocationCustom(this);
        storageRef= FirebaseStorage.getInstance().getReference();
        noReviewsField=(TextView)findViewById(R.id.NoReviews);
        noReviewsField.setVisibility(View.GONE);

        user = new User(this);


        TextView ProductName = (TextView) findViewById(R.id.PProductName);
        TextView Shopname = (TextView) findViewById(R.id.PSName);
        RatingBar Ratings = (RatingBar) findViewById(R.id.ratings2);
        Drawable drawable=Ratings.getProgressDrawable();
        drawable.setColorFilter(ResourcesCompat.getColor(getResources(),R.color.red,null), PorterDuff.Mode.SRC_ATOP);
        TextView Price = (TextView) findViewById(R.id.PPrice);
        TextView Unit = (TextView) findViewById(R.id.PQuantity);
        quantityField=(TextView)findViewById(R.id.quantity);

        Intent intent = getIntent();
        final ImageView image=(ImageView)findViewById(R.id.imageView2);

        shopname = intent.getStringExtra("shop");
        productname = intent.getStringExtra("product");
        ratings = intent.getStringExtra("ratings");
        price = intent.getStringExtra("price");
        unit = intent.getStringExtra("unit");
        imageurl=intent.getStringExtra("imageurl");

        Runnable runnable=new Runnable() {
            @Override
            public void run() {
                final long ONE_MEGABYTE = 1024 * 1024;
                StorageReference pathReference = storageRef.child(imageurl);
                pathReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        // Data for "images/island.jpg" is returns, use this as needed
                        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        image.setImageBitmap(bmp);
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

        //TextViews

        //setText
        ProductName.setText(productname);
        Shopname.setText(shopname);
        Price.setText(price+" Rs");
        Ratings.setNumStars((int)(Math.round(Double.parseDouble(ratings))));
        Unit.setText(unit);
       //loadReviews
        loadReviews();
        Button buynow = (Button) findViewById(R.id.imageButton);
        buynow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Items detail = new Items(productname, shopname, ratings, price, unit,imageurl);
                quantity=quantityField.getText().toString();
                if(quantity.equals("0")){
                    Toast.makeText(PurchaseMenu.this,"Quantity is zero",Toast.LENGTH_LONG).show();
                }else{
                    user.Bought(detail,quantity);

                }

            }
        });
    }


    //Reviews refresh
    public void refresh(){
        //progressDialog = ProgressDialog.show(PurchaseMenu.this,"Loading","Please Wait",false,false);
        DatabaseReference childref=myRef.child("reviews").child(shopname).child(productname);

        final ValueEventListener eventListener = new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.getValue()!=null){
                    List<Reviews> reviews=new ArrayList<>();
                    for(DataSnapshot ds:dataSnapshot.getChildren()){
                        //String details[]=extractDetails(ds.getValue().toString());
                        Log.i("TAG",ds.getValue().toString()+" "+ds.getKey());
                        String ratings=(String)ds.child("ratings").getValue();
                        String reviewvalue=(String)ds.child("review").getValue();
                        String userName=(String)ds.child("userName").getValue();
                        Reviews review=new Reviews(userName , reviewvalue, ratings);
                        reviews.add(review);

                    }


                    ListAdapter listAdapter=new customReview(PurchaseMenu.this,reviews);
                    ListView listview=(ListView)findViewById(R.id.reviewlist);
                    listview.setAdapter(listAdapter);
                    //progressDialog.dismiss();


                }else{
                    noReviewsField.setVisibility(View.VISIBLE);
                    //  progressDialog.dismiss();

                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        childref.addListenerForSingleValueEvent(eventListener);
    }
    public void loadReviews(){
        Runnable runnable=new Runnable() {
            @Override
            public void run() {
              refresh();
            }
        };
        runnable.run();
    }
    public void onSub(View v){

        String quanstr=quantityField.getText().toString();
        if(!quanstr.equals("0")) {
            int newQuan = Integer.parseInt(quanstr) - 1;
            quantityField.setText(Integer.toString(newQuan));

        }else{
            Toast.makeText(this,"Quantity already Zero",Toast.LENGTH_LONG).show();
        }
        }
    public void onAdd(View v){
        String quanstr=quantityField.getText().toString();
        int newQuan=Integer.parseInt(quanstr)+1;
        quantityField.setText(Integer.toString(newQuan));
    }



}
