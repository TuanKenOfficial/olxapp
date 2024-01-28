package com.example.olx.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.olx.R;
import com.example.olx.Utils;
import com.example.olx.databinding.RowImagesCreateBinding;
import com.example.olx.model.ModelImagePicked;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class AdapterImagePicked extends RecyclerView.Adapter<AdapterImagePicked.HolderImagePicked>{
    private RowImagesCreateBinding binding;

    private static final String TAG ="ROWIMAGE";

    private Context context;

    private ArrayList<ModelImagePicked> modelImagePickedArrayList;

    private String adId ="";

    public AdapterImagePicked(Context context, ArrayList<ModelImagePicked> modelImagePickedArrayList, String adId) {
        this.context = context;
        this.modelImagePickedArrayList = modelImagePickedArrayList;
        this.adId = adId;
    }

    @NonNull
    @Override
    public AdapterImagePicked.HolderImagePicked onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowImagesCreateBinding.inflate(LayoutInflater.from(context),parent,false);
        return new HolderImagePicked(binding.getRoot());
    }
    @SuppressLint("RecyclerView")
    @Override
    public void onBindViewHolder(@NonNull AdapterImagePicked.HolderImagePicked holder,  int position) {
        ModelImagePicked model = modelImagePickedArrayList.get(position);

        if (model.getFromInternet()){
            String imageUrl = model.getImageUrl();

            Log.d(TAG, "onBindViewHolder: imageUrl: "+imageUrl);

            try {
                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.image)
                        .into(holder.imageIv);
            }catch (Exception e){
                Log.d(TAG, "onBindViewHolder: "+e);
                Utils.toastyError(context,""+e);
            }
        }
        else {
            Uri imageUri = model.getImageUri();

            Log.d(TAG, "onBindViewHolder: imageUri: "+imageUri);
            try {
                Glide.with(context)
                        .load(imageUri)
                        .placeholder(R.drawable.image)
                        .into(holder.imageIv);
            }catch (Exception e){
                Log.d(TAG, "onBindViewHolder: "+e);
                Utils.toastyError(context,"Lỗi: "+e);
            }
        }



        holder.closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (model.getFromInternet()){
                    deleteImage(model,holder,position);
                    Log.d(TAG, "onClick: deleteImage");
                }
                else {
                    Log.d(TAG, "onClick: Xóa ảnh khi đăng");
                    modelImagePickedArrayList.remove(model);
                    notifyItemChanged(position);
                }

            }
        });
    }

    private void deleteImage(ModelImagePicked model, HolderImagePicked holder, int position) {
        String idImageAd = model.getId();

        Log.d(TAG, "deleteImage: adId:" +adId);
        Log.d(TAG, "deleteImage: imageId:" +idImageAd);
        Log.d(TAG, "deleteImage: imageUrl" +model.getImageUrl());

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Ads");
        ref.child(adId).child("Images").child(idImageAd)
                .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: Xóa thành công");
                        Utils.toastySuccess(context, "Xóa thành công hình ảnh");

                        try {
                            Log.d(TAG, "onSuccess: Xóa");
                            modelImagePickedArrayList.remove(model);
                            notifyItemChanged(position);
                        }
                        catch (Exception e){
                            Log.d(TAG, "onSuccess: ", e);

                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Utils.toastyError(context, "Xóa thất bại");
                        Log.d(TAG, "onFailure: "+e);

                    }
                });
    }

    @Override
    public int getItemCount() {
        return modelImagePickedArrayList.size();
    }

    public class HolderImagePicked extends RecyclerView.ViewHolder{

        ImageView imageIv;
        ImageButton closeBtn;
        public HolderImagePicked(@NonNull View itemView) {
            super(itemView);

            imageIv = binding.imageIv;
            closeBtn = binding.closeBtn;
        }
    }
}
