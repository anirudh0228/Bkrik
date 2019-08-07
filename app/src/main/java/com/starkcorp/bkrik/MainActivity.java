package com.starkcorp.bkrik;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.LocationListener;
import android.provider.SearchRecentSuggestions;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;

import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RatingBar;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.support.v7.widget.DividerItemDecoration.HORIZONTAL;
import static android.support.v7.widget.DividerItemDecoration.VERTICAL;

public class MainActivity extends AppCompatActivity {

public List<Items> shops;
    private ProgressDialog progressDialog;
    private FirebaseAuth auth;
   // private ViewPager mViewPager;
    private AutoCompleteTextView searchfield;
    public FirebaseDatabase database;
    public DatabaseReference myRef;
    public DatabaseReference mdatabase;
    public StorageReference storageRef;
    public List<String> products;
    private DrawerLayout mDrawerLayout;
    private List<String> querylist;
    private  String uid;
    private RecyclerView mRecyclerView;
    private RecyclerView mCategoryList;

    private RecyclerView.Adapter mAdapter;
    private RecyclerView.Adapter mCategoryAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.LayoutManager mCategoryLayout;
    private FirebaseAuth mAuth;
    private String name;
    DividerItemDecoration itemDecor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        searchfield=(AutoCompleteTextView)findViewById(R.id.Search);
        database=FirebaseDatabase.getInstance();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        storageRef= FirebaseStorage.getInstance().getReference();
        myRef=database.getReference();
        products=new ArrayList<>();
        querylist=new ArrayList<>();
        mDrawerLayout = findViewById(R.id.drawer_layout);

        mCategoryList=findViewById(R.id.Categorylist);
        mCategoryLayout=new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false);
        mCategoryList.setLayoutManager(mCategoryLayout);
        mCategoryList.setHasFixedSize(true);

        mRecyclerView = findViewById(R.id.ProductList);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        itemDecor= new DividerItemDecoration(mRecyclerView.getContext(), VERTICAL);
        //itemDecor.setDrawable(ContextCompat.getDrawable(this,R.drawable.custom_divider));
        mRecyclerView.addItemDecoration(itemDecor);


        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user=mAuth.getCurrentUser();
        FirebaseAuth.AuthStateListener authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null){
                    //Do anything here which needs to be done after signout is complete
                    startActivity(new Intent(MainActivity.this,loginact.class));
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



        Runnable runnable=new Runnable() {
            @Override
            public void run() {
               refresh();

            }
        };
        runnable.run();


        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(MainActivity.this,BKrikCart.class);
                startActivity(i);



            }
        });



        final Runnable productNames=new Runnable() {
            @Override
            public void run() {
                DatabaseReference childref=myRef.child("items");
                final ValueEventListener eventListener = new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if(dataSnapshot.getValue()!=null){
                            shops=new ArrayList<>();
                            for(DataSnapshot ds:dataSnapshot.getChildren()){
                                    products.add(ds.getKey());
                                //Log.i("gas",ds.getKey());
                            }

                        }else {
                            ///Toast.makeText(getApplicationContext(),"No item",Toast.LENGTH_LONG).show();
                        }


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                };
                childref.addListenerForSingleValueEvent(eventListener);
            }
        };
        productNames.run();

        searchfield.setDropDownBackgroundResource(R.color.white);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, products);
        searchfield.setAdapter(adapter);

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


    public void opendrawer(View v){
        mDrawerLayout.openDrawer(GravityCompat.START);

    }



    public void onsearch(View v){
            //startSearch(null,false,null,false);
        DatabaseReference childref=myRef.child("items");
        final String query=searchfield.getText().toString();
        querylist=new ArrayList<>();

        final ValueEventListener eventListenerlist = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds:dataSnapshot.getChildren()){
                    if(ds.getKey().contains(query)){
                        querylist.add(ds.getKey());
                        Log.i("name",ds.getKey());
                    }
                }
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        childref.addListenerForSingleValueEvent(eventListenerlist);


        progressDialog = ProgressDialog.show(MainActivity.this,"Wait","Searching",false,false);
        final ValueEventListener eventListener = new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Boolean found;
                shops=new ArrayList<>();
                for(int i=0;i<querylist.size();i++){
                if(dataSnapshot.child(querylist.get(i)).getValue()!=null){

                    for(DataSnapshot ds:dataSnapshot.child(querylist.get(i)).getChildren()){
                        //Log.i("sss",ds.getValue().toString());
                        String productName,shopName,ratings,unit,imageurl,price,status;
                        productName=querylist.get(i);
                        shopName=(String)ds.getKey();
                        ratings=(String)ds.child("ratings").getValue();
                        unit=(String)ds.child("unit").getValue();
                        imageurl=(String)ds.child("imageurl").getValue();
                        price=(String)ds.child("price").getValue();
                        status=(String)ds.child("status").getValue();
                        //String details[]=extractDetails(ds.getValue().toString());
                        Items shop=new Items(productName, shopName, ratings, price,unit,imageurl);//String product, String shop, String ratings, String price, String unit, imageuri
                        shop.setStatusInStore(status);

                       shops.add(shop);
                    }
                    List<Items> tempShops=new ArrayList<>();
                    tempShops=shops;
                   mAdapter=new customview(MainActivity.this,tempShops);
                   mRecyclerView.setAdapter(mAdapter);
                    progressDialog.dismiss();
                }else{
                    ///Toast.makeText(getApplicationContext(),"No item",Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();
                }


            }}

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        childref.addListenerForSingleValueEvent(eventListener);

        //myRef.addListenerForSingleValueEvent(eventListener);
    }


    public void refresh(){
        DatabaseReference childref=myRef.child("selecteditem");
        progressDialog = ProgressDialog.show(MainActivity.this,"Wait","Loading ",false,false);
        shops=new ArrayList<>();
        final ValueEventListener eventListener = new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.getValue()!=null){

                    DatabaseReference refitem=myRef.child("items");
                    for(final DataSnapshot ds:dataSnapshot.getChildren())
                    specifiedItems :{
                        Log.i("ds",ds.getKey());
                        refitem.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for(DataSnapshot dsitem:dataSnapshot.getChildren())
                                productsinItems :{
                                    //Log.i("dsitem",dsitem.getKey());
                                    for(DataSnapshot dsshop:dsitem.getChildren())
                                    shopsInItems :{
                                        //Log.i("dsshopf",dsshop.getKey());

                                        String shopinSpecifiedItems=(String)ds.getValue();
                                        String shopinItems=dsshop.getKey();
                                        if(ds.getKey().equalsIgnoreCase(dsitem.getKey())&&shopinSpecifiedItems.equalsIgnoreCase(shopinItems)){
                                            Log.i("ds",ds.getKey());
                                            String productName,shopName,ratings,unit,imageurl,price,status;
                                            productName=ds.getKey();
                                            shopName=(String)ds.getValue();
                                            ratings=(String)dsshop.child("ratings").getValue();
                                            unit=(String)dsshop.child("unit").getValue();
                                            imageurl=(String)dsshop.child("imageurl").getValue();
                                            price=(String)dsshop.child("price").getValue();
                                            status=(String)dsshop.child("status").getValue();
                                            //String details[]=extractDetails(ds.getValue().toString());
                                            Items shop=new Items(productName, shopName, ratings, price,unit,imageurl);//String product, String shop, String ratings, String price, String unit, imageuri
                                            shop.setStatusInStore(status);
                                            shops.add(shop);
                                            break productsinItems;
                                        }
                                    }

                                }
                                mAdapter=new customview(MainActivity.this,shops);
                                Log.i("count",Integer.toString(shops.size()));
                                mRecyclerView.setAdapter(mAdapter);
                                CategoryRefresh();
                                progressDialog.dismiss();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }



                }else{
                    ///Toast.makeText(getApplicationContext(),"No item",Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        childref.addListenerForSingleValueEvent(eventListener);

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
                                Toast.makeText(MainActivity.this,"Thanks for the Review",Toast.LENGTH_LONG).show();
                                dialog.hide();




                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(MainActivity.this,"Review can't be uploaded",Toast.LENGTH_LONG).show();
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
    public void CategoryRefresh(){
        final List<String> categoryNames=new ArrayList<>();
        DatabaseReference childref=myRef.child("categories");
        final ValueEventListener eventListener=new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue()!=null){
                    for(DataSnapshot ds:dataSnapshot.getChildren()){
                      categoryNames.add(ds.getKey());
                    }
                    mCategoryAdapter=new CategoryList(MainActivity.this,categoryNames);
                    mCategoryList.setAdapter(mCategoryAdapter);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        childref.addListenerForSingleValueEvent(eventListener);
    }
    public void home(View v){
        Intent i=new Intent(MainActivity.this,MainActivity.class);
        startActivity(i);
    }

}
