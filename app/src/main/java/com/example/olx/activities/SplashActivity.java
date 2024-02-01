package com.example.olx.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

import com.example.olx.Utils;
import com.example.olx.databinding.ActivitySplashBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SplashActivity extends AppCompatActivity {

    private ActivitySplashBinding binding;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private static  final  String TAG ="Splash";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance(); //Authentication

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Vui lòng đợi trong giây lát");
        progressDialog.setCanceledOnTouchOutside(false);


        TranslateAnimation animation = new TranslateAnimation(0,0,0,-1500);
        animation.setDuration(5000);
        animation.setFillAfter(false);
        animation.setAnimationListener(new MyAminationListener());
        binding.lv.setAnimation(animation);
        binding.txt.setText("@copyright Tuấn Ken Vlog");

        //start main screen after 2seconds
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
                checkUser(); //kiểm tra tình trạng đăng nhập

            }
        },8000);
    }
    // tạo hiệu ứng hình ảnh
    private class MyAminationListener implements Animation.AnimationListener{

        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            progressDialog.show();
            binding.lv.setVisibility(View.GONE);
            binding.img.setVisibility(View.VISIBLE);
            binding.lv.animate().alpha(1f).setDuration(1000);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }
    //kiểm tra người dùng đã login chưa
    private void checkUser() {
        //get current user, if logged in
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null){
            Log.d(TAG, "checkUser: firebaseUser == null");
            //user not logged in
            //start main screen
            startActivity(new Intent(SplashActivity.this, LoginOptionActivity.class));
            finish();//finish this activity
        }
        else {
            Log.d(TAG, "checkUser: ");
            //user logged in check user type, same as done in login screen
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseUser.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            //get người dùng
                            String accountType = ""+snapshot.child("accountType").getValue();
                            //check người dùng
                            if (accountType.equals("Google")){
                                //nếu người dùng là user thì vào trang người dùng
                                Utils.toast(SplashActivity.this,"Google");
                                Log.d(TAG, "onDataChange: Google");
                                startActivity(new Intent(SplashActivity.this, MainUserActivity.class));
                                finish();
                            }
                            else if (accountType.equals("Phone")){
                                //nếu người dùng là admin thì vào trang số điện thoai
                                Utils.toast(SplashActivity.this,"Phone");
                                Log.d(TAG, "onDataChange: Phone");
                                startActivity(new Intent(SplashActivity.this, MainUserActivity.class));
                                finish();
                            }
                            else if (accountType.equals("User")){
                                //nếu người dùng là admin thì vào trang admin
                                Log.d(TAG, "onDataChange: User");
                                startActivity(new Intent(SplashActivity.this, MainUserActivity.class));
                                finish();
                            }

                            else if (accountType.equals("Seller")){
                                Log.d(TAG, "onDataChange: Seller");
                                //nếu người dùng là admin thì vào trang admin
                                startActivity(new Intent(SplashActivity.this, MainSellerActivity.class));
                                finish();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }
    }
}