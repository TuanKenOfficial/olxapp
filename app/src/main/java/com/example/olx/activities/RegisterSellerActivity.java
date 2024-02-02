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

public class RegisterSellerActivity extends AppCompatActivity implements LocationListener {

    private ActivityRegisterSellerBinding binding;
    private static final String TAG = "RSSeller";
    private FirebaseAuth firebaseAuth;
    //permission constants
    private static final int LOCATION_REQUEST_CODE = 100;
    //permission arrays
    private String[] locationPermissions;
    private double latitude = 0.0, longitude = 0.0;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterSellerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Authencation
        firebaseAuth = FirebaseAuth.getInstance();
        //init permissions array
        locationPermissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
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

        binding.gpsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkLocationPermission()) {
                    //already allowed
                    detectLocation();
                    Log.d(TAG, "onClick: detectLocation");
                } else {
                    //not allowed, request
                    requestLocationPermission();
                    Log.d(TAG, "onClick: requestLocationPermission");
                }
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
            binding.passwordEt.setError("Mật khẩu chưa nhập");
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
        if (latitude == 0.0 || longitude == 0.0) {
            binding.address.setError("Vui lòng nhấp vào nút GPS để phát hiện vị trí...");
            binding.address.requestFocus();
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
        hashMap.put("email", "" + email);
        hashMap.put("name", "" + fullName);
        hashMap.put("shopName", "" + shopName);
        hashMap.put("phone", "" + phoneNumber);
        hashMap.put("address", "" + address);
        hashMap.put("latitude", latitude);
        hashMap.put("longitude", longitude);
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

    private void detectLocation() {
        Utils.toast(RegisterSellerActivity.this, "Vui lòng đợi trong giây lát...");

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }


    //Lấy địa chỉ
    private void findAddress() {
        //find address, country, state, city
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);

            String address = addresses.get(0).getAddressLine(0); //complete address

            //set addresses
            binding.address.setText(address);

        } catch (Exception e) {
            Utils.toastyError(RegisterSellerActivity.this, "Lỗi: " + e.getMessage());
        }
    }

    private boolean checkLocationPermission() {

        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) ==
                (PackageManager.PERMISSION_GRANTED);
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, locationPermissions, LOCATION_REQUEST_CODE);
    }

    //điểm đầu - cuối vị trí
    @Override
    public void onLocationChanged(@NonNull Location location) {
        //location detected
        latitude = location.getLatitude();
        longitude = location.getLongitude();

        findAddress();
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        LocationListener.super.onProviderEnabled(provider);
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        LocationListener.super.onProviderDisabled(provider);
        Utils.toastyInfo(RegisterSellerActivity.this, "Vui lòng bật vị trí trên điện thoại...");
    }

    //xử lý quyền location
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (locationAccepted) {
                        //permission allowed
                        detectLocation();
                    } else {
                        //permission denied
                        Utils.toastyInfo(RegisterSellerActivity.this, "Quyền vị trí là cần thiết...");
                    }
                }
            }
            break;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }


}

