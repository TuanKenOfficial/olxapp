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
import com.example.olx.databinding.ActivityDeleteAccountBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DeleteAccountActivity extends AppCompatActivity {
    private ActivityDeleteAccountBinding binding;

    private static final String TAG = "DELETE_ACCOUNT";

    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDeleteAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Vui lòng đợi");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        //quay lại
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                reference.child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String accountType = "" + snapshot.child("accountType").getValue();
                        Log.d(TAG, "onDataChange: " + accountType);
                        if (accountType.equals("User")) {
                            Log.d(TAG, "onDataChange: User:" + accountType);
                            startActivity(new Intent(DeleteAccountActivity.this, MainUserActivity.class));
                            finishAffinity();
                        } else if (accountType.equals("Seller")) {
                            Log.d(TAG, "onDataChange: Seller:" + accountType);
                            startActivity(new Intent(DeleteAccountActivity.this, MainSellerActivity.class));
                            finishAffinity();
                        } else {
                            Log.d(TAG, "onDataChange: Tài khoản còn lai" + accountType);
                            startActivity(new Intent(DeleteAccountActivity.this, MainUserActivity.class));
                            finishAffinity();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        binding.deleteAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                reference.child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String accountType = "" + snapshot.child("accountType").getValue();
                        Log.d(TAG, "onDataChange: " + accountType);
                        if (accountType.equals("User")) {
                            Log.d(TAG, "onDataChange: User:" + accountType);

                            deleteAccount();
                        } else if (accountType.equals("Seller")) {
                            Log.d(TAG, "onDataChange: Seller:" + accountType);
                            deleteAccount();
                        } else {
                            Log.d(TAG, "onDataChange: Tài khoản còn lai" + accountType);
                            deleteAccount();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        });

    }

    private void deleteAccount() {
        Log.d(TAG, "deleteAccount: ");

        progressDialog.setMessage("Xóa tài khoản");
        progressDialog.show();

        String myUid = firebaseAuth.getUid();
        Log.d(TAG, "deleteAccount: " + myUid);

        firebaseUser.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: ");
                        progressDialog.setMessage("Xóa tài khoản user Ads");
                        deleteProductProfile();
                        deleteProfile();
                        //Xóa tài khoản user quảng cáo, hiện tại chưa làm việc tới nó nẽ lưu Db->Ads->AdsId

                    }

                    private void deleteProductProfile() {
                        Log.d(TAG, "deleteProductProfile: ");
                        DatabaseReference refAd = FirebaseDatabase.getInstance().getReference("ProductAds");
                        refAd.orderByChild("uid").equalTo(myUid)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for (DataSnapshot ds : snapshot.getChildren()) {
                                            ds.getRef().removeValue();
                                            Log.d(TAG, "onDataChange: " + ds);
                                            progressDialog.setMessage("Xóa tài khoản user data");
                                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                                            ref.child(myUid).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    Log.d(TAG, "onSuccess: Thành công");
                                                    progressDialog.dismiss();
                                                    Utils.toastySuccess(DeleteAccountActivity.this, "Xoá tài khoản thành công");
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.d(TAG, "onFailure: Thất bại" + e);
                                                    progressDialog.dismiss();
                                                    Utils.toastyError(DeleteAccountActivity.this, "Thất bại");
                                                }
                                            });
                                        }


                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                    }

                    private void deleteProfile() {
                        Log.d(TAG, "deleteProfile: ");
                        progressDialog.setMessage("Xóa tài khoản user data");
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                        ref.child(myUid).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Log.d(TAG, "onSuccess: Thành công");
                                progressDialog.dismiss();
                                Utils.toastySuccess(DeleteAccountActivity.this, "Xoá tài khoản thành công");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "onFailure: Thất bại" + e);
                                progressDialog.dismiss();
                                Utils.toastyError(DeleteAccountActivity.this, "Thất bại");
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Log.d(TAG, "onSuccess: Thất bại");
                        Utils.toastyError(DeleteAccountActivity.this, "Thất bại");
                    }
                });
    }

}