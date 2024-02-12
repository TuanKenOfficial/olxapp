package com.example.olx.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.olx.R;
import com.example.olx.adapter.AdapterReview;
import com.example.olx.databinding.ActivityShopReviewsBinding;
import com.example.olx.model.ModelReview;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ShopReviewsActivity extends AppCompatActivity {

    private ActivityShopReviewsBinding binding;

    private ArrayList<ModelReview> reviewArrayList; //will contain list of all reviews
    private AdapterReview adapterReview;

    private String shopUid;
    private static final String TAG = "Reviews";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityShopReviewsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //get shop uid from intent
        shopUid = getIntent().getStringExtra("shopUid");
        Log.d(TAG, "onCreate: "+shopUid);
        loadShopDetails(); //for shop name, image
        loadReviews(); //for reviews list, avg rating

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private float ratingSum = 0;
    private void loadReviews() {
        //init list
        reviewArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).child("Ratings")
                .addValueEventListener(new ValueEventListener() {
                    @SuppressLint({"SetTextI18n", "DefaultLocale"})
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //clear list before adding data into it
                        reviewArrayList.clear();
                        ratingSum = 0;
                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                            float rating = Float.parseFloat(""+ds.child("ratings").getValue()); //e.g. 4.3
                            ratingSum = ratingSum +rating; //for avg rating, add(addition of) all ratings, later will divide it by number of reviews

                            ModelReview modelReview = ds.getValue(ModelReview.class);
                            reviewArrayList.add(modelReview);
                        }
                        //setup adapter
                        adapterReview = new AdapterReview(ShopReviewsActivity.this, reviewArrayList);
                        //set to recyclerview
                        binding.reviewsRv.setAdapter(adapterReview);

                        long numberOfReviews = dataSnapshot.getChildrenCount();
                        float avgRating = ratingSum/numberOfReviews;

                        binding.ratingsTv.setText(String.format("%.2f", avgRating) + " [" +numberOfReviews+"]");//e.g. 4.7 [10]
                        binding.ratingBar.setRating(avgRating);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void loadShopDetails() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String shopName = ""+dataSnapshot.child("shopName").getValue();
                        String profileImage = ""+dataSnapshot.child("profileImage").getValue();

                        binding.shopNameTv.setText(shopName);
                        try {
                            Picasso.get().load(profileImage).placeholder(R.drawable.shop).into(binding.profileIv);
                        }
                        catch (Exception e){
                            //if anything goes wrong setting image (exception occurs), set default image
                            binding.profileIv.setImageResource(R.drawable.shop);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
}
