package com.example.olx.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.olx.R;
import com.example.olx.Utils;
import com.example.olx.databinding.ActivityChangePasswordBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ChangePasswordActivity extends AppCompatActivity {
    private ActivityChangePasswordBinding binding;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private static final String TAG = "ChangePassword";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChangePasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Vui lòng đợi trong giây lát");
        progressDialog.setCanceledOnTouchOutside(false);

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                reference.child(firebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String accountType = ""+snapshot.child("accountType").getValue();
                        Log.d(TAG, "onDataChange: "+accountType);
                        if (accountType.equals("User")){
                            Log.d(TAG, "onDataChange: User:"+accountType);
                            startActivity(new Intent(ChangePasswordActivity.this, MainUserActivity.class));
                            finishAffinity();
                        } else if (accountType.equals("Seller")) {
                            Log.d(TAG, "onDataChange: Seller:"+accountType);
                            startActivity(new Intent(ChangePasswordActivity.this, MainSellerActivity.class));
                            finishAffinity();
                        }
                        else {
                            Log.d(TAG, "onDataChange: Tài khoản còn lai"+accountType);
                            startActivity(new Intent(ChangePasswordActivity.this, MainUserActivity.class));
                            finishAffinity();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
        binding.changePasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                reference.child(firebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String accountType = ""+snapshot.child("accountType").getValue();
                        Log.d(TAG, "onDataChange: "+accountType);
                        if (accountType.equals("User")){
                            Log.d(TAG, "onDataChange: "+accountType);
                            validate();

                        } else if (accountType.equals("Seller")) {
                            Log.d(TAG, "onDataChange: "+accountType);
                            validate();
                        }
                        else {
                            Log.d(TAG, "onDataChange: "+accountType);
                           Utils.toastyInfo(ChangePasswordActivity.this,"Tài khoản đăng nhập google và đăng nhập bằng phone, không thể đổi mật khẩu");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        });
    }

    private String currentPassword = "", newpassword = "", cPassword = "";

    private void validate() {
        currentPassword = binding.currentPasswordEt.getText().toString().trim();
        newpassword = binding.passwordEt.getText().toString().trim();
        cPassword = binding.cPasswordEt.getText().toString().trim();

        Log.d(TAG, "validate: currentPassword" + currentPassword);
        Log.d(TAG, "validate: currentPassword" + newpassword);
        Log.d(TAG, "validate: currentPassword" + cPassword);

        if (currentPassword.isEmpty()) {
            binding.currentPasswordEt.setError("Vui lòng nhập mật khẩu cũ");
            binding.currentPasswordEt.requestFocus();
        } else if (newpassword.isEmpty()) {
            binding.passwordEt.setError("Vui lòng nhập mật khẩu mới của bạn");
            binding.passwordEt.requestFocus();
        } else if (cPassword.isEmpty()) {
            binding.passwordEt.setError("Vui lòng nhập lại mật khẩu mới của bạn");
            binding.passwordEt.requestFocus();
        } else if (!newpassword.equals(cPassword)) {
            binding.cPasswordEt.setError("Mật khẩu mới bạn nhập không khớp của bạn");
            binding.cPasswordEt.requestFocus();
        } else {
            authenticateUserForUpdatePassword();
        }

    }

    private void authenticateUserForUpdatePassword() {
        Log.d(TAG, "authenticateUserForUpdatePassword: ");

        progressDialog.setMessage("Tài khoản User");
        progressDialog.show();

        AuthCredential authCredential = EmailAuthProvider.getCredential(firebaseUser.getEmail(), currentPassword);
        firebaseUser.reauthenticate(authCredential)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        updatePassword();
                        Log.d(TAG, "onSuccess: ");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: " + e);
                        Utils.toastyError(ChangePasswordActivity.this, "Lỗi" + e);
                        progressDialog.dismiss();

                    }
                });
    }

    private void updatePassword() {
        Log.d(TAG, "updatePassword: ");

        progressDialog.setMessage("Update mật khẩu mới");
        progressDialog.show();

        firebaseUser.updatePassword(newpassword)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Log.d(TAG, "onSuccess: Update thành công mật khẩu mới");
                        Utils.toastySuccess(ChangePasswordActivity.this, "Update thành công mật khẩu mới");

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Log.d(TAG, "onFailure: " + e);
                        Utils.toastyError(ChangePasswordActivity.this, "Lỗi" + e);

                    }
                });
    }
}