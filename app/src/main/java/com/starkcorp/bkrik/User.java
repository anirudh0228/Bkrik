package com.starkcorp.bkrik;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.Log;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.graphics.Typeface.BOLD;
public class User {
    public String name;
    public List<Items> bought;
    public String email;
    public List<Items> cartItems;
    String uid;
    DatabaseReference mdatabase;
    private Context context;
    private LocationCustom location;
    String UserLocation;
    private int count;
    private int countbought;
    private int Deliverycharges;
    private String PhoneNumber;
    private String password;
    private String cordinates;


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhoneNumber() {
        return PhoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        PhoneNumber = phoneNumber;
    }

    public User(Context context) {
        this.context=context;
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mdatabase= FirebaseDatabase.getInstance().getReference();
        cartItems=new ArrayList<>();
        bought=new ArrayList<>();
        location=new LocationCustom(context);
        DatabaseReference DeliveryChargeListener=mdatabase.getDatabase().getReference();
        final ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Log.i("aaaaaaaaaaaaaaaaaaa",dataSnapshot.getValue().toString());
                Deliverycharges =Integer.parseInt((String)dataSnapshot.child("DeliveryCharges").getValue());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        DeliveryChargeListener.addListenerForSingleValueEvent(eventListener);




    }
    public String getUid(){
        return uid;
    }
    public DatabaseReference getdatabase() {
        return mdatabase;
    }

    public User(String name, List<Items> bought, String email, List<Items> cartItems,Context context) {
        this.name = name;
        this.bought=bought;
        this.email = email;
        this.cartItems = cartItems;
        this.context=context;

    }

    public User(String name, List<Items> bought, String email, List<Items> cartItems,Context context,String PhoneNumber,String password) {
        this.name = name;
        this.bought=bought;
        this.email = email;
        this.cartItems = cartItems;
        this.context=context;
        this.PhoneNumber=PhoneNumber;
        this.password=password;

    }

    public String getName() {
        if(name!=null) {
            return name;
        }else{
            DatabaseReference childref = mdatabase.child("users").child(uid);
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

            return name;
        }

    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void AddtoCart(final Items item){
        cartItems.add(item);
        Log.i("TAG",uid);

        int countvalue=0;
        DatabaseReference childref = mdatabase.child("users").child(uid).child("cartItems");
        final ValueEventListener eventListener = new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Boolean found;
                String key="";
                int count;
                if(dataSnapshot.getValue()!=null) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        key = ds.getKey();


                    }
                    count = Integer.parseInt(key) + 1;
                    Log.i("AG", Integer.toString(count));

                }else{
                    count=0;
                }
                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put("/users/"+uid+"/cartItems/"+count,item);
                mdatabase.updateChildren(childUpdates).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context,item.getProduct()+" added to Cart",Toast.LENGTH_LONG).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context," Try Again",Toast.LENGTH_LONG).show();
                    }
                });


            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        childref.addListenerForSingleValueEvent(eventListener);
        Log.i("AAA",Integer.toString(count));

        ProgressBar progressBar=new ProgressBar(context);


    }

    public void Bought(final Items item,String Quantity){
        double lat=location.getCurrentLatitude();
        double longitude=location.getCurrentLongitude();
        UserLocation=location.getAddress(lat,longitude);
        cordinates="Latitude: "+Double.toString(lat)+" Longitude: "+Double.toString(longitude);
        DatabaseReference DeliveryChargeListener=mdatabase.getDatabase().getReference();

        final ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Log.i("aaaaaaaaaaaaaaaaaaa",dataSnapshot.getValue().toString());
                Deliverycharges =Integer.parseInt((String)dataSnapshot.child("DeliveryCharges").getValue());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        DeliveryChargeListener.addListenerForSingleValueEvent(eventListener);
        item.setQuantity(Quantity);
        int price=Integer.parseInt(item.getQuantity())*Integer.parseInt(item.getPrice());
        if(item.getQuantity().equals("0")){
            Toast.makeText(context,"Quantity is zero",Toast.LENGTH_LONG).show();
            Deliverycharges=0;
            return;
        }
        int totalpricewithDelivery=price+Deliverycharges;
        item.setDeliveryStatus("Request send");
        if(UserLocation==null){
            location.buildAlertMessageNoGps();

        }else{
            item.setUserLocation(UserLocation);
        new AlertDialog.Builder(context)
                .setTitle("Purcahase Confirmation")
                .setMessage(formatAStringPortionInBold("Confirming Your purchase of "+item.getQuantity()+" "+item.getUnit()+" of "+item.getProduct()+"\n Price = "+price+"\n Delivery Charge = "+Deliverycharges+ " Rs \n Total Price = "+totalpricewithDelivery+" Rs","Total Price = "+totalpricewithDelivery+" Rs",false))
                .setIcon(R.drawable.bkrik_action_name)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        new AlertDialog.Builder(context)
                                .setTitle("Location Confirmation")
                                .setMessage("Items will be Delivered to  " + item.getUserLocation() + "\n Confirm Delivery")
                                .setIcon(R.drawable.bkrik_action_name)
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        bought.add(item);
                                        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                                        DatabaseReference childref = mdatabase.getDatabase().getReference().child("users").child(uid).child("bought");
                                        final ValueEventListener eventListener = new ValueEventListener() {

                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {

                                                String key="0";
                                                final int count;
                                                if(dataSnapshot.getValue()!=null) {
                                                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                                        key = ds.getKey();
                                                        if(key.contains("basket")){
                                                            key=key.substring(6);
                                                        }


                                                    }
                                                    count = Integer.parseInt(key) + 1;
                                                    Log.i("AG", Integer.toString(count));

                                                }else{
                                                    count=0;
                                                }
                                                Map<String, Object> childUpdates = new HashMap<>();
                                                childUpdates.put("/users/"+uid+"/bought/"+count,item);
                                                mdatabase.updateChildren(childUpdates).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Toast.makeText(context,item.getProduct()+" Item Will be Delivered Soon",Toast.LENGTH_LONG).show();
                                                        Map<String, Object> childUpdatesDelivery = new HashMap<>();
                                                        childUpdatesDelivery.put("/users/"+uid+"/bought/"+count+"/cordinates",cordinates);
                                                        mdatabase.updateChildren(childUpdatesDelivery);

                                                        Query query = mdatabase.getDatabase().getReference().child("users").child(uid).child("cartItems");


                                                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                for (DataSnapshot Snapshot: dataSnapshot.getChildren()) {
                                                                    String value=Snapshot.getValue().toString();
                                                                    if(value.contains(item.getProduct())){
                                                                        Snapshot.getRef().removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void aVoid) {
                                                                                Log.i("success","item removed from the cart");
                                                                            }
                                                                        }).addOnFailureListener(new OnFailureListener() {
                                                                            @Override
                                                                            public void onFailure(@NonNull Exception e) {
                                                                                Log.i("error","item not removed from the cart");
                                                                            }
                                                                        });

                                                                        break;
                                                                    }
                                                                }
                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {
                                                                Log.e("aa", "onCancelled", databaseError.toException());
                                                            }
                                                        });
                                                        context.startActivity(new Intent(context,BKrikCart.class));

                                                        showNotification("Item purchased",item.getProduct()+" will be delivered soon");

                                                        //service
                                                        Intent service=new Intent(context,NotificationTrigger.class);
                                                        service.putExtra("uid",uid);
                                                        service.putExtra("shop",item.getShop());
                                                        service.putExtra("product",item.getProduct());
                                                        context.startService(service);
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(context,item.getProduct()+" Try Again",Toast.LENGTH_LONG).show();
                                                    }
                                                });

                                            }
                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {}
                                        };

                                        childref.addListenerForSingleValueEvent(eventListener);


                                        //Location

                                    }
                                }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //TODO
                                Toast.makeText(context,"Come again Bbye",Toast.LENGTH_LONG).show();

                            }
                        }).show();
                    }}).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //TODO
                Toast.makeText(context,"Come again Bbye",Toast.LENGTH_LONG).show();

            }
        }).show();

    }}
    public void boughtToDone(final Items item){
        bought.add(item);
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference childref = mdatabase.getDatabase().getReference().child("users").child(uid).child("done");
        final ValueEventListener eventListener = new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String key="";
                int count;
                if(dataSnapshot.getValue()!=null) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        key = ds.getKey();


                    }
                    count = Integer.parseInt(key) + 1;
                    Log.i("AG", Integer.toString(count));

                }else{
                    count=0;
                }
                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put("/users/"+uid+"/done/"+count,item);
                mdatabase.updateChildren(childUpdates).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Query query = mdatabase.getDatabase().getReference().child("users").child(uid).child("bought");

                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot Snapshot: dataSnapshot.getChildren()) {
                                    String value=Snapshot.getValue().toString();
                                    if(value.contains(item.getProduct())){
                                        Snapshot.getRef().removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.i("TSG","Item removed from bought");
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.i("TSG","Item not removed from bought");
                                            }
                                        });
                                        break;
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.e("aa", "onCancelled", databaseError.toException());
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context,item.getProduct()+" Try Again",Toast.LENGTH_LONG).show();
                    }
                });

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };

        childref.addListenerForSingleValueEvent(eventListener);
    }
    void showNotification(String title, String content) {
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("default",
                    "YOUR_CHANNEL_NAME",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("YOUR_NOTIFICATION_CHANNEL_DISCRIPTION");
            mNotificationManager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, "default")
                .setSmallIcon(R.drawable.bkrik_action_name) // notcation icon
                .setContentTitle(title) // title for notification
                .setContentText(content)// message for notification
                // set alarm sound for notification
                .setAutoCancel(true); // clear notification after click
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pi);
        mNotificationManager.notify(0, mBuilder.build());
    }

public void BuyAll(final List<Items> items){
    double lat=location.getCurrentLatitude();
    double longitude=location.getCurrentLongitude();
    UserLocation=location.getAddress(lat,longitude);
    cordinates="Latitude: "+Double.toString(lat)+" Longitude: "+Double.toString(longitude);
    uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    //String DeliveryCharge=mdatabase.getDatabase().getReference().child("DeliveryCharges");
    int TotalPrice=0;
    for(Items item:items){
        TotalPrice+=Integer.parseInt(item.getPrice())*Integer.parseInt(item.getQuantity());
    }
    DatabaseReference DeliveryChargeListener=mdatabase.getDatabase().getReference();

    final ValueEventListener eventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            //Log.i("aaaaaaaaaaaaaaaaaaa",dataSnapshot.getValue().toString());
            Deliverycharges =Integer.parseInt((String)dataSnapshot.child("DeliveryCharges").getValue());
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };
    DeliveryChargeListener.addListenerForSingleValueEvent(eventListener);
    if(items.size()==0){
        Toast.makeText(context,"No item selected",Toast.LENGTH_LONG).show();
    }else{
    int totalpricewithDelivery=TotalPrice+Deliverycharges;

        if (UserLocation == null) {
            location.buildAlertMessageNoGps();

        } else {
            for(Items item:items){
                item.setUserLocation(UserLocation);
            }

            new AlertDialog.Builder(context)
                    .setTitle("Purcahase Confirmation")
                    .setMessage(formatAStringPortionInBold("Confirming Your purchase of "+items.size()+" items of price "+TotalPrice+" Rs"+"\nDelivery charge of Rs "+Deliverycharges+" Rs \n Total Price = "+totalpricewithDelivery+" Rs","Total Price = "+totalpricewithDelivery+" Rs",false))
                    .setIcon(R.drawable.bkrik_action_name)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {
                            new AlertDialog.Builder(context)
                                    .setTitle("Location Confirmation")
                                    .setMessage("Items will be Delivered to  " + UserLocation + "\n Confirm Delivery")
                                    .setIcon(R.drawable.bkrik_action_name)
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {

                                            DatabaseReference childref = mdatabase.getDatabase().getReference().child("users").child(uid).child("bought");
                                            final ValueEventListener eventListener = new ValueEventListener() {

                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {

                                                    Boolean found;
                                                    String key="";
                                                    final int count;
                                                    if(dataSnapshot.getValue()!=null) {
                                                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                                            key = ds.getKey();
                                                            if(key.contains("basket")){
                                                                key=key.substring(6);
                                                            }


                                                        }
                                                        count = Integer.parseInt(key) + 1;
                                                        Log.i("AG", Integer.toString(count));

                                                    }else{
                                                        count=0;
                                                    }
                                                    Map<String, Object> childUpdates = new HashMap<>();

                                                    childUpdates.put("/users/"+uid+"/bought/basket"+count,items);
                                                    mdatabase.updateChildren(childUpdates).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Map<String, Object> childUpdatesDelivery = new HashMap<>();
                                                            childUpdatesDelivery.put("/users/"+uid+"/bought/basket"+count+"/deliveryStatus","Request Send");
                                                            childUpdatesDelivery.put("/users/"+uid+"/bought/basket"+count+"/cordinates",cordinates);
                                                            mdatabase.updateChildren(childUpdatesDelivery);

                                                           // Query queryfordeliverystatus=mdatabase.getDatabase().getReference().child("users").child(uid).child("bought");
                                                            Query query = mdatabase.getDatabase().getReference().child("users").child(uid).child("cartItems");

                                                            query.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                                    for (DataSnapshot Snapshot: dataSnapshot.getChildren()) {
                                                                        String value=Snapshot.getValue().toString();
                                                                        for(int i=0;i<items.size();i++){
                                                                        if(value.contains(items.get(i).getProduct())){
                                                                            final int finalI = i;
                                                                            Snapshot.getRef().removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                @Override
                                                                                public void onSuccess(Void aVoid) {
                                                                                    Log.i("TSG","Item removed from cart");
                                                                                }
                                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                                @Override
                                                                                public void onFailure(@NonNull Exception e) {
                                                                                    Log.i("TSG","Item not removed from cart");
                                                                                }
                                                                            }).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if(finalI ==items.size()-1) {
                                                                                        context.startActivity(new Intent(context, BKrikCart.class));
                                                                                        showNotification("Item purchased", "Yout basket of "+items.size()+" items will be delivered soon");
                                                                                        Intent service=new Intent(context,NotificationTrigger.class);
                                                                                        service.putExtra("uid",uid);
                                                                                        service.putExtra("shop","shop");
                                                                                        service.putExtra("product","basket"+count);
                                                                                        context.startService(service);
                                                                                    }
                                                                                    //service

                                                                                }
                                                                            });
                                                                            break;
                                                                        }
                                                                    }
                                                                }}

                                                                @Override
                                                                public void onCancelled(DatabaseError databaseError) {
                                                                    Log.e("aa", "onCancelled", databaseError.toException());
                                                                }
                                                            });
                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Toast.makeText(context," Try Again",Toast.LENGTH_LONG).show();
                                                        }
                                                    });

                                                }
                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {}
                                            };

                                            childref.addListenerForSingleValueEvent(eventListener);

                                            //Location

                                        }
                                    }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //TODO
                                    Toast.makeText(context,"Come again Bbye",Toast.LENGTH_LONG).show();

                                }
                            }).show();
                        }}).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //TODO
                    Toast.makeText(context,"Come again Bbye",Toast.LENGTH_LONG).show();

                }
            }).show();


        }

}}
public void removeFromCart(final Items item){
    Query query = mdatabase.getDatabase().getReference().child("users").child(uid).child("cartItems");

    query.addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            for (DataSnapshot Snapshot: dataSnapshot.getChildren()) {
                String value = (String) Snapshot.child("product").getValue();
                if(item.getProduct().equals(value)){
                    Snapshot.getRef().removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.i("TSG","Item removed from cart");
                            context.startActivity(new Intent(context,BKrikCart.class));
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.i("TSG","Item not removed from cart");
                        }
                    });

                }else{
                    Log.i("aa",item.getProduct());
                    Log.i("bb",value);
                }
            }
            }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    });
}
    public static SpannableStringBuilder formatAStringPortionInBold(String completeString, String targetStringToFormat, boolean matchCase) {
        //Null complete string return empty
        if (TextUtils.isEmpty(completeString)) {
            return new SpannableStringBuilder("");
        }

        SpannableStringBuilder str = new SpannableStringBuilder(completeString);
        int start_index = 0;

        //if matchCase is true, match exact string
        if (matchCase) {
            if (TextUtils.isEmpty(targetStringToFormat) || !completeString.contains(targetStringToFormat)) {
                return str;
            }

            start_index = str.toString().indexOf(targetStringToFormat);
        } else {
            //else find in lower cases
            if (TextUtils.isEmpty(targetStringToFormat) || !completeString.toLowerCase().contains(targetStringToFormat.toLowerCase())) {
                return str;
            }

            start_index = str.toString().toLowerCase().indexOf(targetStringToFormat.toLowerCase());
        }

        int end_index = start_index + targetStringToFormat.length();
        str.setSpan(new StyleSpan(BOLD), start_index, end_index, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return str;
    }
}