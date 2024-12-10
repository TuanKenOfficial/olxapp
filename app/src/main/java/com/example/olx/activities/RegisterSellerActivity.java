package com.example.olx.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;

import com.example.olx.Utils;
import com.example.olx.databinding.ActivityRegisterSellerBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class RegisterSellerActivity extends AppCompatActivity {

    private ActivityRegisterSellerBinding binding;
    private static final String TAG = "RegisterSeller";
    private FirebaseAuth firebaseAuth;
    //permission constants

    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterSellerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Authencation
        firebaseAuth = FirebaseAuth.getInstance();
        //init permissions array
        //dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Vui lòng đợi");
        progressDialog.setCanceledOnTouchOutside(false);

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        binding.registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputData();
            }
        });

    }

    private String fullName;
    private String shopName;
    private String phoneNumber;
    private String address;
    private String email;
    private String password;

    private void inputData() {
        //input data
        fullName = binding.name.getText().toString().trim();
        shopName = binding.shop.getText().toString().trim();
        phoneNumber = binding.phone.getText().toString().trim();
        address = binding.address.getText().toString().trim();
        email = binding.emailEt.getText().toString().trim();
        password = binding.passwordEt.getText().toString().trim();
        String confirmPassword = binding.confirmpasswordEt.getText().toString().trim();

        Log.d(TAG, "inputData: fullName"+fullName);
        Log.d(TAG, "inputData: shopName"+shopName);
        Log.d(TAG, "inputData: phoneNumber"+phoneNumber);
        Log.d(TAG, "inputData: address"+address);
        Log.d(TAG, "inputData: email"+email);
        Log.d(TAG, "inputData: password"+password);
        Log.d(TAG, "inputData: confirmPassword"+confirmPassword);
        //validate data
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailEt.setError("Email không đúng!! Email hợp lệ @gmail.com");
            binding.emailEt.requestFocus();
        }
        if (password.length() < 6) {
            binding.passwordEt.setError("Mật khẩu chưa nhập, phải 6 số trờ lên");
            binding.passwordEt.requestFocus();
        } else if (!password.equals(confirmPassword)) {
            binding.confirmpasswordEt.setError("Mật khẩu không khớp");
            binding.confirmpasswordEt.requestFocus();
        }
        if (TextUtils.isEmpty(fullName)) {
            binding.name.setError("Nhập họ và tên hay tên");
            binding.name.requestFocus();
        }
        if (TextUtils.isEmpty(shopName)) {
            binding.shop.setError("Nhập tên cửa hàng");
            binding.shop.requestFocus();
        }
        if (TextUtils.isEmpty(phoneNumber)) {
            binding.phone.setError("Nhập số điện thoại");
            binding.phone.requestFocus();
        } else if (phoneNumber.length() < 10) {
            binding.phone.setError("Số điện thoại phải 10 số");
            binding.phone.requestFocus();
        } else if (phoneNumber.length() > 10) {
            binding.phone.setError("Số điện thoại phải 10 số");
            binding.phone.requestFocus();
        }
        if ( address.isEmpty()) {
            binding.address.setError("Vui lòng nhập vị trí cửa hàng...");
            binding.address.requestFocus();
            Utils.toast(RegisterSellerActivity.this,"Địa chỉ: xã-phường/huyện-thành phố/tỉnh");
        }
        else {
            Log.d(TAG, "inputData: ");
            createAccount();
        }


    }

    private void createAccount() {
        progressDialog.setMessage("Tạo tài khoản...");
        progressDialog.show();
        Log.d(TAG, "createAccount: ");

        //create account
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    //account created
                    saverFirebaseData();
                    Log.d(TAG, "createAccount: saverFirebaseData");
                })
                .addOnFailureListener(e -> {
                    //failed creating account
                    progressDialog.dismiss();
                    Utils.toastyError(RegisterSellerActivity.this, "Lỗi: " + e.getMessage());
                });
    }

    private void saverFirebaseData() {
        Log.d(TAG, "saverFirebaseData: ");
        progressDialog.setMessage("Lưu thông tin tài khoản...");

        final String timestamp = "" + System.currentTimeMillis();
        String registerUserEmail = firebaseAuth.getCurrentUser().getEmail();
        String registerUserUid = firebaseAuth.getUid();
        //setup data to save
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", "" + registerUserUid);
        hashMap.put("email", "" + registerUserEmail);
        hashMap.put("name", "" + fullName);
        hashMap.put("shopName", "" + shopName);
        hashMap.put("phone", "" + phoneNumber);
        hashMap.put("address", "" + address);
        hashMap.put("latitude", 0.0);
        hashMap.put("longitude", 0.0);
        hashMap.put("timestamp", "" + timestamp);
        hashMap.put("accountType", "Seller");
        hashMap.put("online", "true");
        hashMap.put("shopOpen", "true");
        hashMap.put("profileImage", "https://firebasestorage.googleapis.com/v0/b/olxs-36d58.appspot.com/o/olx_trangbia.png?alt=media&token=84013b87-58da-401a-aa0d-cfced0202739");
        hashMap.put("dob", "Email");

        //save to db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(registerUserUid).setValue(hashMap)
                .addOnSuccessListener(aVoid -> {
                    //db updated
                    progressDialog.dismiss();
                    Utils.toastySuccess(RegisterSellerActivity.this, "Tạo tài khoản thành công");
                    startActivity(new Intent(RegisterSellerActivity.this, LoginActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    //failed updating db
                    progressDialog.dismiss();
                    Utils.toastyError(RegisterSellerActivity.this, "Lỗi");
                    finish();
                });

    }

}

