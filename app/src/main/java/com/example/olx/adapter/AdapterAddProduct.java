package com.example.olx.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.olx.CurrencyFormatter;
import com.example.olx.FilterAddProducts;
import com.example.olx.R;
import com.example.olx.Utils;
import com.example.olx.activities.ShopAdDetailsActivity;
import com.example.olx.databinding.RowAddproductBinding;
import com.example.olx.model.ModelAddProduct;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdapterAddProduct extends RecyclerView.Adapter<AdapterAddProduct.HolderAddProduct> implements Filterable {
    private RowAddproductBinding binding;
    private Context context;
    private FirebaseAuth firebaseAuth;
    public ArrayList<ModelAddProduct> adArrayList;
    private ArrayList<ModelAddProduct> filterList;

    private ProgressDialog progressDialog;

    private FilterAddProducts filterAddProducts;

    private static final String TAG = "Aproduct";

    public AdapterAddProduct(Context context, ArrayList<ModelAddProduct> adArrayList) {
        this.context = context;
        this.adArrayList = adArrayList;
        this.filterList = adArrayList;
        firebaseAuth = FirebaseAuth.getInstance();
    }



    @NonNull
    @Override
    public AdapterAddProduct.HolderAddProduct onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = com.example.olx.databinding.RowAddproductBinding.inflate(LayoutInflater.from(context), parent, false);
        return new HolderAddProduct(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterAddProduct.HolderAddProduct holder, int position) {
        Log.d(TAG, "onBindViewHolder: ");
        ModelAddProduct modelAd = adArrayList.get(position);

        String id = modelAd.getId();

        String title = modelAd.getTitle();
        String description = modelAd.getDescription();
        String address = modelAd.getAddress();
        String condition = modelAd.getCondition();
        int price = modelAd.getPrice();
        int quantity = modelAd.getQuantity();
        int raito = modelAd.getRaito();
        int reducedprice = modelAd.getReducedprice();
        long timestamp = modelAd.getTimestamp();
        String formattedDate = Utils.formatTimestampDate(timestamp);

        loadAdFirstImage(modelAd, holder);

        if (firebaseAuth.getCurrentUser() != null) {
            checkIsFavorites(modelAd, holder);
        }


        holder.titleTv.setText(title);
        holder.descriptionTv.setText(description);
        holder.addressTv.setText(address);
        holder.conditionTv.setText("Tình trạng: " + condition);
        holder.priceTv.setText(CurrencyFormatter.getFormatter().format(Double.valueOf(price)));
        ;
        holder.raitoTv.setText(raito + "%");
        holder.pricesTv.setText(CurrencyFormatter.getFormatter().format(Double.valueOf(reducedprice)));
        holder.dateTv.setText(formattedDate);
        ;

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentShopAd = new Intent(context, ShopAdDetailsActivity.class);
                intentShopAd.putExtra("adId", modelAd.getId());
                context.startActivity(intentShopAd);
            }
        });

//        holder.favBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                boolean favorite = modelAd.isFavorite();
//                if (favorite) {
//                    Utils.removeFavorite(context, id);
//                } else {
//                    Utils.addToFavorite(context, id);
//
//                }
//            }
//        });

    }

    private void checkIsFavorites(ModelAddProduct modelAd, HolderAddProduct holder) {
    }

    private void loadAdFirstImage(ModelAddProduct modelAd, HolderAddProduct holder) {
        Log.d(TAG, "loadAdFirstImage: ");
        String id = modelAd.getId();
        Log.d(TAG, "loadAdFirstImage: "+id);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ProductAds");
        ref.child(id).child("Images").limitToFirst(1)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String imageUrl = "" + ds.child("imageUrl").getValue();
                            Log.d(TAG, "onDataChange: "+imageUrl);
                            try {
                                Glide.with(context)
                                        .load(imageUrl)
                                        .placeholder(R.drawable.image)
                                        .into(holder.imageIv);
                            } catch (Exception e) {
                                Log.d(TAG, "onDataChange: Lỗi: " + e);
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return adArrayList.size();
    }
    @Override
    public Filter getFilter() {
        if (filterAddProducts == null) {
            filterAddProducts = new FilterAddProducts(this, filterList);
        }
        return filterAddProducts;
    }

    public class HolderAddProduct extends RecyclerView.ViewHolder{
        ShapeableImageView imageIv;
        TextView titleTv, descriptionTv, addressTv, conditionTv, raitoTv, pricesTv, priceTv, dateTv;

        ImageButton favBtn, otherBtn;

        public HolderAddProduct(@NonNull View itemView) {
            super(itemView);
            imageIv = binding.imageIv;
            titleTv = binding.titleTv;
            descriptionTv = binding.descriptionTv;
            addressTv = binding.addressTv;
            conditionTv = binding.conditionTv;
            raitoTv = binding.raitoTv;
            pricesTv = binding.pricesTv;
            priceTv = binding.priceTv;
            dateTv = binding.dateTv;
            favBtn = binding.favBtn;
            otherBtn = binding.ortherBtn;
        }
    }
}
