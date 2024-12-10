package com.example.olx.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import com.bumptech.glide.Glide;
import com.example.olx.R;
import com.example.olx.Utils;
import com.example.olx.databinding.ActivityMainUserBinding;
import com.example.olx.fragment.ChatsFragment;
import com.example.olx.fragment.HomeUserFragment;
import com.example.olx.fragment.NotificationFragment;
import com.example.olx.fragment.ProfileFragment;
import com.example.olx.fragment.ProfileSellerFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.HashMap;

/** @noinspection deprecation*/
public class MainUserActivity extends AppCompatActivity {

    private ActivityMainUserBinding binding;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private static final String TAG ="MainUser";
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Vui lòng đợi trong giây lát");
        progressDialog.setCanceledOnTouchOutside(false);
        showHomeFragment();
        checkUser();

        binding.sellFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (firebaseUser == null){
                    Utils.toast(MainUserActivity.this,"Bạn cần đăng nhập tài khoản");
                    startActivity(new Intent(MainUserActivity.this,LoginOptionActivity.class));

                }
                else{
                    Utils.toastyInfo(MainUserActivity.this, "Tài khoản không được phép");
                    Utils.toastyInfo(MainUserActivity.this, "Bạn phải đăng ký tài khoản người bán");
                }
            }
        });
        binding.bottomNv.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId== R.id.menu_home){
                    Log.d(TAG, "onNavigationItemSelected: Home");
                    //Home item click, Fragment Home
                    showHomeFragment();
                    return true;

                }
                else if (itemId == R.id.menu_chat){
                    //Home item click, Fragment Chat
                    if (firebaseUser == null){
                        Utils.toast(MainUserActivity.this,"Bạn cần đăng nhập tài khoản");
                        startActivity(new Intent(MainUserActivity.this,LoginOptionActivity.class));
                        return false;
                    }
                    else{
                        showChatsFragment();
                        return true;
                    }

                }
                else if (itemId == R.id.menu_fav){
                    //Home item click, Fragment Navigition
                    if (firebaseUser == null){
                        Utils.toast(MainUserActivity.this,"Bạn cần đăng nhập tài khoản");
                        startActivity(new Intent(MainUserActivity.this,LoginOptionActivity.class));
                        return false;
                    }
                    else{
                        showFavFragment();
                        return true;
                    }

                }
                else if (itemId == R.id.menu_person){
                    //Home item click, Fragment Profile
                    if (firebaseUser == null){
                        Utils.toast(MainUserActivity.this,"Bạn cần đăng nhập tài khoản");
                        startActivity(new Intent(MainUserActivity.this,LoginOptionActivity.class));
                        return false;
                    }
                    else{
                        showProfileFragment();
                        return true;
                    }

                }
                else {
                    return false;
                }

            }
        });


    }


    private void checkUser() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null){
            Log.d(TAG, "checkUser: user = null");
            Utils.toastyInfo(MainUserActivity.this,"Bạn chưa đăng nhập");
        }
        else {
            Log.d(TAG, "checkUser: user != null");
//            showHomeFragment();
            loadUserProfile();
            updateFCMToken();
        }
    }


    //update FCM Token thông báo chat
    private void updateFCMToken(){
        String myUid = ""+firebaseAuth.getUid();

        Log.d(TAG, "updateFCMToken: myUid: "+myUid);

        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String token) {
                        Log.d(TAG, "onSuccess: token: "+token);
                        HashMap<String,Object> hashMap = new HashMap<>();
                        hashMap.put("fcmToken",token);

                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");

                        ref.child(myUid)
                                .updateChildren(hashMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d(TAG, "onSuccess: Update thành công");
                                        Utils.toast(MainUserActivity.this,"Tạo token chat thành công");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "onFailure: Lỗi update token lên csdl: "+e);
                                        Utils.toastyError(MainUserActivity.this, "Lỗi update toke lên csdl");
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Utils.toastyError(MainUserActivity.this,"Lỗi Token chat");
                        Log.d(TAG, "onFailure: Lỗi: "+e);
                    }
                });
    }
    private void loadUserProfile() {
        Log.d(TAG, "loadSellerProfile: ");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String name =""+snapshot.child("name").getValue();
                        Log.d(TAG, "onDataChange: name"+name);
                        String email =""+snapshot.child("email").getValue();
                        Log.d(TAG, "onDataChange: email"+email);
                        String phone =""+snapshot.child("phone").getValue();
                        Log.d(TAG, "onDataChange: phone"+phone);
                        String profileImage =""+snapshot.child("profileImage").getValue();
                        Log.d(TAG, "onDataChange: profileImage"+profileImage);

                        String accountType =""+snapshot.child("accountType").getValue();
                        Log.d(TAG, "onDataChange: accountType"+accountType);

                        binding.nameTv.setText("Tên:"+name);
                        binding.emailTv.setText("Email:"+email);
                        binding.phoneTv.setText("SĐT:"+phone);
                        binding.accountTv.setText("Tài khoản:"+accountType);

                        //profileImage
                        try {
                            Glide.with(MainUserActivity.this)
                                    .load(profileImage)
                                    .placeholder(R.drawable.shop)
                                    .into(binding.profileIv);
                        }catch (Exception e){
                            Log.d(TAG, "onBindViewHolder: "+e);
                            Utils.toastyError(MainUserActivity.this,""+e);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void showProfileFragment() {
        binding.toolbarRl.setVisibility(View.GONE);
        Utils.toast(MainUserActivity.this,"Profile User");
        ProfileFragment fragment = new ProfileFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.fragmentsFl.getId(), fragment, "ProfileFragment");
        fragmentTransaction.commit();
    }

    private void showFavFragment() {
        binding.toolbarRl.setVisibility(View.GONE);
        Utils.toast(MainUserActivity.this,"Thông báo");
        NotificationFragment fragment = new NotificationFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.fragmentsFl.getId(), fragment, "NotificationFragment");
        fragmentTransaction.commit();
    }

    private void showChatsFragment() {
        binding.toolbarRl.setVisibility(View.GONE);
        Utils.toast(MainUserActivity.this,"Chats");
        ChatsFragment fragment = new ChatsFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.fragmentsFl.getId(), fragment, "ChatsFragment");
        fragmentTransaction.commit();
    }

    private void showHomeFragment() {
        binding.toolbarRl.setVisibility(View.VISIBLE);
        HomeUserFragment fragment = new HomeUserFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.fragmentsFl.getId(), fragment, "HomeUserFragment");
        fragmentTransaction.commit();
    }
}