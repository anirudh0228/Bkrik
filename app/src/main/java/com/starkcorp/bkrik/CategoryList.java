package com.starkcorp.bkrik;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by anirudh on 11/13/2018.
 */

public class CategoryList extends RecyclerView.Adapter<CategoryList.MyViewHolder> {
    Context context;
    private List<String> Categories;
    private String Category;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    FirebaseDatabase database;
    DatabaseReference myRef;
    ProgressDialog progressDialog;


    public CategoryList(Context context,List<String> Categories) {
        this.context = context;
        mRecyclerView =((Activity)context).findViewById(R.id.ProductList);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(context);
        mRecyclerView.setLayoutManager(mLayoutManager);
        database=FirebaseDatabase.getInstance();
        myRef=database.getReference();
        this.Categories=Categories;

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.categoryview, parent, false);

        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final MyViewHolder viewHolder=holder;
        Category=Categories.get(viewHolder.getAdapterPosition());
        holder.mTextView.setText(Category);
        holder.mTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Category=Categories.get(viewHolder.getAdapterPosition());
                Listrefresh(Category);

            }
        });

    }

    @Override
    public int getItemCount() {
        return Categories.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        private TextView mTextView;

        public MyViewHolder(View view) {
            super(view);
            mTextView=(TextView)view.findViewById(R.id.CategoryName);

        }



}

    public void Listrefresh(String child){


        DatabaseReference childref=myRef.child("categories").child(child);
        progressDialog = ProgressDialog.show(context,"Wait","Loading ",false,false);
        final List<Items> shops=new ArrayList<>();
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
                                    mAdapter=new customview(context,shops);
                                    Log.i("count",Integer.toString(shops.size()));
                                    mRecyclerView.setAdapter(mAdapter);
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
}
