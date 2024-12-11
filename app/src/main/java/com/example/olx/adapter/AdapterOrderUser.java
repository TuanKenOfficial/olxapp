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
import com.example.olx.FilterOrderUser;
import com.example.olx.Utils;

import com.example.olx.activities.ShopOrderUserDetailActivity;
import com.example.olx.databinding.RowOrderUserBinding;
import com.example.olx.model.ModelOrderUser;
import com.example.olx.model.ModelUsers;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdapterOrderUser extends RecyclerView.Adapter<AdapterOrderUser.HolderOrderUser> implements Filterable {

    private RowOrderUserBinding binding;
    private final Context context;
    public ArrayList<ModelOrderUser> orderUserArrayList, filterList;
    private FilterOrderUser filter;
    private static final String TAG ="Order_User";
    private FirebaseAuth firebaseAuth;


    public AdapterOrderUser(Context context, ArrayList<ModelOrderUser> orderUserArrayList) {
        this.context = context;
        this.orderUserArrayList = orderUserArrayList;
        this.filterList = orderUserArrayList;
        firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public AdapterOrderUser.HolderOrderUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        binding = RowOrderUserBinding.inflate(LayoutInflater.from(context), parent, false);
        return new AdapterOrderUser.HolderOrderUser(binding.getRoot());
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull AdapterOrderUser.HolderOrderUser holder, int position) {
        //get data at position
        ModelOrderUser modelOrderUser = orderUserArrayList.get(position);
        String orderId = modelOrderUser.getOrderId();
        long orderMaHD = modelOrderUser.getOrderMaHD();
        String orderBy = modelOrderUser.getOrderBy();
        String address = modelOrderUser.getAddress();
        String sanpham = modelOrderUser.getSanpham();

        int soluong = modelOrderUser.getSoluong();
        int orderTongTien = modelOrderUser.getOrderTongTien();
        String orderStatus = modelOrderUser.getOrderStatus();
        long timestamp = modelOrderUser.getTimestamp();
        String orderTo = modelOrderUser.getOrderTo();

        Log.d(TAG, "onBindViewHolder: id: "+orderId);
        Log.d(TAG, "onBindViewHolder: orderTo: "+orderTo);
        //set data
        String formatted = Utils.formatTimestampDateTime(timestamp); // load dd/MM/yyyy HH:mm
        holder.ngayDat.setText("Thời gian đặt hàng: "+formatted);
        holder.maHD.setText("Hoá đơn: #"+orderMaHD);
        holder.soluongSP.setText("Số lượng sản phẩm đã đặt: "+soluong);
        holder.titleSP.setText("Sản phẩm: "+sanpham);
        holder.diachi.setText("Địa chỉ: "+address);
        holder.tongHoaDon.setText("Tổng cộng: "+ CurrencyFormatter.getFormatter().format(Double.parseDouble(String.valueOf(orderTongTien))));
        holder.statusTv.setText("Trạng thái: "+orderStatus);
//        holder.tenNB.setText("Người bán: "+orderTo);
        //load user/buyer info
        loadUserInfo(holder, modelOrderUser);
        loadSellerInfo(holder, modelOrderUser);



        holder.itemView.setOnClickListener(v -> {
            //open order details
            Intent intent = new Intent(context, ShopOrderUserDetailActivity.class);
            intent.putExtra("orderId", orderId); //để tải thông tin id hóa đơn đơn hàng
            intent.putExtra("orderTo", orderTo); //để tải thông tin của người bán
            intent.putExtra("orderBy", orderBy); //để tải thông tin của người mua
            context.startActivity(intent);
        });

    }

    //load tên người bán
    private void loadSellerInfo(HolderOrderUser holder, ModelOrderUser modelOrderUser) {
        Log.d(TAG, "loadSellerInfo: ");
            // Giả sử có một phương thức để lấy UID người bán

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(modelOrderUser.getOrderTo())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                            ModelUsers modelUsers = userSnapshot.getValue(ModelUsers.class);
                            String name = modelUsers.getName();
                            Log.d(TAG, "Tên người bán: " + name);
                            holder.tenNB.setText("Người bán: "+name);
                            // Cập nhật giao diện hoặc thực hiện các thao tác khác với tên người bán
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Lỗi khi truy xuất dữ liệu người bán", error.toException());
                    }
                });
    }
    //load email and số điện thoại người mua
    private void loadUserInfo(HolderOrderUser holder, ModelOrderUser modelOrderUser) {
        Log.d(TAG, "loadUserInfo: ");
        //to load email of the user/buyer: modelOrderShop.getOrderBy() contains uid of that user/buyer
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(modelOrderUser.getOrderBy())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                            ModelUsers modelUsers = userSnapshot.getValue(ModelUsers.class);
                           
                            String name = modelUsers.getName();
                            Log.d(TAG, "Tên người mua: " + name);
                            holder.tenNM.setText("Người mua: "+name);

                            String email = modelUsers.getEmail();
                            Log.d(TAG, "Email: " + email);
                            holder.email.setText("Email: "+email);

                            String sdt = modelUsers.getPhone();
                            Log.d(TAG, "SĐT: " + sdt);
                            holder.sdt.setText("SĐT: "+sdt);
                          
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Lỗi khi truy xuất dữ liệu người bán", error.toException());
                    }
                });
    }

    @Override
    public int getItemCount() {
        return orderUserArrayList.size(); //return size of list / number of records
    }

    @Override
    public Filter getFilter() {
        if (filter == null){
            //init filter
            filter = new FilterOrderUser(this, filterList);
        }
        return filter;
    }

    //view holder class for row_order_seller.xml
    public class HolderOrderUser extends RecyclerView.ViewHolder{

        //ui views of row_order_seller.xml
        public TextView maHD, tenNM,tenNB, soluongSP, sdt, diachi,
                titleSP,ngayDat,tongHoaDon,statusTv,email;
        public HolderOrderUser(@NonNull View itemView) {
            super(itemView);

            //init ui views
            maHD = binding.maHD;
            tenNM = binding.tenNM;
            tenNB = binding.tenNB;
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