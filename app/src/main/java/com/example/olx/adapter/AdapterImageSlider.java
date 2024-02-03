package com.example.olx.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.olx.R;
import com.example.olx.databinding.RowImageSliderBinding;
import com.example.olx.model.ModelImageSlider;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;

public class AdapterImageSlider extends RecyclerView.Adapter<AdapterImageSlider.HolderImageSlider> {

    private RowImageSliderBinding binding;

    private  static  final  String TAG ="IMAGE_SLIDER";
    private Context context;
    private ArrayList<ModelImageSlider> imageSliderArrayList;


    public AdapterImageSlider(Context context, ArrayList<ModelImageSlider> imageSliderArrayList) {
        this.context = context;
        this.imageSliderArrayList = imageSliderArrayList;
    }

    @NonNull
    @Override
    public HolderImageSlider onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowImageSliderBinding.inflate(LayoutInflater.from(context), parent, false);
        return new HolderImageSlider(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderImageSlider holder, int position) {
        ModelImageSlider modelImageSlider = imageSliderArrayList.get(position);

        String imageUrl = modelImageSlider.getImageUrl();

        String imageCount = (position + 1) +"/" +imageSliderArrayList.size();

        holder.imageCountIv.setText(imageCount);

        Log.d(TAG, "onBindViewHolder: "+imageCount);

        try {
            Log.d(TAG, "onBindViewHolder: Load Image" +imageUrl);
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.image)
                    .into(holder.imageIv);
        }catch (Exception e){
            Log.e(TAG, "onBindViewHolder: ",e);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return imageSliderArrayList.size();
    }

    public  class  HolderImageSlider extends RecyclerView.ViewHolder{
        ShapeableImageView imageIv;

        TextView imageCountIv;


        public HolderImageSlider(@NonNull View itemView) {
            super(itemView);

            imageIv = binding.imageIv;
            imageCountIv = binding.imageCountTv;
        }
    }
}