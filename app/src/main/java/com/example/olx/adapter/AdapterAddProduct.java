package com.example.olx.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class AdapterAddProduct extends RecyclerView.Adapter<AdapterAddProduct.HolderAddProduct> implements Filterable {
    private RowAddproductBinding binding;
    private Context context;
    private FirebaseAuth firebaseAuth;
    public ArrayList<ModelAddProduct> adArrayList;
    private ArrayList<ModelAddProduct> filterList;

    private ProgressDialog progressDialog;

    private FilterAddProducts filterAddProducts;

    private static final String TAG = "AdapterProduct";

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
        ModelAddProduct modelAddProduct = adArrayList.get(position);

        String id = modelAddProduct.getId();

        String title = modelAddProduct.getTitle();
        String description = modelAddProduct.getDescription();
        String address = modelAddProduct.getAddress();
        String condition = modelAddProduct.getCondition();
        int price = modelAddProduct.getPrice();
        int quantity = modelAddProduct.getQuantity();
        int raito = modelAddProduct.getRaito();
        int reducedprice = modelAddProduct.getReducedprice();
        long timestamp = modelAddProduct.getTimestamp();
        String uid = modelAddProduct.getUid();
        String formattedDate = Utils.formatTimestampDate(timestamp);
        Log.d(TAG, "onBindViewHolder: "+formattedDate);

        loadAdFirstImage(modelAddProduct, holder);

        if (firebaseAuth.getCurrentUser() != null) {
            checkIsFavorites(modelAddProduct, holder);
        }

        holder.titleTv.setText("Sản phẩm: " + title);
        holder.descriptionTv.setText("Mô tả: " + description);
        holder.conditionTv.setText("Tình trạng: " + condition);
        if (modelAddProduct.isDiscount()){
            // trường hợp có giảm giá thì gạch giá gốc
            holder.priceTv.setPaintFlags(holder.priceTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.priceTv.setText("Giá gốc: " + CurrencyFormatter.getFormatter().format(Double.valueOf(price)));
        }else {
            // trường hợp không có giảm giá thì gạch giá gốc
            holder.priceTv.setText("Giá gốc: " + CurrencyFormatter.getFormatter().format(Double.valueOf(price)));
        }
//        holder.priceTv.setText("Giá gốc: " + CurrencyFormatter.getFormatter().format(Double.valueOf(price)));
        holder.raitoTv.setText("-" + raito + "%");
        holder.pricesTv.setText("Giảm giá: " + CurrencyFormatter.getFormatter().format(Double.valueOf(reducedprice)));
        holder.dateTv.setText("Ngày: "+formattedDate);


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentShopAd = new Intent(context, ShopAdDetailsActivity.class);
                intentShopAd.putExtra("adId", modelAddProduct.getId());
                context.startActivity(intentShopAd);
            }
        });

        holder.favBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean favorite = modelAddProduct.isFavorite();
                if (favorite) {
                    Utils.removeFavorite(context, id);
                } else {
                    Utils.addToFavorite(context, id);

                }
            }
        });

        holder.ortherBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OderAd(modelAddProduct);

            }
        });

    }

    //phần xử lý thêm vào giỏ hàng
    private int quantitys = 0; // khi người dùng bấm tăng số lượng cần đặt hàng vào giỏ hàng
    private int giaTien = 0;
    private int tongGiaTienSanPham = 0; //giá tiền và tổng giá tiền
    public String giaReducedPrice = ""; //giá giảm
    public String giaPrice = ""; // giá gốc

    private void OderAd(ModelAddProduct modelAddProducts) {
        //inflate layout for dialog
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_order, null);
        //init layout views
        final ImageView productIv = view.findViewById(R.id.productIv);
        final TextView titleTv = view.findViewById(R.id.titleTv);
        final TextView pQuantityTv = view.findViewById(R.id.pQuantityTv);
        final TextView slQuantityTv = view.findViewById(R.id.slQuantityTv);
        final TextView descriptionTv = view.findViewById(R.id.descriptionTv);
        final TextView raitoTv = view.findViewById(R.id.raitoTv);
        final TextView priceTv = view.findViewById(R.id.gPriceTv);
        final TextView reducedPriceTv = view.findViewById(R.id.reducedPriceTv);
        final TextView finalPriceTv = view.findViewById(R.id.finalPriceTv);
        ImageButton decrementBtn = view.findViewById(R.id.decrementBtn);
        ImageButton incrementBtn = view.findViewById(R.id.incrementBtn);
        Button continueBtn = view.findViewById(R.id.continueBtn);

        //get data from model
        String addId = modelAddProducts.getId();
        String title = modelAddProducts.getTitle();
        int price = modelAddProducts.getPrice();
        int productQuantity = modelAddProducts.getQuantity();
        String description = modelAddProducts.getDescription();
        int reducedprice = modelAddProducts.getReducedprice();
        int raito = modelAddProducts.getRaito();
        String uidNguoiBan = modelAddProducts.getUid();
        String uidNguoiMua = firebaseAuth.getUid();


        // lấy hình ảnh đầu tiên của sản phẩm
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ProductAds");
        ref.child(addId).child("Images").limitToFirst(1)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String imageUrl = "" + ds.child("imageUrl").getValue();
                            Log.d(TAG, "onDataChange: ");
                            try {
                                Picasso.get().load(imageUrl).placeholder(R.drawable.cart).into(productIv);
                            } catch (Exception e) {
                                Log.d(TAG, "onDataChange: Lỗi: " + e);
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        //nếu có giảm giá thì vô dòng if, còn ngược lại sản phẩm ko có giảm giá thì else
        if (modelAddProducts.isDiscount()) {

            //chỗ này còn sai
            Log.d(TAG, "showOderAd: true " + modelAddProducts.isDiscount());
            raitoTv.setVisibility(View.VISIBLE);
            reducedPriceTv.setVisibility(View.VISIBLE);
            priceTv.setPaintFlags(priceTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            giaReducedPrice = String.valueOf(reducedprice);

            Log.d(TAG, "showOderAd: Giá tiền reducedprice: " + giaReducedPrice);
            // tôi lấy tổng tiền = giá giảm
            //xử lý chỗ này
            quantitys = 1; // ban đầu chưa đặt hàng trong giỏ hàng có 1 sản phẩm
            giaTien = Integer.parseInt(giaReducedPrice.replace("đ", ""));
            tongGiaTienSanPham = Integer.parseInt(giaReducedPrice.replace("đ", ""));
            finalPriceTv.setText("Tổng tiền: " + CurrencyFormatter.getFormatter().format(Double.valueOf(tongGiaTienSanPham)));

        } else {
            Log.d(TAG, "showOderAd: false " + modelAddProducts.isDiscount());
            raitoTv.setVisibility(View.GONE);
            reducedPriceTv.setVisibility(View.GONE);
            giaPrice = String.valueOf(price);
            Log.d(TAG, "showOderAd: Giá tiền price" + giaPrice);
            // tôi lấy tổng tiền = giá gốc
            //xử lý chỗ này
            quantitys = 1; // ban đầu chưa đặt hàng trong giỏ hàng có 1 sản phẩm
            giaTien = Integer.parseInt(giaPrice.replace("đ", ""));
            tongGiaTienSanPham = Integer.parseInt(giaPrice.replace("đ", ""));
            Log.d(TAG, "showOderAd: quantitys: " + quantitys);
            Log.d(TAG, "showOderAd: giaTien: " + giaTien);
            Log.d(TAG, "showOderAd: tongGiaTienSanPham: " + tongGiaTienSanPham);
            finalPriceTv.setText("Tổng tiền: " + CurrencyFormatter.getFormatter().format(Double.valueOf(tongGiaTienSanPham))); // tôi lấy tổng tiền == giá gốc

        }


        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);


        titleTv.setText("" + title);
        pQuantityTv.setText("Số lượng sản phẩm đăng bán: " + productQuantity);
        descriptionTv.setText("" + description);
        raitoTv.setText("" + raito + "%");
        slQuantityTv.setText("" + quantitys);
        priceTv.setText("Giá: " + CurrencyFormatter.getFormatter().format(Double.valueOf(price)));
        reducedPriceTv.setText("Giảm giá: " + CurrencyFormatter.getFormatter().format(Double.valueOf(reducedprice)));


        //decrement quantiity of product, only if quantity is > 1
        //increase quantity of the product
        incrementBtn.setOnClickListener(v -> {
            quantitys++;
            Log.d(TAG, "showOderAd: 1");
            if (modelAddProducts.isDiscount()) {
                tongGiaTienSanPham = tongGiaTienSanPham + giaTien;
                slQuantityTv.setText("" + quantitys);
                finalPriceTv.setText("Tổng tiền: " + CurrencyFormatter.getFormatter().format(Double.valueOf(tongGiaTienSanPham))); //giá giảm
            } else {
                tongGiaTienSanPham = tongGiaTienSanPham + giaTien;
                slQuantityTv.setText("" + quantitys);
                finalPriceTv.setText("Tổng tiền: " + CurrencyFormatter.getFormatter().format(Double.valueOf(tongGiaTienSanPham))); //giá gốc
            }


        });
        decrementBtn.setOnClickListener(v -> {
            if (quantitys > 1) {
                quantitys--;
                Log.d(TAG, "showOderAd: 2");
                if (modelAddProducts.isDiscount()) {
                    tongGiaTienSanPham = tongGiaTienSanPham - giaTien;
                    slQuantityTv.setText("" + quantitys);
                    finalPriceTv.setText("Tổng tiền: " + CurrencyFormatter.getFormatter().format(Double.valueOf(tongGiaTienSanPham)));
                } else {
                    tongGiaTienSanPham = tongGiaTienSanPham - giaTien;
                    slQuantityTv.setText("" + quantitys);
                    finalPriceTv.setText("Tổng tiền: " + CurrencyFormatter.getFormatter().format(Double.valueOf(tongGiaTienSanPham)));
                }

            }
        });
        final AlertDialog dialog = builder.create();
        dialog.show();
        //khi bấm button thêm vào giỏ hàng
        continueBtn.setOnClickListener(v -> {
            //Lay san pham ra so sanh
            Log.d(TAG, "OderAd: continueBtn");
            String titleOrder = titleTv.getText().toString();
            int priceOrder = giaTien;
            int tongGiaTienSP = tongGiaTienSanPham; //chuyển giá tiền kiểu int -> string ->lưu lên csdl
            int quantitySL = quantitys; // số lượng tăng giảm
            int raitoOrder = raito;
            // số lượng ban đầu khi đăng bán sản phẩm
            long timestamp = Utils.getTimestamp();
            //format date
            Log.d(TAG, "OderAd: SL đã đặt:" + quantitySL);
            Log.d(TAG, "OderAd: SL:" + productQuantity);
            Log.d(TAG, "OderAd: Tổng tiền:" + tongGiaTienSP);

            Utils.toast(context, "Bạn mới đặt hàng");
            //add to db
            addToCart(addId, titleOrder, priceOrder, quantitySL, tongGiaTienSP, uidNguoiBan, uidNguoiMua);
            dialog.dismiss();
        });
    }

    private int itemId = 1;

    private void addToCart(String addId, String titleOrder, int priceOrder, int quantitySL, int tongGiaTienSP, String uidNguoiBan, String uidNguoiMua) {
        Log.d(TAG, "addToCart: ");
        itemId++;
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

        Boolean b = easyDB.addData("GH_Id", itemId)
                .addData("GH_PID", addId)
                .addData("GH_Title", titleOrder)
                .addData("GH_Price", priceOrder)
                .addData("GH_Quantity", quantitySL)
                .addData("GH_FinalPrice", tongGiaTienSP)
                .addData("GH_UidNguoiBan", uidNguoiBan)
                .addData("GH_UidNguoiMua", uidNguoiMua)
                .doneDataAdding();

        Utils.toastySuccess(context, "Thêm vào giỏ hàng thành công");

    }


    //check yêu thích
    private void checkIsFavorites(ModelAddProduct modelAd, HolderAddProduct holder) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Favorites").child(modelAd.getId())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        boolean favotite = snapshot.exists();
                        Log.d(TAG, "onDataChange: favotite: " + favotite);
                        modelAd.setFavorite(favotite);

                        if (favotite) {
                            holder.favBtn.setImageResource(R.drawable.ic_favorite_yes);
                        } else {
                            holder.favBtn.setImageResource(R.drawable.ic_fav);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadAdFirstImage(ModelAddProduct modelAd, HolderAddProduct holder) {
        Log.d(TAG, "loadAdFirstImage: ");
        String id = modelAd.getId();
        Log.d(TAG, "loadAdFirstImage: " + id);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ProductAds");
        ref.child(id).child("Images").limitToFirst(1)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String imageUrl = "" + ds.child("imageUrl").getValue();
                            Log.d(TAG, "onDataChange: " + imageUrl);
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

    public class HolderAddProduct extends RecyclerView.ViewHolder {
        ShapeableImageView imageIv;
        TextView titleTv, descriptionTv, conditionTv, raitoTv, pricesTv, priceTv, dateTv, ortherBtn;

        ImageButton favBtn;

        public HolderAddProduct(@NonNull View itemView) {
            super(itemView);
            imageIv = binding.imageIv;
            titleTv = binding.titleTv;
            descriptionTv = binding.descriptionTv;
            conditionTv = binding.conditionTv;
            raitoTv = binding.raitoTv;
            pricesTv = binding.pricesTv;
            priceTv = binding.priceTv;
            dateTv = binding.dateTv;
            favBtn = binding.favBtn;
            ortherBtn = binding.ortherBtn;

        }
    }
}
