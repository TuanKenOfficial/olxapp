package com.example.olx.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.olx.CurrencyFormatter;
import com.example.olx.R;
import com.example.olx.Utils;
import com.example.olx.activities.ShopAdDetailsActivity;
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
        String pId = modelCart.getProductAdsId();
        String title = modelCart.getTen();
        int Soluongdadat = modelCart.getSoluongdadat();
        int price = modelCart.getPrice();
        int tongtiensp = modelCart.getTongtien();
        String uidNguoiMua = modelCart.getUidNguoiMua();


        Log.d(TAG, "onBindViewHolder: idGH: "+idGH);
        Log.d(TAG, "onBindViewHolder: title: "+title);
        Log.d(TAG, "onBindViewHolder: Soluongdadat: "+Soluongdadat);
        Log.d(TAG, "onBindViewHolder: giá: "+price);
        Log.d(TAG, "onBindViewHolder: tongtiensp: "+tongtiensp);
        Log.d(TAG, "onBindViewHolder: id ảnh: "+pId);
        Log.d(TAG, "onBindViewHolder: uidNguoiMua: "+uidNguoiMua);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("GioHang");
        reference.orderByChild("uidNguoiMua").equalTo(uidNguoiMua)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // lấy hình ảnh đầu tiên của sản phẩm
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ProductAds");
                        ref.child(pId).child("Images").limitToFirst(1)
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


                        holder.titleTv.setText(title);
                        holder.sQuantityTv.setText("Số lượng đã đặt: "+Soluongdadat);
                        holder.priceTv.setText("Giá: "+CurrencyFormatter.getFormatter().format(Double.valueOf(price)));
                        holder.finalPriceTv.setText("Tổng tiền: "+ CurrencyFormatter.getFormatter().format(Double.valueOf(tongtiensp)));

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        //xoá sản phẩm ra khỏi giỏ hàng
        holder.btnXoacart.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
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

                easyDB.deleteRow(1, idGH); //xoá từng dòng
//                easyDB.deleteAllDataFromTable();//xoá tất cả sản phẩm
                Utils.toastySuccess(context,"Đã xóa khỏi giỏ hàng...");

                //refresh list
                cartArrayList.remove(position);
                notifyItemChanged(position);
                notifyDataSetChanged();
                int quantity = Soluongdadat - Soluongdadat;
                ((ShopAdDetailsActivity)context).sluong = quantity;
                //điều chỉnh tính tổng  sau khi loại bỏ sản phẩm
                double subTotalWithoutDiscount = Double.parseDouble(CurrencyFormatter.getFormatter().format(Double.parseDouble
                        (((ShopAdDetailsActivity)context).finalPriceTv.getText().toString().
                                replace("đ", ""))));
                double totalPrice = subTotalWithoutDiscount - Double.parseDouble(CurrencyFormatter.getFormatter().format(Double.valueOf(tongtiensp)));
                double stotalPrice = Double.parseDouble(CurrencyFormatter.getFormatter().format(Double.valueOf(tongtiensp)));
                ((ShopAdDetailsActivity)context).priceTv.setText(CurrencyFormatter.getFormatter().format(stotalPrice));
                ((ShopAdDetailsActivity)context).finalPriceTv.setText(CurrencyFormatter.getFormatter().format(totalPrice));

//                if (((ShopAdDetailsActivity)context).isPromoCodeApplied){
//                    //applied
//                    if (totalPrice < Double.parseDouble(((ShopAdDetailsActivity)context).promoMinimumOrderPrice)){
//                        //current order price is less then minimum required price
//                        Utils.toastyInfo(context, "Mã này hợp lệ cho đơn hàng với số tiền tối thiểu: VNĐ"+((ShopAdDetailsActivity)context).promoMinimumOrderPrice);
//                        ((ShopAdDetailsActivity)context).applyBtn.setVisibility(View.GONE);
//                        ((ShopAdDetailsActivity)context).promoDescriptionTv.setVisibility(View.GONE);
//                        ((ShopAdDetailsActivity)context).promoDescriptionTv.setText("");
//                        ((ShopAdDetailsActivity)context).discountTv.setText("0đ");
//                        ((ShopAdDetailsActivity)context).isPromoCodeApplied = false;
//                        //show new net total after delivery fee
//                        ((ShopAdDetailsActivity)context).finalPriceTv.setText(CurrencyFormatter.getFormatter().format(totalPrice));
//                    }
//                    else {
//                        ((ShopAdDetailsActivity)context).applyBtn.setVisibility(View.VISIBLE);
//                        ((ShopAdDetailsActivity)context).promoDescriptionTv.setVisibility(View.VISIBLE);
//                        ((ShopAdDetailsActivity)context).promoDescriptionTv.setText(((ShopAdDetailsActivity)context).promoDescription);
//                        //show new total price after adding delivery fee and subtracting promo fee
//                        ((ShopAdDetailsActivity)context).isPromoCodeApplied = true;
//                        ((ShopAdDetailsActivity)context).finalPriceTv.setText(CurrencyFormatter.getFormatter().format(totalPrice-stotalPrice));
//                    }
//                }
//                else {
//                    //not applied
//                    ((ShopAdDetailsActivity)context).finalPriceTv.setText(CurrencyFormatter.getFormatter().format(totalPrice));
//                }
//                ((ShopAdDetailsActivity)context).cartCount(); // tăng số lượng đặt hàng ở biểu tượng giỏ hàng
            }

        });

    }

    @Override
    public int getItemCount() {
        return cartArrayList.size();
    }

    public class HolderCart  extends RecyclerView.ViewHolder{
        ShapeableImageView productIv;

        TextView titleTv,sQuantityTv,priceTv,finalPriceTv;
        Button btnXoacart;

        public HolderCart(@NonNull View itemView) {
            super(itemView);
            productIv = binding.productIv;
            titleTv = binding.titleTv;
            sQuantityTv = binding.sQuantityTv;
            priceTv = binding.priceTv;
            finalPriceTv = binding.finalPriceTv;
            btnXoacart = binding.btnXoacart;
        }
    }
}
