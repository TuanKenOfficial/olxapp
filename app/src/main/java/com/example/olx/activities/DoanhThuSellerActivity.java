package com.example.olx.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;

import com.example.olx.CurrencyFormatter;
import com.example.olx.Utils;
import com.example.olx.adapter.AdapterOrderSeller;
import com.example.olx.databinding.ActivityDoanhThuSellerBinding;
import com.example.olx.model.ModelCart;
import com.example.olx.model.ModelOrder;
import com.example.olx.model.ModelOrderSeller;
import com.github.dewinjm.monthyearpicker.MonthYearPickerDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

public class DoanhThuSellerActivity extends AppCompatActivity {
    private ActivityDoanhThuSellerBinding binding;
    private FirebaseAuth firebaseAuth;
    private String customTitle = "Chọn tháng/năm cần coi doanh thu";
    private MonthYearPickerDialogFragment dialogFragment = null;
    private ArrayList<ModelOrderSeller> modelOrderSellers;
    private int thang, nam ;
    private int tongtiens,count;
    private static final String TAG = "DoanhThu";
    private String orderId, orderBy,orderTo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDoanhThuSellerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //get data from intent
        orderId = getIntent().getStringExtra("orderId");
        orderBy = getIntent().getStringExtra("orderBy");
        orderTo = getIntent().getStringExtra("orderTo");//Người bán
        Log.d(TAG, "onCreate: idHD: " + orderId);
        Log.d(TAG, "onCreate: Người bán uid: " + orderBy);
        firebaseAuth = FirebaseAuth.getInstance();
        binding.chonThoiGian.setOnClickListener(view1 -> {

            xulyHoaDon();
        });
        //reset lai cac mat hang
        binding.xoaHet.setOnClickListener(view1 -> {
            binding.tongDonHang.setText("");
            binding.doanhThu.setText("");
            binding.chonThoiGian.setText("");
        });
        binding.thoat.setText("Quay về trang chủ");
        //quay về trang chủ
        binding.thoat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.tongDonHang.setText("");
                binding.doanhThu.setText("");
                binding.chonThoiGian.setText("");
                onBackPressed();
            }
        });
    }

    private void xulyHoaDon() {
        Log.d(TAG, "xulyHoaDon: ");
        Calendar calendar = Calendar.getInstance();
        thang = calendar.get(Calendar.MONTH);
        nam = calendar.get(Calendar.YEAR);
        MonthYearPickerDialogFragment dialogFragment = MonthYearPickerDialogFragment.getInstance(thang, nam);
        dialogFragment.show(getSupportFragmentManager(), null);
        dialogFragment.setOnDateSetListener((year, monthOfYear) -> {
            String thoiGian = "0" + (monthOfYear + 1) + "/" + year;
            binding.chonThoiGian.setText(thoiGian);
            truyVanThangNam(thoiGian);
        });
    }

    private void truyVanThangNam(String thoiGian) {
        Calendar calendar = Calendar.getInstance();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(orderTo).child("Order").child(orderId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                            // lấy dl từ csdl
                            int tongtien = Integer.parseInt(""+snapshot.child("orderTongTien").getValue());
                            long ngayDat = Long.parseLong(""+snapshot.child("timestamp").getValue());
                            Log.d(TAG, "onDataChange: tongtien"+tongtien);
                            Log.d(TAG, "onDataChange: ngayDat"+ngayDat);
                            calendar.clear();
                            calendar.setTimeInMillis(ngayDat);
                            // rồi chuyển đổi nó sang chuỗi
                            String ngay_dat = DateFormat.format("MM/yyyy", calendar).toString();
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                            reference.child(orderTo).child("Order").child(orderId).child("GioHang").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot ds: snapshot.getChildren()){
                                        ModelCart modelCart = ds.getValue(ModelCart.class);
                                        int Soluong = modelCart.getSoluongdadat();
                                        count = Soluong;
                                        Log.d(TAG, "onDataChange: "+count);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                            if (thoiGian.equals(ngay_dat)) {
                                int counts = count;
                                tongtiens = tongtien;
                                binding.doanhThu.setText("Số lượng sản phẩm: "+counts);
                                binding.tongDonHang.setText(CurrencyFormatter.getFormatter().format(Double.parseDouble(String.valueOf(tongtiens))));
                            } else  {
                                tongtiens = 0;
                                int countss = 0;
                                binding.doanhThu.setText(CurrencyFormatter.getFormatter().format(Double.parseDouble(String.valueOf(tongtiens))));
                                binding.tongDonHang.setText("Số lượng sản phẩm: "+countss);
                            }
                        }


                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }
}