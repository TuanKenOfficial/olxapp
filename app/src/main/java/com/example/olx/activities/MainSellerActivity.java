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
import com.example.olx.fragment.ChatsFragment;
import com.example.olx.fragment.HomeSellerFragment;
import com.example.olx.R;
import com.example.olx.Utils;
import com.example.olx.databinding.ActivityMainSellerBinding;
import com.example.olx.fragment.NotificationFragment;
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

import java.util.HashMap;

/** @noinspection deprecation*/
public class MainSellerActivity extends AppCompatActivity {

    private ActivityMainSellerBinding binding;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private static final String TAG ="MainSeller";
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainSellerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Vui lòng đợi trong giây lát");
        progressDialog.setCanceledOnTouchOutside(false);
        showHomeFragment(); //Home Fragment
        checkUser();


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
                        Utils.toast(MainSellerActivity.this,"Bạn cần đăng nhập tài khoản");
                        startActivity(new Intent(MainSellerActivity.this,LoginOptionActivity.class));
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
                        Utils.toast(MainSellerActivity.this,"Bạn cần đăng nhập tài khoản");
                        startActivity(new Intent(MainSellerActivity.this,LoginOptionActivity.class));
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
                        Utils.toast(MainSellerActivity.this,"Bạn cần đăng nhập tài khoản");
                        startActivity(new Intent(MainSellerActivity.this,LoginOptionActivity.class));
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

        binding.sellFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainSellerActivity.this, ShopAdCreateActivity.class);
                intent.putExtra("isEditMode", false);
                startActivity(intent);
            }
        });
        //popup menu
        final PopupMenu popupMenu = new PopupMenu(MainSellerActivity.this, binding.moreBtn);
        //add menu items to our menu
        popupMenu.getMenu().add("Cài đặt");
        popupMenu.getMenu().add("Đánh giá");
        popupMenu.getMenu().add("Doanh thu");
        //handle menu item click
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            if (menuItem.getTitle() == "Cài đặt"){
                //start settings screen
                Utils.toast(MainSellerActivity.this,"Chức năng đang phát triển");
            }
            else if (menuItem.getTitle() == "Đánh giá"){
                //open same reviews activity as used in user main page
                Utils.toast(MainSellerActivity.this,"Chức năng đang code, đợi clip sau");
                Intent intent = new Intent(MainSellerActivity.this, ShopReviewsActivity.class);
                intent.putExtra("shopUid", ""+firebaseAuth.getUid());
                startActivity(intent);
            }
            else if (menuItem.getTitle() == "Doanh thu"){
                //start promotions list screen
                Utils.toast(MainSellerActivity.this,"Chức năng đang code, đợi clip sau");
                startActivity(new Intent(MainSellerActivity.this, DoanhThuSellerActivity.class));
            }

            return true;
        });

        //show more options: Settings, Reviews, Promotion Codes
        binding.moreBtn.setOnClickListener(view -> {
            //show popup menu
            popupMenu.show();
        });
    }



    private void checkUser() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null){
            Utils.toastyInfo(MainSellerActivity.this,"Bạn chưa đăng nhập");
        }
        else {
            showHomeFragment();
            loadSellerProfile();
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
                                        Utils.toast(MainSellerActivity.this,"Tạo token chat thành công");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "onFailure: Lỗi update token lên csdl: "+e);
                                        Utils.toastyError(MainSellerActivity.this, "Lỗi update toke lên csdl");
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Utils.toastyError(MainSellerActivity.this,"Lỗi Token chat");
                        Log.d(TAG, "onFailure: Lỗi: "+e);
                    }
                });
    }
    private void loadSellerProfile() {
        Log.d(TAG, "loadSellerProfile: ");
        String registerUserUid = firebaseAuth.getUid();
        Log.d(TAG, "loadSellerProfile: registerUserUid:"+registerUserUid);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(registerUserUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String name =""+snapshot.child("name").getValue();
                        Log.d(TAG, "onDataChange: name"+name);
                        String shopName =""+snapshot.child("shopName").getValue();
                        Log.d(TAG, "onDataChange: shopName"+shopName);
                        String email =""+snapshot.child("email").getValue();
                        Log.d(TAG, "onDataChange: email"+email);
                        String phone =""+snapshot.child("phone").getValue();
                        Log.d(TAG, "onDataChange: phone"+phone);
                        String profileImage =""+snapshot.child("profileImage").getValue();
                        Log.d(TAG, "onDataChange: profileImage"+profileImage);
                        String accountType =""+snapshot.child("accountType").getValue();
                        Log.d(TAG, "onDataChange: accountType"+accountType);
                        binding.nameTv.setText("Tên:"+name);
                        binding.shopNameTv.setText("Tên Shop:"+shopName);
                        binding.emailTv.setText("Email:"+email);
                        binding.phoneTv.setText("SĐT:"+phone);
                        binding.accountTv.setText("Tài khoản:"+accountType);

                        //profileImage
                        try {
                            Glide.with(MainSellerActivity.this)
                                    .load(profileImage)
                                    .placeholder(R.drawable.olx_trangbia)
                                    .into(binding.profileIv);
                        }catch (Exception e){
                            Log.d(TAG, "onBindViewHolder: "+e);
                            Utils.toastyError(MainSellerActivity.this,""+e);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void showProfileFragment() {
        binding.toolbarRl.setVisibility(View.GONE);
        Utils.toast(MainSellerActivity.this,"Profile Seller");
        ProfileSellerFragment fragment = new ProfileSellerFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.fragmentsFl.getId(), fragment, "ProfileSellerFragment");
        fragmentTransaction.commit();

    }

    private void showFavFragment() {
        binding.toolbarRl.setVisibility(View.GONE);
        Utils.toast(MainSellerActivity.this,"Thông báo");
        NotificationFragment fragment = new NotificationFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.fragmentsFl.getId(), fragment, "NotificationFragment");
        fragmentTransaction.commit();

    }

    private void showChatsFragment() {
        binding.toolbarRl.setVisibility(View.GONE);
        Utils.toast(MainSellerActivity.this,"Chats");
        ChatsFragment fragment = new ChatsFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.fragmentsFl.getId(), fragment, "ChatsFragment");
        fragmentTransaction.commit();
    }

    private void showHomeFragment() {
        binding.toolbarRl.setVisibility(View.VISIBLE);
        HomeSellerFragment fragment = new HomeSellerFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.fragmentsFl.getId(), fragment, "HomeSellerFragment");
        fragmentTransaction.commit();
    }
}