package com.example.olx.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;

import com.example.olx.R;
import com.example.olx.Utils;
import com.example.olx.databinding.ActivityLoginBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/** @noinspection ALL*/
public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private static final String TAG ="LOGIN";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Authencation
        firebaseAuth = FirebaseAuth.getInstance();
        //dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Vui lòng đợi");
        progressDialog.setCanceledOnTouchOutside(false);
        
        //quay về màn hình
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //xử lý nút đăng ký người mua
        binding.noLoginUserTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alers = new AlertDialog.Builder(LoginActivity.this);
                //thiết lập tiêu đề:
                alers.setTitle("Đăng ký");
                //thiết lập icon:
                alers.setIcon(R.drawable.ic_phone);
                //thiết lập nội dung cho dialog:
                alers.setMessage("Bạn đang vào trang đăng ký tài khoản email người mua?");
                //thiết lập các nút lệnh để người dùng tương tác:
                alers.setPositiveButton("Có", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Utils.toastySuccess(LoginActivity.this,"Vui lòng đợi");
                        startActivity(new Intent(LoginActivity.this, RegisterUserActivity.class));
                    }
                });
                alers.setNegativeButton("Không", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                //tạo cửa sổ Dialog:
                AlertDialog dialog=alers.create();
                dialog.setCanceledOnTouchOutside(false);
                //hiển thị cửa sổ này lên:
                dialog.show();
            }
        });

        //xử lý nút đăng ký người bán
        binding.noLoginSellerTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.toastySuccess(LoginActivity.this,"Vui lòng đợi");
                startActivity(new Intent(LoginActivity.this, RegisterSellerActivity.class));
            }
        });

        binding.noPasswordTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //quên mật khẩu
            }
        });
        //xử lý nút đăng nhập
        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputData();
            }
        });


    }
    private String email, password;
    private void inputData() {
        email = binding.emailEt.getText().toString().trim();
        password = binding.passwordEt.getText().toString().trim();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.emailEt.setError("Email không đúng");
            binding.emailEt.requestFocus();
        }
        else if (password.isEmpty()){
            binding.passwordEt.setError("Mật khẩu chưa nhập");
            binding.passwordEt.requestFocus();
        }
        else if (password.length()<6){
            binding.passwordEt.setError("Mật khẩu bắt buộc 6 số");
            binding.passwordEt.requestFocus();
        }
        else{
            Log.d(TAG, "inputData: ");
            loginUser();
        }
    }

    private void loginUser() {
        Log.d(TAG, "loginUser: ");
        progressDialog.setMessage("Đăng nhập");
        progressDialog.show();

        firebaseAuth.signInWithEmailAndPassword(email,password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {

                        Log.d(TAG, "onSuccess: Đăng nhập thành công...");
                        progressDialog.dismiss();
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                        reference.child(firebaseAuth.getUid())
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        String accountType = ""+snapshot.child("accountType").getValue();
                                        Log.d(TAG, "onDataChange: "+accountType);
                                        Utils.toastyInfo(LoginActivity.this,"accountType: "+accountType);
                                        if (accountType.equals("User")){
                                            Log.d(TAG, "onDataChange: User");
                                            Utils.toastySuccess(LoginActivity.this, "Đăng nhập thành công");
                                            startActivity(new Intent(LoginActivity.this, MainUserActivity.class));
                                            finishAffinity();
                                        }
                                        else if (accountType.equals("Seller")){
                                            Log.d(TAG, "onDataChange: Seller");
                                            Utils.toastySuccess(LoginActivity.this, "Đăng nhập thành công");
                                            startActivity(new Intent(LoginActivity.this, MainSellerActivity.class));
                                            finishAffinity();
                                        }
                                        else if (accountType.equals("Google")){
                                            Log.d(TAG, "onDataChange: Google");
                                            Utils.toastySuccess(LoginActivity.this, "Đăng nhập thành công");
                                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                            finishAffinity();
                                        }
                                        else{
                                            Log.d(TAG, "onDataChange: Phone");
                                            Utils.toastySuccess(LoginActivity.this, "Đăng nhập thành công");
                                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                            finishAffinity();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: ",e );
                        Utils.toast(LoginActivity.this,"Email chưa đăng ký hoặc mật khẩu không đúng");
                        Utils.toastyError(LoginActivity.this,"Lỗi: xin đăng nhập lại bằng tài khoản khác "+e);
                        progressDialog.dismiss();
                    }
                });
    }
}