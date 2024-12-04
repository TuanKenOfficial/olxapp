package com.example.olx.adapter;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.olx.CurrencyFormatter;
import com.example.olx.FilterOrderSeller;
import com.example.olx.R;
import com.example.olx.Utils;
import com.example.olx.activities.DoanhThuSellerActivity;
import com.example.olx.activities.ShopOrderSellerDetailActivity;
import com.example.olx.activities.ShopOrderUserDetailActivity;
import com.example.olx.databinding.RowOderSellerBinding;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class AdapterOrderSeller extends RecyclerView.Adapter<AdapterOrderSeller.HolderOrderSeller> implements Filterable {

    private RowOderSellerBinding binding;
    private final Context context;
    public ArrayList<ModelOrderSeller> orderSellerArrayList, filterList;
    private FilterOrderSeller filter;

    private FirebaseAuth firebaseAuth;


    private static final String TAG ="AOS";

    public AdapterOrderSeller(Context context, ArrayList<ModelOrderSeller> orderSellerArrayList) {
        this.context = context;
        this.orderSellerArrayList = orderSellerArrayList;
        this.filterList = orderSellerArrayList;

    }

    @NonNull
    @Override
    public HolderOrderSeller onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        binding = RowOderSellerBinding.inflate(LayoutInflater.from(context), parent, false);
        return new AdapterOrderSeller.HolderOrderSeller(binding.getRoot());

    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull HolderOrderSeller holder, int position) {
        //get data at position
        ModelOrderSeller modelOrderSeller = orderSellerArrayList.get(position);
        String orderId = modelOrderSeller.getOrderId();
        long orderMaHD = modelOrderSeller.getOrderMaHD();
        String orderBy = modelOrderSeller.getOrderBy();
        String address = modelOrderSeller.getAddress();
        String sanpham = modelOrderSeller.getSanpham();
        int soluong = modelOrderSeller.getSoluong();
        int orderTongTien = modelOrderSeller.getOrderTongTien();
        String orderStatus = modelOrderSeller.getOrderStatus();
        long timestamp = modelOrderSeller.getTimestamp();
        String orderTo = modelOrderSeller.getOrderTo();
        Log.d(TAG, "onBindViewHolder: orderBy: "+modelOrderSeller.getOrderBy());

        firebaseAuth = FirebaseAuth.getInstance();
        //load
        loadUserInfo(modelOrderSeller, holder);
        loadOrderUserInfo(modelOrderSeller, holder);

        //set data
        String formatted = Utils.formatTimestampDateTime(timestamp); // load dd/MM/yyyy HH:mm
        holder.ngayDat.setText("Thời gian đặt hàng: "+formatted);
        holder.maHD.setText("Hoá đơn: #"+orderMaHD);
        holder.soluongSPDD.setText("Số lượng sản phẩm đã đặt: "+soluong);
        holder.titleSP.setText("Sản phẩm: "+sanpham);
        holder.diachi.setText("Địa chỉ: "+address);
        holder.tongHoaDon.setText("Tổng cộng: "+ CurrencyFormatter.getFormatter().format(Double.parseDouble(String.valueOf(orderTongTien))));
        holder.statusTv.setText("Trạng thái: "+orderStatus);


        holder.itemView.setOnClickListener(v -> {
            //open order details
            Intent intent = new Intent(context, ShopOrderUserDetailActivity.class);
            intent.putExtra("orderId", orderId); //để tải thông tin id hóa đơn đơn hàng
            intent.putExtra("orderTo", orderTo); //để tải thông tin của người bán
            intent.putExtra("orderBy", orderBy); //để tải thông tin của người mua
            context.startActivity(intent);
            Log.d(TAG, "onBindViewHolder:orderTo: "+orderTo);
            Log.d(TAG, "onBindViewHolder: orderBy: "+orderBy);//để tải thông tin của người bán đã đặt hàng


        });
        holder.doanhthuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open order details
                Intent intent = new Intent(context, DoanhThuSellerActivity.class);
                intent.putExtra("orderId", orderId); //để tải thông tin đơn hàng
                intent.putExtra("orderBy", orderBy); //để tải thông tin của người dùng đã đặt hàng
                intent.putExtra("orderTo", orderTo); //để tải thông tin của người bán đã đặt hàng
                context.startActivity(intent);
            }
        });

    }


    //load tên nguời mua
    private void loadOrderUserInfo(ModelOrderSeller modelOrderSeller, AdapterOrderSeller.HolderOrderSeller holder) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(modelOrderSeller.getOrderBy()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String name = ""+snapshot.child("name").getValue();
                Log.d(TAG, "onDataChange: tên người mua: "+name);
                holder.tenNM.setText("Người mua: "+name);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void loadUserInfo(ModelOrderSeller modelOrderSeller, HolderOrderSeller holder) {
        //to load email of the user/buyer: modelOrderShop.getOrderBy() contains uid of that user/buyer
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(modelOrderSeller.getOrderBy())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        String sdt = ""+dataSnapshot.child("phone").getValue();
                        Log.d(TAG, "onDataChange: sdt"+sdt);
                        holder.sdt.setText("Số điện thoại: "+sdt);
                        String email = ""+dataSnapshot.child("email").getValue();
                        holder.email.setText("Email: "+email);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return orderSellerArrayList.size(); //return size of list / number of records
    }

    @Override
    public Filter getFilter() {
        if (filter == null){
            //init filter
            filter = new FilterOrderSeller(this, filterList);
        }
        return filter;
    }

    //view holder class for row_order_seller.xml
    public  class   HolderOrderSeller extends RecyclerView.ViewHolder{

        //ui views of row_order_seller.xml
        public  TextView maHD, tenNM, soluongSPDD, sdt, diachi,
                titleSP,ngayDat,tongHoaDon,statusTv,email;
        public Button doanhthuBtn;
        public ImageButton editBtn;
        public HolderOrderSeller(@NonNull View itemView) {
            super(itemView);

            //init ui views
            maHD = binding.maHD;
            tenNM = binding.tenNM;
            soluongSPDD = binding.soluongSPDD;
            sdt = binding.sdt;
            email = binding.email;
            diachi = binding.diachi;
            titleSP = binding.titleSP;
            ngayDat = binding.ngayDat;
            tongHoaDon = binding.tongHoaDon;
            statusTv = binding.statusTv;
            doanhthuBtn = binding.doanhthuBtn;
            editBtn = binding.editBtn;
        }
    }
}