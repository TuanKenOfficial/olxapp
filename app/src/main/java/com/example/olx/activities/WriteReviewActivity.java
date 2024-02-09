package com.example.olx.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.olx.R;
import com.example.olx.Utils;
import com.example.olx.databinding.ActivityWriteReviewBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Objects;

public class WriteReviewActivity extends AppCompatActivity {

    private ActivityWriteReviewBinding binding;

    private String shopUid;

    private FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWriteReviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        //get shop uid from intent
        shopUid = getIntent().getStringExtra("shopUid");

        firebaseAuth = FirebaseAuth.getInstance();
        //load shopf info: shop name, shop image
        loadShopInfo();
        //if user has written review to this shop, load it
        loadMyReview();

        //go back to previous activity
        binding.backBtn.setOnClickListener(v -> onBackPressed());

        //input data
        binding.submitBtn.setOnClickListener(v -> inputData());
    }

    private void loadShopInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //get shop info
                String shopName = ""+dataSnapshot.child("shopName").getValue();
                String shopImage = ""+dataSnapshot.child("profileImage").getValue();

                //set shop info to ui
                binding.shopNameTv.setText(shopName);
                try {
                    Picasso.get().load(shopImage).placeholder(R.drawable.shop).into(binding.profileIv);
                }
                catch (Exception e){
                    binding.profileIv.setImageResource(R.drawable.shop);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadMyReview() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).child("Ratings").child(Objects.requireNonNull(firebaseAuth.getUid()))
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            //my review is available in this shop

                            //get review details
                            String uid = ""+dataSnapshot.child("uid").getValue();
                            String ratings = ""+dataSnapshot.child("ratings").getValue();
                            String review = ""+dataSnapshot.child("review").getValue();
                            String timestamp = ""+dataSnapshot.child("timestamp").getValue();

                            //set review details to our ui
                            float myRating = Float.parseFloat(ratings);
                            binding.ratingBar.setRating(myRating);
                            binding.reviewEt.setText(review);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void inputData() {
        String ratings = ""+binding.ratingBar.getRating();
        String review = binding.reviewEt.getText().toString().trim();

        //for time of review
        String timestamp = ""+System.currentTimeMillis();

        //setup data in hashmap
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", ""+ firebaseAuth.getUid());
        hashMap.put("ratings", ""+ ratings); //e.g. 4.6
        hashMap.put("review", ""+ review); //e.g. Good service
        hashMap.put("timestamp", ""+ timestamp);

        //put to db: DB > Users > ShopUid > Ratings
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).child("Ratings").child(Objects.requireNonNull(firebaseAuth.getUid())).updateChildren(hashMap)
                .addOnSuccessListener(aVoid -> {
                    //review added to db
                    Utils.toastySuccess(WriteReviewActivity.this,"Bài đánh giá đã xuất bản thành công...");
                })
                .addOnFailureListener(e -> {
                    //failed adding review to db
                    Utils.toastyError(WriteReviewActivity.this, "Lỗi: "+e.getMessage());
                });
    }
}
