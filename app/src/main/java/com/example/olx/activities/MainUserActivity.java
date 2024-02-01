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
import com.example.olx.fragment.HomeSellerFragment;
import com.example.olx.fragment.HomeUserFragment;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
/** @noinspection deprecation*/
public class MainUserActivity extends AppCompatActivity {

    private ActivityMainUserBinding binding;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private static final String TAG ="MaiUser";
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
        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeMeOffline();
               
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

        binding.sellFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userTypes ;
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                reference.child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String accountType = ""+snapshot.child("accountType").getValue();
                        Log.d(TAG, "onDataChange: "+accountType);
                        if (accountType.equals("User")){
                            Utils.toastyInfo(MainUserActivity.this, "tài khoản của bạn không dùng được chức năng này");
                        }
                        else {
                            Intent intent = new Intent(MainUserActivity.this, AdCreateActivity.class);
                            intent.putExtra("isEditMode", false);
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        });
        //popup menu
        final PopupMenu popupMenu = new PopupMenu(MainUserActivity.this, binding.moreBtn);
        //add menu items to our menu
        popupMenu.getMenu().add("Cài đặt");
        popupMenu.getMenu().add("Reviews");
        popupMenu.getMenu().add("Khuyến mãi");
        //handle menu item click
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            if (menuItem.getTitle() == "Cài đặt"){
                //start settings screen
                Utils.toast(MainUserActivity.this,"Chức năng đang code, đợi clip sau");
//                startActivity(new Intent(MainSellerActivity.this, SettingsActivity.class));
            }
            else if (menuItem.getTitle() == "Reviews"){
                //open same reviews activity as used in user main page
                Utils.toast(MainUserActivity.this,"Chức năng đang code, đợi clip sau");
//                Intent intent = new Intent(MainSellerActivity.this, ShopReviewsActivity.class);
//                intent.putExtra("shopUid", ""+firebaseAuth.getUid());
//                startActivity(intent);
            }
            else if (menuItem.getTitle() == "Khuyến mãi"){
                //start promotions list screen
                Utils.toast(MainUserActivity.this,"Chức năng đang code, đợi clip sau");
//                startActivity(new Intent(MainSellerActivity.this, PromotionCodesActivity.class));
            }

            return true;
        });

        //show more options: Settings, Reviews, Promotion Codes
        binding.moreBtn.setOnClickListener(view -> {
            //show popup menu
            popupMenu.show();
        });
//        loadUserProfile();
    }

    private void makeMeOffline() {
        //after logging in, make user online
        progressDialog.setMessage("Logging Out...");

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("online","false");

        //update value to db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(aVoid -> {
                    //update successfully
                    firebaseAuth.signOut();
                    checkUser();
                })
                .addOnFailureListener(e -> {
                    //failed updating
                    progressDialog.dismiss();
                    Utils.toastyError(MainUserActivity.this,""+e.getMessage());

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
            loadUserProfile();
        }
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
                                    .placeholder(R.drawable.image)
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
        Utils.toast(MainUserActivity.this,"Chức năng đang code, đợi clip sau");
    }

    private void showFavFragment() {
        Utils.toast(MainUserActivity.this,"Chức năng đang code, đợi clip sau");
    }

    private void showChatsFragment() {
        Utils.toast(MainUserActivity.this,"Chức năng đang code, đợi clip sau");
    }

    private void showHomeFragment() {
        HomeUserFragment fragment = new HomeUserFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.fragmentsFl.getId(), fragment, "HomeUserFragment");
        fragmentTransaction.commit();
    }
}