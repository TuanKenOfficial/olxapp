package com.example.olx.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.olx.Utils;
import com.example.olx.databinding.ActivityLoginOptionBinding;

public class LoginOptionActivity extends AppCompatActivity {

    private ActivityLoginOptionBinding binding;
    private static final String TAG="LOGINOPTION";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginOptionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.LoginEmailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(TAG, "onClick: Bạn đăng nhập email");
                Utils.toast(LoginOptionActivity.this,"Bạn đang vào trang đăng nhập email");
                startActivity(new Intent(LoginOptionActivity.this, LoginActivity.class));

            }
        });

        //đăng nhập google
        binding.LoginGoogleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(TAG, "onClick: Bạn đăng nhập google");
                Utils.toast(LoginOptionActivity.this,"Bạn đang vào trang đăng nhập google");
                //giải quyet tình trạng không đăng nhập chọn lại tài khoản google
                startActivity(new Intent(LoginOptionActivity.this, LoginGoogleActivity.class));

            }
        });

        //đăng nhập số điện thoại
        binding.LoginPhoneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(TAG, "onClick: Bạn đăng nhập sđt");
                Utils.toast(LoginOptionActivity.this,"Bạn đang vào trang đăng nhập số điện thoại");
                startActivity(new Intent(LoginOptionActivity.this, LoginPhoneActivity.class));

            }
        });

        binding.RegisterEmailSellerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Bạn đăng ký Email");
                startActivity(new Intent(LoginOptionActivity.this, RegisterSellerActivity.class));
                Utils.toast(LoginOptionActivity.this,"Chuyển sang trang đăng ký Email");
            }
        });

        binding.RegisterEmailUserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Bạn đăng ký Email");
                startActivity(new Intent(LoginOptionActivity.this, RegisterUserActivity.class));
                Utils.toast(LoginOptionActivity.this,"Chuyển sang trang đăng ký Email");
            }
        });


        binding.img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: OP");
                Utils.toastyInfo(LoginOptionActivity.this,"Bạn chưa đăng nhập, nên sẽ không dùng được chức năng");
                startActivity(new Intent(LoginOptionActivity.this, MainUserActivity.class));
            }
        });
    }
}