package com.example.olx.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.olx.Utils;
import com.example.olx.adapter.AdapterAddProduct;
import com.example.olx.databinding.ActivityMainSellerProfileBinding;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.olx.R;
import com.example.olx.model.ModelAddProduct;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainSellerProfileActivity extends AppCompatActivity {

    private ActivityMainSellerProfileBinding binding;
    private String sellerUid = "";

    private static final String TAG ="SELLER_PROFILE";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainSellerProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sellerUid = getIntent().getStringExtra("sellerUid");
        loadSellerDetails(); // load thông tin profile seller
        loadAds(); //load danh sách sản phẩm
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }
    private void loadAds() {
        ArrayList<ModelAddProduct> adArrayList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("ProductAds");
        reference.orderByChild("uid").equalTo(sellerUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        adArrayList.clear();

                        for (DataSnapshot ds : snapshot.getChildren()){
                            try {
                                ModelAddProduct modelAddProduct = ds.getValue(ModelAddProduct.class);
                                adArrayList.add(modelAddProduct);
                            }
                            catch (Exception e){
                                Log.e(TAG, "onDataChange: ",e);
                            }

                        }

                        AdapterAddProduct adapterAd = new AdapterAddProduct(MainSellerProfileActivity.this,adArrayList);
                        binding.adsRv.setAdapter(adapterAd);

                        String adAccount = ""+adArrayList.size();
                        binding.publishedAdsCountTv.setText(adAccount);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadSellerDetails() {
        Log.d(TAG, "loadSellerDetails: ");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(sellerUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String name = ""+snapshot.child("name").getValue();
                        String profileImageUrl = ""+snapshot.child("profileImageUrl").getValue();
                        String timestamp = ""+ snapshot.child("timestamp").getValue();

                        String formattedDate = Utils.formatTimestampDate(Long.valueOf(timestamp));

                        binding.sellerNameTv.setText(name);
                        binding.sellerMeberSinceTv.setText(formattedDate);

                        try {
                            Glide.with(MainSellerProfileActivity.this)
                                    .load(profileImageUrl)
                                    .placeholder(R.drawable.ic_users)
                                    .into(binding.sellerProfileIv);
                        }
                        catch (Exception e){
                            Utils.toastyError(MainSellerProfileActivity.this,"Lỗi hình ảnh");
                            Log.e(TAG, "onDataChange: ",e);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}