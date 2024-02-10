package com.example.olx.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.olx.CurrencyFormatter;
import com.example.olx.FilterOrderSeller;
import com.example.olx.R;
import com.example.olx.Utils;
import com.example.olx.activities.ShopOrderSellerDetailActivity;
import com.example.olx.databinding.RowOderSellerBinding;
import com.example.olx.model.ModelCart;
import com.example.olx.model.ModelOrderSeller;
import com.example.olx.model.ModelOrderUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdapterOrderSeller extends RecyclerView.Adapter<AdapterOrderSeller.HolderOrderSeller> implements Filterable {

    private RowOderSellerBinding binding;
    private final Context context;
    public ArrayList<ModelOrderSeller> orderSellerArrayList, filterList;
    private FilterOrderSeller filter;
    private static final String TAG ="Order_Seller";

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
        int orderTongTien = modelOrderSeller.getOrderTongTien();
        String orderStatus = modelOrderSeller.getOrderStatus();
        long timestamp = modelOrderSeller.getTimestamp();
        String orderTo = modelOrderSeller.getOrderTo();

        //load user/buyer info
        loadUserInfo(modelOrderSeller, holder);
        loadOrderInfo(modelOrderSeller,holder);
        loadOrderUserInfo(modelOrderSeller,holder);

        //set data
        String formatted = Utils.formatTimestampDateTime(timestamp); // load dd/MM/yyyy HH:mm
        holder.ngayDat.setText("Thời gian đặt hàng: "+formatted);
        holder.maHD.setText("Hoá đơn: #"+orderMaHD);
        holder.diachi.setText("Địa chỉ: "+address);
        holder.tongHoaDon.setText("Tổng cộng: "+ CurrencyFormatter.getFormatter().format(Double.parseDouble(String.valueOf(orderTongTien))));
        holder.statusTv.setText("Trạng thái: "+orderStatus);

        //change order status text color
        switch (orderStatus) {
            case "Chưa duyệt":
                holder.statusTv.setTextColor(context.getResources().getColor(R.color.colorblack));
                break;
            case "Đã duyệt":
                holder.statusTv.setTextColor(context.getResources().getColor(R.color.colorgold));
                break;
            case "Đã hủy":
                holder.statusTv.setTextColor(context.getResources().getColor(R.color.colorred));
                break;
        }

        holder.itemView.setOnClickListener(v -> {
            //open order details
            Intent intent = new Intent(context, ShopOrderSellerDetailActivity.class);
            intent.putExtra("orderId", orderId); //để tải thông tin đơn hàng
            intent.putExtra("orderBy", orderBy); //để tải thông tin của người dùng đã đặt hàng
            context.startActivity(intent);
        });

    }
    //load tên nguời mua
    private void loadOrderUserInfo(ModelOrderSeller modelOrderSeller, AdapterOrderSeller.HolderOrderSeller holder) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(modelOrderSeller.getOrderBy()).addValueEventListener(new ValueEventListener() {
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
    private void loadOrderInfo(ModelOrderSeller modelOrderSeller, HolderOrderSeller holder) {
        //load thông tin tên và số lượng đã đặt của sản phẩm
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(modelOrderSeller.getOrderTo()).child("Order").child(modelOrderSeller.getOrderId()).child("GioHang").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelCart modelCart = ds.getValue(ModelCart.class);
                    int Soluong = modelCart.getSoluongdadat();
                    String titleSP = modelCart.getTen();
                    Log.d(TAG, "onDataChange: số lượng: "+Soluong);
                    Log.d(TAG, "onDataChange: tên sản phẩm: "+titleSP);
                    holder.soluongSP.setText("Số lượng: "+Soluong);
                    holder.titleSP.setText("Sản phẩm gồm: "+titleSP);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadUserInfo(ModelOrderSeller modelOrderSeller, final HolderOrderSeller holder) {
        //to load email of the user/buyer: modelOrderShop.getOrderBy() contains uid of that user/buyer
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(modelOrderSeller.getOrderBy())
                .addValueEventListener(new ValueEventListener() {
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
        public  TextView maHD, tenNM, soluongSP, sdt, diachi,
                titleSP,ngayDat,tongHoaDon,statusTv,email;

        public HolderOrderSeller(@NonNull View itemView) {
            super(itemView);

            //init ui views
            maHD = binding.maHD;
            tenNM = binding.tenNM;
            soluongSP = binding.soluongSP;
            sdt = binding.sdt;
            email = binding.email;
            diachi = binding.diachi;
            titleSP = binding.titleSP;
            ngayDat = binding.ngayDat;
            tongHoaDon = binding.tongHoaDon;
            statusTv = binding.statusTv;
        }
    }
}