package com.example.olx.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;


import com.example.olx.CurrencyFormatter;
import com.example.olx.R;
import com.example.olx.Utils;
import com.example.olx.adapter.AdapterCart;
import com.example.olx.adapter.AdapterOrder;
import com.example.olx.databinding.ActivityShopOrderUserDetailBinding;
import com.example.olx.model.ModelCart;
import com.example.olx.model.ModelOrder;
import com.example.olx.model.ModelOrderSeller;
import com.example.olx.model.ModelOrderUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ShopOrderUserDetailActivity extends AppCompatActivity {
    private ActivityShopOrderUserDetailBinding binding;

    String orderId, orderTo;
    private ArrayList<ModelOrder> orderArrayList;
    private AdapterOrder adapterOrder;
    private FirebaseAuth firebaseAuth;
    private static final String TAG ="ODER_DETAILS_USER";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityShopOrderUserDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        final Intent intent = getIntent();
        orderTo = intent.getStringExtra("orderTo"); //orderTo contains uid of the shop where we placed order
        orderId = intent.getStringExtra("orderId");

        loadShopInfo();
        loadOrderDetails();
        loadOrderedItems();
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        //handle writeReviewBtn click, start write review activity
        binding.writeReviewBtn.setOnClickListener(v -> {
            Intent intent1 = new Intent(ShopOrderUserDetailActivity.this, WriteReviewActivity.class);
            intent1.putExtra("shopUid", orderTo); // to write review to a shop we must have uid of shop
            startActivity(intent1);
        });
    }

    private void loadOrderedItems() {
        //init list
        orderArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(orderTo).child("Order").child(orderId).child("GioHang")
                .addValueEventListener(new ValueEventListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        orderArrayList.clear(); //before loading items clear list
                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                            ModelOrder modelOrder = ds.getValue(ModelOrder.class);
                            //add to list
                            orderArrayList.add(modelOrder);
                        }
                        //all items added to list
                        //setup adapter
                        adapterOrder = new AdapterOrder(ShopOrderUserDetailActivity.this, orderArrayList);
                        //set adapter
                        binding.itemsRv.setAdapter(adapterOrder);

                        //set items count
                        binding.totalItemsTv.setText(""+dataSnapshot.getChildrenCount());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void loadOrderDetails() {
        //load order details
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(orderTo).child("Order").child(orderId)
                .addValueEventListener(new ValueEventListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //get order info
                        ModelOrderUser modelOrderUser = dataSnapshot.getValue(ModelOrderUser.class);
                        String orderMaHD = ""+dataSnapshot.child("orderMaHD").getValue();
                        String orderTongTien = ""+dataSnapshot.child("orderTongTien").getValue();
                        String orderId = ""+dataSnapshot.child("orderId").getValue();
                        String orderStatus = ""+dataSnapshot.child("orderStatus").getValue();
                        String orderBy = ""+dataSnapshot.child("orderBy").getValue();
                        String orderTo = ""+dataSnapshot.child("orderTo").getValue();
                        String address = ""+dataSnapshot.child("address").getValue();
                        String latitude = ""+dataSnapshot.child("latitude").getValue();
                        String longitude = ""+dataSnapshot.child("longitude").getValue();
                        long timestamp = modelOrderUser.getTimestamp();


                        String dateFormated = Utils.formatTimestampDateTime(timestamp);

                        //order status
                        if (orderStatus.equals("Chưa duyệt")){
                            binding.orderStatusTv.setTextColor(getResources().getColor(R.color.colorblack));
                        }
                        else if (orderStatus.equals("Đã duyệt")){
                            binding.orderStatusTv.setTextColor(getResources().getColor(R.color.colorgold));
                        }
                        else if (orderStatus.equals("Đã hủy")){
                            binding.orderStatusTv.setTextColor(getResources().getColor(R.color.colorred));
                        }

                        //set data
                        binding.orderMaTv.setText(orderMaHD);
                        binding.orderStatusTv.setText(orderStatus);
                        binding.amountTv.setText(""+ CurrencyFormatter.getFormatter().format(Double.parseDouble(orderTongTien)));
                        binding.dateTv.setText(dateFormated);

                        findAddress(latitude, longitude); //to find delivery address
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void loadShopInfo() {
        //get shop info
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(orderTo)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String shopName = ""+dataSnapshot.child("shopName").getValue();
                        binding.shopNameTv.setText(shopName);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void findAddress(String latitude, String longitude) {
        double lat = Double.parseDouble(latitude);
        double lon = Double.parseDouble(longitude);

        //find address, country, state, city
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(lat, lon,1);

            String address = addresses.get(0).getAddressLine(0); //complete address
            binding.addressTv.setText(address);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}