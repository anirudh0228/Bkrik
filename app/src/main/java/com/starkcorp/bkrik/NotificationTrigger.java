package com.starkcorp.bkrik;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Intent;
import android.content.Context;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */

public class NotificationTrigger extends Service {
    private Handler mHandler = new Handler();   //run on another Thread to avoid crash
    private int flag=0;
    public void CheckDeliveryStatus(final Intent intent) {
        String uid=intent.getStringExtra("uid");
        final String shop=intent.getStringExtra("shop");
        final String product=intent.getStringExtra("product");
        DatabaseReference childref;
        int flag=0;
       if(product.contains("basket")){
            childref= FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("bought").child(product);


        }else{
            childref= FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("bought");

        }
        //progressDialog = ProgressDialog.show(MainActivity.this,"Wait","Searching for "+query,false,false);
        final ValueEventListener eventListener = new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.getValue()!=null){
                    for(DataSnapshot ds:dataSnapshot.getChildren()){

                        Log.i("product",ds.getValue().toString());
                        if(!product.contains("basket")){
                            if(!ds.getKey().contains("basket")) {
                                String DeliveryStatus = (String) ds.child("deliveryStatus").getValue();
                                String dshop = (String) ds.child("shop").getValue();
                                String dproduct = (String) ds.child("product").getValue();
                                if (dshop.equals(shop) && dproduct.equals(product)) {
                                    Log.i("fffasd", DeliveryStatus);
                                    if (DeliveryStatus.equals("Delivered")) {
                                        showNotification("Delivery", product + " has been delivered to your place");
                                        stopSelf();

                                    }else if(DeliveryStatus.equals("Canceled")){
                                        showNotification("Delivery", product + " has been Canceled. Sorry for the inconvenience");
                                        stopSelf();
                                    }
                                }
                            }

                    }
                    }
                    if(product.contains("basket")){

                            String DeliveryStatus=(String)dataSnapshot.child("deliveryStatus").getValue();
                            if(DeliveryStatus.equals("Delivered")){
                                showNotification("Delivery",product+" has been delivered to your place");
                                stopService(new Intent(NotificationTrigger.this,NotificationTrigger.class));
                            }else if(DeliveryStatus.equals("Canceled")){
                                showNotification("Delivery", product + " has been Canceled. Sorry for the inconvenience");
                                stopService(new Intent(NotificationTrigger.this, NotificationTrigger.class));

                            }

                    }

                }else{
                    ///Toast.makeText(getApplicationContext(),"No item",Toast.LENGTH_LONG).show();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        childref.addListenerForSingleValueEvent(eventListener);


    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate() {
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {

        CheckDeliveryStatus(intent);


        Runnable updateTimerThread = new Runnable()
        {
            public void run()
            {
                CheckDeliveryStatus(intent);
                //write here whaterver you want to repeat
                mHandler.postDelayed(this, 10000);
            }
        };
        mHandler.postDelayed(updateTimerThread, 0);
        return START_STICKY;
    }

    public void showNotification(String title, String content) {
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("default",
                    "YOUR_CHANNEL_NAME",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("YOUR_NOTIFICATION_CHANNEL_DISCRIPTION");
            mNotificationManager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), "default")
                .setSmallIcon(R.drawable.bkrik_action_name) // notification icon
                .setContentTitle(title) // title for notification
                .setContentText(content)// message for notification
                // set alarm sound for notification
                .setAutoCancel(true); // clear notification after click

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pi);
        mNotificationManager.notify(0, mBuilder.build());
    }
}
