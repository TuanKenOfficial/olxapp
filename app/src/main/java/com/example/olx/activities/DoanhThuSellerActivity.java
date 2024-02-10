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
    private int thang, nam, count;
    private String idHD = "";
    private static final String TAG = "DoanhThu";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDoanhThuSellerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        idHD = getIntent().getStringExtra("idHD");
        Log.d(TAG, "onCreate: idHD: " + idHD);
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
                onBackPressed();
            }
        });
    }

    private  int Soluong;
    private void xulyHoaDon() {
        Log.d(TAG, "xulyHoaDon: ");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("HoaDon");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                reference.child("" + firebaseAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String uid = "" + snapshot.getRef().getKey();
                        String accountType = "" + snapshot.child("accountType").getValue();
                        Log.d(TAG, "onDataChange: accountType: " + accountType);
                        Log.d(TAG, "onDataChange: uid: " + uid);

                        //Xét điều kiện nếu là tài khoản email và phone
                        if (accountType.equals("Seller")) {
                            Log.d(TAG, "onDataChange: Hoa don đang hiển thị");
                            String uid1 = uid;
                            Log.d(TAG, "onDataChange: uid1: " + uid1);
                            chonThoiGianEmail(uid1);

                        } else if (accountType.equals("Phone")) {
                            String uid2 = uid;
                            chonThoiGianPhone(uid2);
                            Log.d(TAG, "onDataChange: Hoa don đang hiển thị");
                            Log.d(TAG, "onDataChange: uid2: " + uid2);
                        } else if (accountType.equals("Google")) {
                            String uid3 = uid;
                            chonThoiGianGoogle(uid3);
                            Log.d(TAG, "onDataChange: Hoa don đang hiển thị");
                            Log.d(TAG, "onDataChange: uid3: " + uid3);
                        } else {
                            Utils.toast(DoanhThuSellerActivity.this, "Bạn là người mua nên không xem được doanh thu, vì bạn không được đăng bán hàng, xin cảm ơn!!");
                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void chonThoiGianGoogle(String uid3) {
        Log.d(TAG, "chonThoiGianPhone: ");
        count = 0;
        Calendar calendar = Calendar.getInstance();
        thang = calendar.get(Calendar.MONTH);
        nam = calendar.get(Calendar.YEAR);
        dialogFragment = MonthYearPickerDialogFragment.getInstance(thang, nam);
        dialogFragment = MonthYearPickerDialogFragment.getInstance(thang, nam, customTitle);
        dialogFragment.show(getSupportFragmentManager(), null);
        dialogFragment.setOnDateSetListener((year, monthOfYear) -> {
            String thoiGian = (monthOfYear + 1) + "/" + year;
            binding.chonThoiGian.setText(thoiGian);
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).child("Order").orderByChild("orderTo").equalTo(uid3).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    // lấy dl từ
                        ModelOrderSeller modelOrderSeller = snapshot.getValue(ModelOrderSeller.class);
                        Log.d(TAG, "onDataChange: 111");
                        int tongtien = modelOrderSeller.getOrderTongTien();
                        Log.d(TAG, "onDataChange: tongtien: " + tongtien);
                        long timestamp = modelOrderSeller.getTimestamp();
                        Log.d(TAG, "onDataChange: timestamp: " + timestamp);
                        DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("Users");
                        ref1.child(firebaseAuth.getUid()).child("Order").child("GioHang").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot ds1 : snapshot.getChildren()){
                                    ModelCart modelCart = ds1.getValue(ModelCart.class);
                                    Soluong = modelCart.getSoluongdadat();
                                    Log.d(TAG, "onDataChange: "+Soluong);
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        calendar.setTimeInMillis(timestamp);
                        String ngay_dat = DateFormat.format("MM/yyyy", calendar).toString();
                        if (thoiGian.equals(ngay_dat)) {
                            //xử lý đoạn code ở đây
                            Log.d(TAG, "onDataChange: " + thoiGian.equals(ngay_dat));
                            count = Soluong;
                            binding.doanhThu.setText(CurrencyFormatter.getFormatter().format(Double.valueOf(tongtien)));
                            binding.tongDonHang.setText(count + " đơn");
                        } else {
                            Log.d(TAG, "onDataChange: ");
                            count = 0;
                            binding.doanhThu.setText(CurrencyFormatter.getFormatter().format(Double.valueOf(tongtien)));
                            binding.tongDonHang.setText(count + " đơn");
                        }



                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        });
    }

    private void chonThoiGianPhone(String uid2) {

        Log.d(TAG, "chonThoiGianPhone: ");
        count = 0;
        Calendar calendar = Calendar.getInstance();
        thang = calendar.get(Calendar.MONTH);
        nam = calendar.get(Calendar.YEAR);
        dialogFragment = MonthYearPickerDialogFragment.getInstance(thang, nam);
        dialogFragment = MonthYearPickerDialogFragment.getInstance(thang, nam, customTitle);
        dialogFragment.show(getSupportFragmentManager(), null);
        dialogFragment.setOnDateSetListener((year, monthOfYear) -> {
            String thoiGian = (monthOfYear + 1) + "/" + year;
            binding.chonThoiGian.setText(thoiGian);
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
            reference.child(firebaseAuth.getUid()).child("Order").orderByChild("orderByTo").equalTo(uid2).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    // lấy dl từ
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        ModelOrderSeller modelOrderSeller = ds.getValue(ModelOrderSeller.class);
                        Log.d(TAG, "onDataChange: ");
                        int tongtien = modelOrderSeller.getOrderTongTien();
                        Log.d(TAG, "onDataChange: tongtien: " + tongtien);
                        long timestamp = modelOrderSeller.getTimestamp();
                        Log.d(TAG, "onDataChange: timestamp: " + timestamp);
                        ModelOrder modelOrder = ds.getValue(ModelOrder.class);
                        int Soluong = modelOrder.getSoluongdadat();
                        calendar.setTimeInMillis(timestamp);
                        String ngay_dat = DateFormat.format("MM/yyyy", calendar).toString();
                        if (thoiGian.equals(ngay_dat)) {
                            //xử lý đoạn code ở đây
                            Log.d(TAG, "onDataChange: ");
                            count = Soluong;
                            binding.doanhThu.setText(CurrencyFormatter.getFormatter().format(Double.valueOf(tongtien)));
                            binding.tongDonHang.setText(count + " đơn");
                        } else {
                            Log.d(TAG, "onDataChange: ");
                            count = 0;
                            binding.doanhThu.setText(CurrencyFormatter.getFormatter().format(Double.valueOf(tongtien)));
                            binding.tongDonHang.setText(count + " đơn");
                        }

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        });
    }

    //dành cho tài khoản email
    private void chonThoiGianEmail(String uid1) {

        Log.d(TAG, "chonThoiGianEmail: ");
        count = 0;
        Calendar calendar = Calendar.getInstance();
        thang = calendar.get(Calendar.MONTH);
        nam = calendar.get(Calendar.YEAR);
        dialogFragment = MonthYearPickerDialogFragment.getInstance(thang, nam, customTitle);
        dialogFragment.show(getSupportFragmentManager(), null);
        dialogFragment.setOnDateSetListener((year, monthOfYear) -> {
            String thoiGian = (monthOfYear + 1) + "/" + year;
            binding.chonThoiGian.setText(thoiGian);
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
            reference.child(firebaseAuth.getUid()).child("Order").orderByChild("orderByTo").equalTo(uid1).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Log.d(TAG, "onDataChange: 111");
                    for (DataSnapshot ds: snapshot.getChildren()){
                        // lấy dl từ
                        ModelOrderSeller modelOrderSeller = ds.getValue(ModelOrderSeller.class);
                        Log.d(TAG, "onDataChange: 222");
                        int tongtien = modelOrderSeller.getOrderTongTien();
                        Log.d(TAG, "onDataChange: tongtien: " + tongtien);
                        long timestamp = modelOrderSeller.getTimestamp();
                        Log.d(TAG, "onDataChange: timestamp: " + timestamp);
                        ModelCart modelCart = ds.getValue(ModelCart.class);
                        int Soluong = modelCart.getSoluongdadat();
                        calendar.setTimeInMillis(timestamp);
                        String ngay_dat = DateFormat.format("MM/yyyy", calendar).toString();
                        if (thoiGian.equals(ngay_dat)) {
                            //xử lý đoạn code ở đây
                            Log.d(TAG, "onDataChange: ");
                            count = Soluong;
                            binding.doanhThu.setText(CurrencyFormatter.getFormatter().format(Double.valueOf(tongtien)));
                            binding.tongDonHang.setText(count + " đơn");
                        } else {
                            Log.d(TAG, "onDataChange: ");
                            count = 0;
                            binding.doanhThu.setText(CurrencyFormatter.getFormatter().format(Double.valueOf(tongtien)));
                            binding.tongDonHang.setText(count + " đơn");
                        }
                    }


                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        });
    }
}