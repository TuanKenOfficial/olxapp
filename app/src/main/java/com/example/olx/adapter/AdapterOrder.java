package com.example.olx.adapter;

import static android.os.Build.VERSION_CODES.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.olx.CurrencyFormatter;
import com.example.olx.databinding.RowOrderBinding;
import com.example.olx.model.ModelOrder;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;

public class AdapterOrder extends RecyclerView.Adapter<AdapterOrder.HolderOrder> {

    private RowOrderBinding binding;
    private final Context context;
    private final ArrayList<ModelOrder> orderArrayList;

    public AdapterOrder(Context context, ArrayList<ModelOrder> orderArrayList) {
        this.context = context;
        this.orderArrayList = orderArrayList;
    }


    @NonNull
    @Override
    public AdapterOrder.HolderOrder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        binding = RowOrderBinding.inflate(LayoutInflater.from(context), parent, false);
        return new AdapterOrder.HolderOrder(binding.getRoot());
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull AdapterOrder.HolderOrder holder, int position) {

        //get data at position
        ModelOrder modelOrder = orderArrayList.get(position);
        String productAdsId = modelOrder.getProductAdsId();
        String ten = modelOrder.getTen();
        int tongtien = modelOrder.getTongtien();
        int price = modelOrder.getPrice();
        int soluongdadat = modelOrder.getSoluongdadat();

        //set data
        holder.titleTv.setText(ten);
        holder.priceTv.setText(CurrencyFormatter.getFormatter().format(Double.parseDouble(String.valueOf(price))));
        holder.finalPriceTv.setText(CurrencyFormatter.getFormatter().format(Double.parseDouble(String.valueOf(tongtien))));
        holder.sQuantityTv.setText(""+soluongdadat);

    }

    @Override
    public int getItemCount() {
        return orderArrayList.size(); //return list size
    }

    //view holder class
    public class HolderOrder extends RecyclerView.ViewHolder{

        //views of row_ordereditem.xml
        public TextView titleTv, sQuantityTv, priceTv, finalPriceTv;
        public ShapeableImageView productIv;
        public HolderOrder(@NonNull View itemView) {
            super(itemView);

            //init views
            productIv = binding.productIv;
            titleTv = binding.titleTv;
            sQuantityTv = binding.sQuantityTv;
            priceTv = binding.priceTv;
            finalPriceTv = binding.finalPriceTv;

        }
    }
}
