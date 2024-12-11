package com.example.olx.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.olx.CurrencyFormatter;
import com.example.olx.R;
import com.example.olx.Utils;
import com.example.olx.adapter.AdapterCart;
import com.example.olx.adapter.AdapterOrder;
import com.example.olx.adapter.AdapterOrderSeller;
import com.example.olx.adapter.AdapterOrderUser;
import com.example.olx.databinding.ActivityShopOrderSellerDetailBinding;
import com.example.olx.model.ModelCart;
import com.example.olx.model.ModelOrder;
import com.example.olx.model.ModelOrderUser;
import com.example.olx.model.ModelUsers;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ShopOrderSellerDetailActivity extends AppCompatActivity {

    private ActivityShopOrderSellerDetailBinding binding;

    private String orderId, orderBy, orderTo;
    //to open destination in map
    private double latitude;
    private double longitude;

    private FirebaseAuth firebaseAuth;

    private ArrayList<ModelCart> cartArrayList;
    private AdapterOrder adapterOrder;
    private static final String TAG ="ODERSELLER";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityShopOrderSellerDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        //get data from intent
        orderId = getIntent().getStringExtra("orderId");
        orderBy = getIntent().getStringExtra("orderBy");
        orderTo = getIntent().getStringExtra("orderTo");



        firebaseAuth = FirebaseAuth.getInstance();

        loadUserInfo();
        loadOrderDetails();
        loadOrderedItems();

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        binding.editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editOrderStatusDialog();
            }
        });
        binding.mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //map
                if (firebaseAuth.getCurrentUser() == null) {
                    Utils.toast(ShopOrderSellerDetailActivity.this, "Bạn cần đăng nhập tài khoản");
                } else {
                    Utils.mapIntent(ShopOrderSellerDetailActivity.this, latitude, longitude);
                }
            }
        });
    }

    private void editOrderStatusDialog() {
        //options to display in dialog
        final String[] options = {"Chưa thanh toán", "Đã thanh toán", "Đã hủy"};
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chỉnh sửa Trạng thái Đơn hàng")
                .setItems(options, (dialogInterface, i) -> {
                    //handle item clikcs
                    String selectedOption = options[i];
                    editOrderStatus(selectedOption);
                })
                .show();
    }
    private void editOrderStatus(final String selectedOption) {
        //setup data to put in firebase db
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("orderStatus", ""+selectedOption);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Orders");
        ref.child(orderId)
                .updateChildren(hashMap)
                .addOnSuccessListener(aVoid -> {
                    String message = "Đặt hàng bây giờ là "+selectedOption;
                    //status updated
                    Utils.toastySuccess(ShopOrderSellerDetailActivity.this,"Trạng thái đặt hàng của bạn: "+selectedOption);
                    findAddress(String.valueOf(latitude), String.valueOf(longitude));
                })
                .addOnFailureListener(e -> {
                    //failed updating status, show reason
                    Utils.toastyError(ShopOrderSellerDetailActivity.this,"Lỗi");
                });
    }

    //load từng sản phẩm đã đặt
    private void loadOrderedItems() {
        Log.d(TAG, "loadOrderedItems: ");
        //init list
        cartArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Orders");
        ref.child(orderId).child("GioHang")
                .addValueEventListener(new ValueEventListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        cartArrayList.clear(); //before loading items clear list
                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                            ModelCart modelCart = ds.getValue(ModelCart.class);
                            //add to list
                            cartArrayList.add(modelCart);
                        }

                        //all items added to list
                        //setup adapter
                        adapterOrder = new AdapterOrder(ShopOrderSellerDetailActivity.this,  cartArrayList);
                        //set adapter
                        binding.row.setAdapter(adapterOrder);
                        //set items count
                        binding.totalItemsTv.setText(""+dataSnapshot.getChildrenCount());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    // load email, số điện thoại người mua
    private void loadUserInfo() {
        Log.d(TAG, "loadUserInfo: ");
        //to load email of the user/buyer: modelOrderShop.getOrderBy() contains uid of that user/buyer
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(orderBy)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                            ModelUsers modelUsers = userSnapshot.getValue(ModelUsers.class);

                            String email = modelUsers.getEmail();
                            Log.d(TAG, "Email: " + email);
                            binding.emailTv.setText("Email: "+email);

                            String sdt = modelUsers.getPhone();
                            Log.d(TAG, "SĐT: " + sdt);
                            binding.phoneTv.setText("SĐT: "+sdt);

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Lỗi khi truy xuất dữ liệu người bán", error.toException());
                    }
                });
    }


    // load thông tin hóa đơn
    private void loadOrderDetails() {
        Log.d(TAG, "loadOrderDetails: ");
        //load order details
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Orders");
        ref.child(orderId)
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
                        double latitude = Double.parseDouble(""+dataSnapshot.child("latitude").getValue());
                        double longitude = Double.parseDouble(""+dataSnapshot.child("longitude").getValue());
                        long timestamp = modelOrderUser.getTimestamp();


                        String dateFormated = Utils.formatTimestampDateTime(timestamp);

                        //order status
                        if (orderStatus.equals("Chưa thanh toán")){
                            binding.orderStatusTv.setTextColor(getResources().getColor(R.color.colorblack));
                        }
                        else if (orderStatus.equals("Đã thanh toán")){
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

                        Log.d(TAG, "onDataChange: mã hóa đơn: "+orderMaHD);
                        Log.d(TAG, "onDataChange: trạng thái đơn hàng: "+orderStatus);
                        Log.d(TAG, "onDataChange: ngày tháng năm: "+dateFormated);

                        findAddress(String.valueOf(latitude), String.valueOf(longitude)); //to find delivery address
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void findAddress(String latitude, String longitude) {
        double lat = Double.parseDouble(latitude);
        double lon = Double.parseDouble(longitude);

        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(lat, lon, 1);

            //complete address
            String address = addresses.get(0).getAddressLine(0);
            binding.addressTv.setText(address);
        }
        catch (Exception e){
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


}