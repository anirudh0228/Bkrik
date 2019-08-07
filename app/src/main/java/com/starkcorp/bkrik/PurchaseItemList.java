package com.starkcorp.bkrik;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.starkcorp.bkrik.Items;
import com.starkcorp.bkrik.R;
import com.starkcorp.bkrik.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class PurchaseItemList extends RecyclerView.Adapter<PurchaseItemList.MyViewHolder> {
    String username;
    Context context;
    private String shop;
    private User user;
    private String product;
    private Items detail;
    private String UserLocation;
    private LocationCustom location;
    private int positionG;
    private List<Items> details;
    private StorageReference storageRef;
    private String name;
    String uid;
    public PurchaseItemList(Context context, List<Items> details)
    {this.context=context;
        user=new User(context);
        this.details=details;
        location=new LocationCustom(context);
        storageRef= FirebaseStorage.getInstance().getReference();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference childref = FirebaseDatabase.getInstance().getReference().getDatabase().getReference().child("users").child(uid);
        final ValueEventListener eventListener = new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                name=(String)dataSnapshot.child("name").getValue();
                Log.i("aaaaaaaa",name);


            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        childref.addListenerForSingleValueEvent(eventListener);

    }
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView ProductName;
        public TextView ShopName;
        public TextView Price;
        public ImageView ProductImage;
        public TextView Unit;
        public TextView Quantity;
        private TextView Status;
        public Button RateAndReview;
        public MyViewHolder(View view) {
            super(view);
             ProductName=(TextView)view.findViewById(R.id.ProductName);
             ShopName=(TextView)view.findViewById(R.id.ShopName);
             ProductImage=(ImageView)view.findViewById(R.id.ProductImage);
             Price=(TextView)view.findViewById(R.id.Price);
             Unit=(TextView)view.findViewById(R.id.Unit);
             Quantity=(TextView)view.findViewById(R.id.Quantity);
             RateAndReview=(Button)view.findViewById(R.id.RateAndReview);
             Status=(TextView)view.findViewById(R.id.deliveryStatus);

        }
    }
    @Override
    public PurchaseItemList.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                    int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.purchaseitemlist, parent, false);

        MyViewHolder vh = new MyViewHolder(v);
//        detail=details.get(vh.getAdapterPosition());
        return vh;
    }
    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        detail = details.get(holder.getAdapterPosition());
        holder.ProductName.setText(detail.getProduct());
        holder.ShopName.setText(detail.getShop());
        holder.Price.setText(detail.getPrice()+" Rs");
        holder.Unit.setText(detail.getUnit());
        holder.Quantity.setText(detail.getQuantity());
        holder.RateAndReview.setVisibility(View.GONE);
        if(detail.getDeliveryStatus().equalsIgnoreCase("canceled")){
            holder.Status.setTextColor(context.getResources().getColor(R.color.red));
        }
        holder.Status.setText(detail.getDeliveryStatus());
        if(detail.getDeliveryStatus().equals("Delivered")||detail.getDeliveryStatus().equals("Canceled")){
            holder.RateAndReview.setVisibility(View.VISIBLE);
        }

        holder.RateAndReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                detail = details.get(holder.getAdapterPosition());
                final Dialog dialog=new Dialog(context);
                dialog.setContentView(R.layout.dialogforrating);
                dialog.setCancelable(true);
                dialog.show();
                Button submit=(Button)dialog.findViewById(R.id.submit);

                submit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        detail = details.get(holder.getAdapterPosition());
                        RatingBar ratingBar=(RatingBar)dialog.findViewById(R.id.Ratingbar);
                        String Ratings=Float.toString(ratingBar.getRating());
                        EditText reviewfield=(EditText)dialog.findViewById(R.id.review);
                        String reviewfromfield=reviewfield.getText().toString();
                        shop=detail.getShop();
                        product=detail.getProduct();
                        final Reviews review=new Reviews(name,reviewfromfield,Ratings);

                        final DatabaseReference mdatabase= FirebaseDatabase.getInstance().getReference();
                        DatabaseReference childref = mdatabase.getDatabase().getReference().child("reviews").child(shop).child(product);
                        final ValueEventListener eventListener = new ValueEventListener() {

                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Map<String, Object> childUpdates = new HashMap<>();
                                childUpdates.put("/reviews/"+shop+"/"+product+"/"+uid,review);
                                mdatabase.updateChildren(childUpdates).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(context,"Thanks for the Review",Toast.LENGTH_LONG).show();
                                        user.boughtToDone(detail);
                                        dialog.hide();
                                        new updateRatings().execute();
                                        Intent i=new Intent(context,BKrikCart.class);
                                        context.startActivity(i);



                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(context,"Review can't be uploaded",Toast.LENGTH_LONG).show();
                                    }
                                });

                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {}
                        };

                        childref.addListenerForSingleValueEvent(eventListener);


                    }
                });
            }
        });

        Runnable runnable=new Runnable() {
            @Override
            public void run() {
                detail = details.get(holder.getAdapterPosition());
                final long ONE_MEGABYTE = 1024 * 1024;
                StorageReference pathReference = storageRef.child(detail.getImageurl());
                pathReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Log.i("imagepur",detail.getImageurl());
                        // Data for "images/island.jpg" is returns, use this as needed
                        final Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
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


    }
    @Override
    public int getItemCount() {
        return details.size();
    }

    public class updateRatings extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            DatabaseReference refreviews=FirebaseDatabase.getInstance().getReference().child("reviews").child(detail.getShop()).child(detail.getProduct());
            final double[] ratings = new double[1];
            refreviews.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    double avgrating=0;
                    int i=0;
                    for(DataSnapshot ds:dataSnapshot.getChildren()){
                        double rating=Double.parseDouble((String)ds.child("ratings").getValue());
                        avgrating=avgrating+rating;
                        i++;
                    }
                    if(i!=0) {
                        avgrating = avgrating / i;
                    }
                    ratings[0] =avgrating;
                    Log.i("raingfs",Double.toString(ratings[0]));
                    Log.i("raingfss",Double.toString(avgrating));


                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            final DatabaseReference refitems=FirebaseDatabase.getInstance().getReference().child("items").child(detail.getProduct()).child(detail.getShop());
            refitems.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    HashMap<String, Object> result = new HashMap<>();
                    result.put("ratings",Double.toString(ratings[0]));
                    Log.i("raingfsss",Double.toString(ratings[0]));
                    refitems.updateChildren(result).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.i("ratings","ratings updated");
                        }
                    });

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            return null;
        }
    }

}