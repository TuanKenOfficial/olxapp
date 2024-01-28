package com.example.olx.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.olx.Utils;
import com.example.olx.databinding.ActivityLoginPhoneBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class LoginPhoneActivity extends AppCompatActivity {

    private ActivityLoginPhoneBinding binding;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private static final String TAG = "LoginPhone";

    private PhoneAuthProvider.ForceResendingToken forceResendingToken;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    private String mVerificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginPhoneBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.phoneInputRl.setVisibility(View.VISIBLE);
        binding.otpInputRl.setVisibility(View.GONE);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Vui lòng đợi");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();
        phoneLoginCallBack(); // phone login call back
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //gửi SĐT
        binding.btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateData();
            }
        });

        //chưa gửi xác thực
        binding.resentOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resentOTP(forceResendingToken);
            }
        });

        //xác thực OTP
        binding.btnVeryfied.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String otp = binding.otpView.getOTP();
                Log.d(TAG, "onClick: OTP: "+otp);

                if (otp.isEmpty()){
                    Utils.toastyInfo(LoginPhoneActivity.this,"mã OTP không được bỏ trống");
                }
                else if (otp.length()<6){
                    Utils.toastyInfo(LoginPhoneActivity.this,"mã OTP là 6 số");
                }
                else {

                    verifyPhone(mVerificationId,otp);
                }
            }
        });


    }

    //xử lý xác thực số điện thoại
    private void verifyPhone(String mVerificationId, String otp) {
        Log.d(TAG, "verifyPhone: verificationId: "+mVerificationId);
        Log.d(TAG, "verifyPhone: otp: "+otp);

        progressDialog.setMessage("Xác thực OTP");
        progressDialog.show();

        PhoneAuthCredential credential =PhoneAuthProvider.getCredential(mVerificationId,otp);
        signinWithPhone(credential);
    }

    // xử lý đăng nhập số điện thoại credential
    private void signinWithPhone(PhoneAuthCredential credential) {
        Log.d(TAG, "signinWithPhone: ");
        progressDialog.setMessage("Đăng nhập số điện thoại");
        progressDialog.show();

        firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        if (authResult.getAdditionalUserInfo().isNewUser()){
                            progressDialog.dismiss();
                            Log.d(TAG, "onSuccess: Đang update dữ liệu và tạo tài khoản");
                            Utils.toastySuccess(LoginPhoneActivity.this, "Đang tạo tài khoản");
                            updateDatabase();
                        }
                        else {
                            progressDialog.dismiss();
                            Log.d(TAG, "onSuccess: Tài khoản đã có");
                            Utils.toastySuccess(LoginPhoneActivity.this, "Tài khoản số điện thoại đã đăng ký");
                            startActivity(new Intent(LoginPhoneActivity.this, MainActivity.class));
                        }

                    }


                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: Lỗi: ", e);
                    }
                });
    }

    private void updateDatabase() {
        progressDialog.setMessage("Update lên cơ sở dữ liệu");

        long timestamp = Utils.getTimestamp();
        String registerUserUid = firebaseAuth.getUid();


        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("name","Phone");
        hashMap.put("shopName", "shopName");
        hashMap.put("phoneCode",phoneCode);
        hashMap.put("phoneNumber",phoneNumber);
        hashMap.put("phone",phoneNumberWithCode);
        hashMap.put("profileImageUrl","");
        hashMap.put("timestamp",timestamp);
        hashMap.put("email", "olx@gmail.com");
        hashMap.put("uid", registerUserUid);
        hashMap.put("address", "5M26+XRW, Phong Hoà, Lai Vung, Đồng Tháp, Việt Nam");
        hashMap.put("latitude", "10.15655");
        hashMap.put("longitude", "105.66237");
        hashMap.put("online", true);
        hashMap.put("shopOpen", true);
        hashMap.put("accountType","Phone");


        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(registerUserUid)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Utils.toastySuccess(LoginPhoneActivity.this, "Tạo tài khoản thành công");
                        Log.d(TAG, "onSuccess: Thành công");
                        startActivity(new Intent(LoginPhoneActivity.this, MainActivity.class));
                        finishAffinity();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Utils.toastyError(LoginPhoneActivity.this,"Lỗi: "+e);
                        Log.d(TAG, "onFailure: Lỗi: "+e);
                    }
                });
    }

    //otp chưa được gửi resent
    private void resentOTP(PhoneAuthProvider.ForceResendingToken token) {
        Log.d(TAG, "resentOTP: resendToke: "+token);

        progressDialog.setMessage("Gửi lại OTP đến số điện thoại: "+phoneNumberWithCode);
        progressDialog.show();
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phoneNumberWithCode)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(LoginPhoneActivity.this)
                .setCallbacks(mCallbacks)
                .setForceResendingToken(token)
                .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
        
    }

    //người dùng nhập số điện thoại
    private String phoneCode ="", phoneNumber="", phoneNumberWithCode="";
    private void updateData() {
        phoneCode = binding.phoneCode.getSelectedCountryCodeWithPlus();
        phoneNumber = binding.phoneNumber.getText().toString().trim();
        phoneNumberWithCode = phoneCode+phoneNumber;

        Log.d(TAG, "updateData: phoneCode: "+phoneCode);
        Log.d(TAG, "updateData: phoneNumber: "+phoneNumber);
        Log.d(TAG, "updateData: phoneNumberWithCode: "+phoneNumberWithCode);

        if (phoneCode.isEmpty()){
            Utils.toastyInfo(LoginPhoneActivity.this,"Bạn chưa chọn vùng quốc gia!!");
        }

        if (phoneNumber.isEmpty()){
            Utils.toastyInfo(LoginPhoneActivity.this,"Số điện thoại đang trống!!");
        }
        else if (phoneNumber.length()<9){
            Utils.toastyInfo(LoginPhoneActivity.this,"Số điện thoại phải 9 số trở lên, cộng với đầu +84 hoặc nhập 10 số");
        }
        else if (phoneNumber.length()>10){
            Utils.toastyInfo(LoginPhoneActivity.this,"Số điện thoại không lớn hơn 10 số");
        }
        else {
            startPhone();
        }
    }

    private void startPhone() {
        Log.d(TAG, "startPhone: ");

        progressDialog.setMessage("Gửi OTP đến số điện thoại: "+phoneNumberWithCode);
        progressDialog.show();

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phoneNumberWithCode)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(LoginPhoneActivity.this)
                .setCallbacks(mCallbacks)
                .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void phoneLoginCallBack() {
        Log.d(TAG, "phoneLoginCallBack: ");

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                Log.d(TAG, "onVerificationCompleted: ");
                signinWithPhone(credential);
                Utils.toastyInfo(LoginPhoneActivity.this,"Thành công VerificationId");

            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                progressDialog.dismiss();
                Utils.toastyInfo(LoginPhoneActivity.this,"Lỗi VerificationId: "+e);
                Log.e(TAG, "onVerificationFailed: ",e );
            }

            @Override
            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                super.onCodeSent(verificationId, token);
                mVerificationId = verificationId;
                forceResendingToken =token ;

                progressDialog.dismiss();

                binding.phoneInputRl.setVisibility(View.INVISIBLE);
                binding.otpInputRl.setVisibility(View.VISIBLE);

                Utils.toastySuccess(LoginPhoneActivity.this, "OTP đã được gửi đến: "+phoneNumberWithCode);

                binding.txtOTP.setText("OTP đã được gửi đến: "+phoneNumberWithCode);
            }
        };
    }
}