package com.example.olx.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.olx.CurrencyFormatter;
import com.example.olx.R;
import com.example.olx.databinding.RowCartBinding;
import com.example.olx.model.ModelAddProduct;
import com.example.olx.model.ModelCart;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class AdapterCart extends RecyclerView.Adapter<AdapterCart.HolderCart> {

    private RowCartBinding binding;
    private  static  final  String TAG ="ADAPTER_GIO";
    private Context context;
    private ArrayList<ModelCart> cartArrayList;
    private ArrayList<ModelAddProduct> addProductArrayList;

    public AdapterCart(Context context, ArrayList<ModelCart> cartArrayList) {
        this.context = context;
        this.cartArrayList = cartArrayList;
    }

    @NonNull
    @Override
    public AdapterCart.HolderCart onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowCartBinding.inflate(LayoutInflater.from(context), parent, false);
        return new AdapterCart.HolderCart(binding.getRoot());
    }
    @SuppressLint("RecyclerView")
    @Override
    public void onBindViewHolder(@NonNull AdapterCart.HolderCart holder,  int position) {
        ModelCart modelCart = cartArrayList.get(position);
        int idGH = modelCart.getId();
        String title = modelCart.getTenSP();
        int Soluongdadat = modelCart.getQuantity();
        int tongtiensp = modelCart.getTongtienSP();


        Log.d(TAG, "onBindViewHolder: idGH: "+idGH);
        Log.d(TAG, "onBindViewHolder: title: "+title);
        Log.d(TAG, "onBindViewHolder: Soluongdadat: "+Soluongdadat);
        Log.d(TAG, "onBindViewHolder: tongtiensp: "+tongtiensp);
        // lấy hình ảnh đầu tiên của sản phẩm
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("ProductAds");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String idProductAds = ""+snapshot.child("id").getValue();
                Log.d(TAG, "onDataChange: idProductAds"+idProductAds);
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ProductAds");
                ref.child(idProductAds).child("Images").limitToFirst(1)
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot ds : snapshot.getChildren()) {
                                    String imageUrl = "" + ds.child("imageUrl").getValue();
                                    Log.d(TAG, "onDataChange: imageUrl:"+imageUrl);
                                    try {
                                        Glide.with(context)
                                                .load(imageUrl)
                                                .placeholder(R.drawable.image)
                                                .into(holder.productIv);
                                    }catch (Exception e){
                                        Log.e(TAG, "onBindViewHolder: ",e);
                                    }

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        holder.titleTv.setText(title);
        holder.sQuantityTv.setText("Số lượng đã đặt: "+Soluongdadat);
        holder.finalPriceTv.setText("Tổng tiền: "+ CurrencyFormatter.getFormatter().format(Double.valueOf(tongtiensp)));



        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //suy nghĩ code thêm
                EasyDB easyDB = EasyDB.init(context, "GIOHANG_DB")
                        .setTableName("GIOHANG_TABLE")
                        .addColumn(new Column("GH_Id", "text", "unique"))
                        .addColumn(new Column("GH_PID", "text", "not null"))
                        .addColumn(new Column("GH_Title", "text", "not null"))
                        .addColumn(new Column("GH_Price", "text", "not null"))
                        .addColumn(new Column("GH_Quantity", "text", "not null"))
                        .addColumn(new Column("GH_FinalPrice", "text", "not null"))
                        .addColumn(new Column("GH_UidNguoiBan", "text", "not null"))
                        .addColumn(new Column("GH_UidNguoiMua", "text", "not null"))
                        .doneTableColumn();

                easyDB.deleteRow(1, idGH); //column Number 1 is Item_Id
                Toast.makeText(context, "Đã xóa khỏi giỏ hàng...", Toast.LENGTH_SHORT).show();

                //refresh list
                cartArrayList.remove(position);
                notifyItemChanged(position);
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartArrayList.size();
    }

    public class HolderCart  extends RecyclerView.ViewHolder{
        ShapeableImageView productIv;

        TextView titleTv,sQuantityTv,raitoTv,finalPriceTv;
        ImageButton deleteBtn;
        public HolderCart(@NonNull View itemView) {
            super(itemView);
            productIv = binding.productIv;
            titleTv = binding.titleTv;
            sQuantityTv = binding.sQuantityTv;
            finalPriceTv = binding.finalPriceTv;
            deleteBtn = binding.deleteBtn;

        }
    }
}
