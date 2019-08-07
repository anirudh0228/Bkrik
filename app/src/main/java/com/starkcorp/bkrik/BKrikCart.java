package com.starkcorp.bkrik;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.support.v7.widget.DividerItemDecoration.HORIZONTAL;
import static android.support.v7.widget.DividerItemDecoration.VERTICAL;

public class BKrikCart extends AppCompatActivity {
private DatabaseReference myRef;
private  String uid;
public List<Items> purchaseditems;
public List<Items> Cartitems;
public DrawerLayout mDrawerLayout;
public List<String> products;
private TabLayout tabLayout;
private AutoCompleteTextView searchfield;ProgressDialog progressDialog ;
 private RelativeLayout relativeLayout;
 private TextView totalCartPrice;
 private ListAdapter adapter;
 private User user;
 private FirebaseAuth mAuth;

private RecyclerView mRecyclerView;
private RecyclerView.Adapter mAdapter;
private RecyclerView.LayoutManager mLayoutManager;

 private TextView noitemfield;
 private String noitem;
 private String name;
 DividerItemDecoration itemDecor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bkrikcart);
        myRef= FirebaseDatabase.getInstance().getReference();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        purchaseditems=new ArrayList<>();
        Cartitems=new ArrayList<>();
        searchfield=(AutoCompleteTextView) findViewById(R.id.Search);
        mDrawerLayout = findViewById(R.id.drawer_layout2);
        relativeLayout=(RelativeLayout)findViewById(R.id.relative1) ;
        tabLayout=(TabLayout)findViewById(R.id.tabbar);
        totalCartPrice=(TextView)findViewById(R.id.totalprice);
        user=new User(BKrikCart.this);
        noitemfield=(TextView)findViewById(R.id.noitem);
        noitem=noitemfield.getText().toString();
        mRecyclerView = (RecyclerView) findViewById(R.id.ProductList);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        itemDecor= new DividerItemDecoration(this, VERTICAL);
        mRecyclerView.addItemDecoration(itemDecor);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user=mAuth.getCurrentUser();

        FirebaseAuth.AuthStateListener authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null){
                    //Do anything here which needs to be done after signout is complete
                    startActivity(new Intent(BKrikCart.this,loginact.class));
                    finish();
                }
                else {
                }
            }
        };
        mAuth.addAuthStateListener(authStateListener);

        NavigationView navigationView = findViewById(R.id.nav_view);
        View hView =  navigationView.inflateHeaderView(R.layout.nav_header);
        TextView headerName=hView.findViewById(R.id.headerName);
        headerName.setText(user.getDisplayName());


        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // set item as selected to persist highlight
                        menuItem.setChecked(true);
                        switch (menuItem.getItemId()){
                            case R.id.nav_Logout:
                                signOut();
                                break;
                            case R.id.nav_help:
                                feedback();
                        }
                        // close drawer when item is tapped
                        mDrawerLayout.closeDrawers();

                        // Add code here to update the UI based on the item selected
                        // For example, swap UI fragments here

                        return true;
                    }
                });



        refreshCart();
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(tab.getPosition()==0){
                    refreshCart();
                }else if(tab.getPosition()==1){
                    refreshPurchased();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

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
    public void refreshPurchased(){
        relativeLayout.setVisibility(View.GONE);
        noitemfield.setVisibility(View.GONE);
        progressDialog = ProgressDialog.show(BKrikCart.this,"Loading","Please Wait",false,false);
        DatabaseReference childref=myRef.child("users").child(uid).child("bought");
        final ValueEventListener eventListener = new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
            purchaseditems=new ArrayList<>();
                if(dataSnapshot.getValue()!=null){
                    for(DataSnapshot ds:dataSnapshot.getChildren()) {
                        //String details[]=extractDetails(ds.getValue().toString());

                        Log.i("TAG", ds.getValue().toString());
                        if (!ds.getKey().contains("basket")) {
                            String product = (String) ds.child("product").getValue();
                            String ratings = (String) ds.child("ratings").getValue();
                            String unit = (String) ds.child("unit").getValue();
                            String imageurl = (String) ds.child("imageurl").getValue();
                            String price = (String) ds.child("price").getValue();
                            String shop = (String) ds.child("shop").getValue();
                            String deliveryStatus = (String) ds.child("deliveryStatus").getValue();
                            String quantity = (String) ds.child("quantity").getValue();
                            String userLocation = (String) ds.child("userLocation").getValue();
                            //String details[]=extractDetailsforPurchasedItem(ds.getValue().toString());
                            //Log.i("AAAAAAAAAAAA",details[5]);
                            //product,shop,ratings,price,unit,quantity,location,deliverystatus,imageurl

                            Items item = new Items(product, shop, ratings, price, unit, imageurl);
                            item.setQuantity(quantity);
                            item.setUserLocation(userLocation);
                            item.setDeliveryStatus(deliveryStatus);
                            purchaseditems.add(item);
                        } else {
                            for (DataSnapshot ds1 : ds.getChildren()) {
                                Log.i("print", ds1.getValue().toString());
                                if (!ds1.getValue().toString().equalsIgnoreCase("Request Send") && !ds1.getValue().toString().equalsIgnoreCase("on the way") && !ds1.getValue().toString().equalsIgnoreCase("delivered")) {
                                    String product = (String) ds1.child("product").getValue();
                                    String ratings = (String) ds1.child("ratings").getValue();
                                    String unit = (String) ds1.child("unit").getValue();
                                    String imageurl = (String) ds1.child("imageurl").getValue();
                                    String price = (String) ds1.child("price").getValue();
                                    String shop = (String) ds1.child("shop").getValue();
                                    String deliveryStatus = (String) ds.child("deliveryStatus").getValue();
                                    String quantity = (String) ds1.child("quantity").getValue();
                                    String userLocation = (String) ds1.child("userLocation").getValue();
                                    //String details[]=extractDetailsforPurchasedItem(ds.getValue().toString());
                                    //Log.i("AAAAAAAAAAAA",details[5]);
                                    //product,shop,ratings,price,unit,quantity,location,deliverystatus,imageurl

                                    Items item = new Items(product, shop, ratings, price, unit, imageurl);
                                    item.setQuantity(quantity);
                                    item.setUserLocation(userLocation);
                                    item.setDeliveryStatus(deliveryStatus);
                                    purchaseditems.add(item);
                                }
                            }

                        }
                        mAdapter = new PurchaseItemList(BKrikCart.this, purchaseditems);
                        //ListView lv = (ListView) findViewById(R.id.ProductList);
                        mRecyclerView.setAdapter(mAdapter);

                        progressDialog.dismiss();
                    }


                }else{
                    mAdapter = new PurchaseItemList(BKrikCart.this, purchaseditems);
                    //ListView lv = (ListView) findViewById(R.id.ProductList);
                    mRecyclerView.setAdapter(mAdapter);
                    noitemfield.setVisibility(View.VISIBLE);
                    noitemfield.setText("No Purchased item yet");
                    progressDialog.dismiss();
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        childref.addListenerForSingleValueEvent(eventListener);
    }
    public void refreshCart(){
        relativeLayout.setVisibility(View.VISIBLE);
        noitemfield.setVisibility(View.GONE);
        progressDialog = ProgressDialog.show(BKrikCart.this,"Loading","Please Wait",false,false);
        DatabaseReference childref=myRef.child("users").child(uid);

        final ValueEventListener eventListener = new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Cartitems=new ArrayList<>();

                Boolean found;
                if(dataSnapshot.getValue()!=null){

                    List<Items> AllData=new ArrayList<>();
                    for(DataSnapshot ds:dataSnapshot.child("cartItems").getChildren()){
                        //String details[]=extractDetails(ds.getValue().toString());
                        Log.i("TiG",ds.getValue().toString());
                       // String details[]=extractDetailsforCart(ds.getValue().toString());
                        String product=(String)ds.child("product").getValue();
                        String ratings=(String)ds.child("ratings").getValue();
                        String unit=(String)ds.child("unit").getValue();
                        String imageurl=(String)ds.child("imageurl").getValue();
                        String price=(String)ds.child("price").getValue();
                        String shop=(String)ds.child("shop").getValue();

                        //product,shop,ratings,price,unit,quantity,location,deliverystatus,imageurl
                        Items itemwithShop=new Items( product, shop, ratings, price,unit,imageurl);
                        Cartitems.add(itemwithShop);

                    }


                    mAdapter=new cartview(BKrikCart.this,Cartitems);
                    //ListView lv=(ListView)findViewById(R.id.ProductList);
                    mRecyclerView.setAdapter(mAdapter);
                    int totalprice=0;
                    for(int i=0;i<Cartitems.size();i++){
                        if(Cartitems.get(i).getQuantity()==null){
                            continue;
                        }else {
                            totalprice+=Integer.parseInt(Cartitems.get(i).getQuantity())*Integer.parseInt(Cartitems.get(i).getPrice());
                        }
                    }
                    totalCartPrice.setText(Integer.toString(totalprice)+" Rs");
                    if(Cartitems.size()==0){
                        noitemfield.setVisibility(View.VISIBLE);
                        noitemfield.setText("No Item in Cart");
                    }
                    progressDialog.dismiss();


                }else{
                    Toast.makeText(getApplicationContext(),"No item",Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        childref.addListenerForSingleValueEvent(eventListener);
    }

    public void opendrawer(View v){
        mDrawerLayout.openDrawer(GravityCompat.START);

    }
    public void onsearch(View v){

        final String query=searchfield.getText().toString();
        List<Items> searchedItems = new ArrayList<>();
        if(tabLayout.getSelectedTabPosition()==0) {

            for (int i = 0; i < Cartitems.size(); i++) {
                if (Cartitems.get(i).getProduct().contains(query)) {
                    searchedItems.add(Cartitems.get(i));
                }

            }
        }else{
            for (int i = 0; i < purchaseditems.size(); i++) {
                if (purchaseditems.get(i).getProduct().contains(query)) {
                    searchedItems.add(purchaseditems.get(i));
                }

            }

        }
        mAdapter=new cartview(BKrikCart.this,searchedItems);
        mRecyclerView.setAdapter(mAdapter);

        //myRef.addListenerForSingleValueEvent(eventListener);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if(tabLayout.getSelectedTabPosition()==0){
            refreshCart();
        }else{
            refreshPurchased();
        }
    }

  public void buyAll(View v){

        List<Items> updatedCartItems=cartview.getDetails1();

        user.BuyAll(updatedCartItems);
    }
    private void signOut() {
        mAuth.signOut();


    }
    private void feedback(){
        final Dialog dialog=new Dialog(this);
        dialog.setContentView(R.layout.dialogforrating);
        dialog.setCancelable(true);
        dialog.show();
        Button submit=(Button)dialog.findViewById(R.id.submit);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RatingBar ratingBar=(RatingBar)dialog.findViewById(R.id.Ratingbar);
                String Ratings=Float.toString(ratingBar.getRating());
                EditText reviewfield=(EditText)dialog.findViewById(R.id.review);
                String reviewfromfield=reviewfield.getText().toString();
                final Reviews review=new Reviews(name,reviewfromfield,Ratings);
                final DatabaseReference mdatabase= FirebaseDatabase.getInstance().getReference();
                DatabaseReference childref = mdatabase.getDatabase().getReference().child("AppReviews").child(name);
                final ValueEventListener eventListener = new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Map<String, Object> childUpdates = new HashMap<>();
                        childUpdates.put("/AppReviews/"+name+"/",review);
                        mdatabase.updateChildren(childUpdates).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(BKrikCart.this,"Thanks for the Review",Toast.LENGTH_LONG).show();
                                dialog.hide();




                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(BKrikCart.this,"Review can't be uploaded",Toast.LENGTH_LONG).show();
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
    public void home(View v){
        Intent i=new Intent(BKrikCart.this,MainActivity.class);
        startActivity(i);
    }
}
