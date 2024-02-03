package com.example.olx.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import com.bumptech.glide.Glide;
import com.example.olx.CurrencyFormatter;
import com.example.olx.R;
import com.example.olx.Utils;
import com.example.olx.adapter.AdapterImageSlider;
import com.example.olx.databinding.ActivityShopAdDetailsBinding;
import com.example.olx.model.ModelAddProduct;
import com.example.olx.model.ModelImageSlider;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class ShopAdDetailsActivity extends AppCompatActivity {
    private ActivityShopAdDetailsBinding binding;

    private static  final String TAG ="AD_DETAILS";

    private FirebaseAuth firebaseAuth;

    private String id =""; // id của Model Ad

    private ProgressDialog progressDialog;
    private double latitude;
    private double longitude;

    private String sellerUid= null; // tài khoản user = null

    private String sellerPhone= "";

    private boolean favorite = false;

    private String idImage = "";

    private ArrayList<ModelImageSlider> imageSliderArrayList;

    private EasyDB easyDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityShopAdDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.editBtn.setVisibility(View.GONE);
        binding.deleteBtn.setVisibility(View.GONE);
        binding.smsBtn.setVisibility(View.GONE);

        id =getIntent().getStringExtra("adId");
        Log.d(TAG, "onCreate: id: "+id);
        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null){
            checkIsFavorite();
        }
        loadAdImages();
        loadDetails();
        //init progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Vui lòng đợi");
        progressDialog.setCanceledOnTouchOutside(false);

        //declare it to class level and init in onCreate
//        easyDB = EasyDB.init(this, "ITEMS_DB")
//                .setTableName("ITEMS_TABLE")
//                .addColumn(new Column("Item_Id", "text", "unique"))
//                .addColumn(new Column("Item_PID", "text", "not null"))
//                .addColumn(new Column("Item_Name", "text", "not null"))
//                .addColumn(new Column("Item_Price_Each", "text", "not null"))
//                .addColumn(new Column("Item_Price", "text", "not null"))
//                .addColumn(new Column("Item_Quantity", "text", "not null"))
//                .doneTableColumn();

        //each shop have its own products and orders so if user add items to cart and go back and open cart in different shop then cart should be different
        //so delete cart data whenever user open this activity
//        deleteCartData();
//        cartCount();

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        binding.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(ShopAdDetailsActivity.this);
                materialAlertDialogBuilder.setTitle("Xóa quảng cáo sản phẩm")
                        .setMessage("Bạn có chắc chắn muốn xóa không?")
                        .setPositiveButton("Xóa", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
//                                deleteAd();
                                Log.d(TAG, "onClick: Xóa");
                                Utils.toastySuccess(ShopAdDetailsActivity.this,"Xóa thành công");
                            }
                        })
                        .setNegativeButton("Thoát", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Utils.toastyInfo(ShopAdDetailsActivity.this,"Thoát");
                                Log.d(TAG, "onClick: Xóa thất bại");
                                dialog.dismiss();
                            }
                        }).show();
            }
        });




        binding.editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //edit
                editOptions();

            }
        });

        binding.favBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.toast(ShopAdDetailsActivity.this,"Chức năng đang code");
//                if (favorite){
//                    Utils.removeFavorite(ShopAdDetailsActivity.this,id);
//                }
//                else {
//                    Utils.addToFavorite(ShopAdDetailsActivity.this,id);
//                }
            }
        });
        binding.sellerProfileCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //seller profile
                Utils.toast(ShopAdDetailsActivity.this,"Chức năng đang code");
                if (firebaseAuth.getCurrentUser() == null){
                    Utils.toast(ShopAdDetailsActivity.this,"Bạn cần đăng nhập tài khoản");
                }else{
//                    Intent intentProfileSeller = new Intent(ShopAdDetailsActivity.this, AdSellerProfileActivity.class);
//                    intentProfileSeller.putExtra("sellerUid",sellerUid);
//                    startActivity(intentProfileSeller);
                }

            }
        });
        binding.mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //map
                if (firebaseAuth.getCurrentUser() == null){
                    Utils.toast(ShopAdDetailsActivity.this,"Bạn cần đăng nhập tài khoản");
                }else{
                    Utils.mapIntent(ShopAdDetailsActivity.this,latitude, longitude);
                }

            }
        });
        //bấm vào dấu ba chấm

        //popup menu
        final PopupMenu popupMenu = new PopupMenu(ShopAdDetailsActivity.this, binding.moreBtn);
        //add menu items to our menu
        popupMenu.getMenu().add("Gọi điện");
        popupMenu.getMenu().add("Chats");
        popupMenu.getMenu().add("Đánh giá sản phẩm");
        //handle menu item click
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            if (menuItem.getTitle() == "Gọi điện"){
                //start settings screen
                Utils.toast(ShopAdDetailsActivity.this,"Chức năng đang code, đợi clip sau");
//                startActivity(new Intent(MainSellerActivity.this, SettingsActivity.class));
            }
            else if (menuItem.getTitle() == "Chats"){
                //open same reviews activity as used in user main page
                Utils.toast(ShopAdDetailsActivity.this,"Chức năng đang code, đợi clip sau");
//                Intent intent = new Intent(MainSellerActivity.this, ShopReviewsActivity.class);
//                intent.putExtra("shopUid", ""+firebaseAuth.getUid());
//                startActivity(intent);
            }
            else if (menuItem.getTitle() == "Đánh giá sản phẩm"){
                //start promotions list screen
                Utils.toast(ShopAdDetailsActivity.this,"Chức năng đang code, đợi clip sau");
//                startActivity(new Intent(MainSellerActivity.this, PromotionCodesActivity.class));
            }

            return true;
        });
        //show more options: Gọi điện, Chat, Đánh giá sản phẩm
        binding.moreBtn.setOnClickListener(view -> {
            //show popup menu
            popupMenu.show();
        });

    }

    private void cartCount() {
        //keep it public so we can access in adapter
        //get cart count
        int count = easyDB.getAllData().getCount();
        if (count<=0){
            //no item in cart, hide cart count textview
            binding.cartCountTv.setVisibility(View.GONE);
        }
        else {
            //have items in cart, show cart count textview and set count
            binding.cartCountTv.setVisibility(View.VISIBLE);
            binding.cartCountTv.setText(""+count);//concatenate with string, because we cant set integer in textview
        }
    }

    private void deleteCartData() {
        easyDB.deleteAllDataFromTable();//delete all records from cart
    }

    private void editOptions() {
        Log.d(TAG, "editOptions: ");

        PopupMenu popupMenu = new PopupMenu(this, binding.editBtn);

        popupMenu.getMenu().add(Menu.NONE,0,0,"Chỉnh sửa");
        popupMenu.getMenu().add(Menu.NONE,1,1,"Đánh dấu là đã bán");

        popupMenu.show();

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == 0){
                    Intent intent = new Intent(ShopAdDetailsActivity.this, ShopAdCreateActivity.class);
                    intent.putExtra("isEditMode", true);
                    intent.putExtra("adId", id);
                    startActivity(intent);
                }
                else if (itemId == 1){
                    showMarkAsSoldDialog();
                }
                return true;
            }
        });
    }

    //Đánh dấu là đã bán hay chưa bán
    private void showMarkAsSoldDialog() {
        MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(this);
        alertDialogBuilder.setTitle("Đánh dấu là đã bán sản phẩm")
                .setMessage("Bán có chắc chắn là đánh dấu quảng cáo sản phẩm này đã bán không?")
                .setPositiveButton("Đã bán", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "onClick: Đã bán thành công...");

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("status", ""+ Utils.AD_STATUS_SOLD);

                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Ads");
                        ref.child(id)
                                .updateChildren(hashMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {

                                        Log.d(TAG, "onSuccess: Đánh dấu là đã bán");
                                        Utils.toastySuccess(ShopAdDetailsActivity.this, "Đánh dấu là đã bán sản phẩm thành công");

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "onFailure: Lỗi: " + e);
                                        Utils.toastyError(ShopAdDetailsActivity.this, "Lỗi không đánh dấu là đã bán được" + e.getMessage());
                                    }
                                });


                        Utils.toastySuccess(getApplicationContext(),"Đã bán thành công");

                    }
                })
                .setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "onClick: Hủy bỏ...");
                        dialog.dismiss();
                        Utils.toastyInfo(getApplicationContext(),"Hủy bỏ");
                    }
                })
                .show();
    }

    private void checkIsFavorite() {
        Log.d(TAG, "checkIsFavorite: ");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Favorites").child(id)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        favorite = snapshot.exists();

                        Log.d(TAG, "onDataChange: favorite: "+favorite);

                        if (favorite){
                            binding.favBtn.setImageResource(R.drawable.ic_favorite_yes);
                        }
                        else {
                            binding.favBtn.setImageResource(R.drawable.ic_fav);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    //load sản phẩm quảng cáo
    private  void loadDetails(){
        Log.d(TAG, "loadDetails: ");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("ProductAds");
        reference.child(id)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        try {
                            ModelAddProduct modelAd = snapshot.getValue(ModelAddProduct.class);

                            sellerUid = modelAd.getUid(); // tài khoản user
                            String title = modelAd.getTitle();
                            String description = modelAd.getDescription();
                            String address = modelAd.getAddress();
                            String condition = modelAd.getCondition();
                            String category = modelAd.getCategory();
                            int reducedprice = modelAd.getReducedprice();
                            boolean discount = modelAd.isDiscount();
                            int price = modelAd.getPrice();
                            latitude = modelAd.getLatitude();
                            longitude = modelAd.getLongitude();

                            long timestamp = modelAd.getTimestamp();

                            String formatteDate = Utils.formatTimestampDate(timestamp);

                            if (sellerUid.equals(firebaseAuth.getUid())){
                                binding.editBtn.setVisibility(View.VISIBLE);
                                binding.deleteBtn.setVisibility(View.VISIBLE);
                                binding.smsBtn.setVisibility(View.GONE);
                                binding.receiptProfileLabelTv.setVisibility(View.GONE);
                                binding.sellerProfileCv.setVisibility(View.GONE);
                            }
                            else {
                                binding.editBtn.setVisibility(View.GONE);
                                binding.deleteBtn.setVisibility(View.GONE);
                                binding.smsBtn.setVisibility(View.VISIBLE);
                                binding.receiptProfileLabelTv.setVisibility(View.VISIBLE);
                                binding.sellerProfileCv.setVisibility(View.VISIBLE);

                            }

                            binding.titleTv.setText(title);
                            binding.descriptionTv.setText(description);
                            binding.addressTv.setText(address);
                            binding.conditionTv.setText(condition);
                            binding.categoryTv.setText(category);
                            if (reducedprice==0){
                                Log.d(TAG, "onDataChange: reducedprice");
                                binding.priceTv.setVisibility(View.VISIBLE); //hiện giá gốc
                                binding.priceSymbolTv.setVisibility(View.GONE);
                                binding.priceTv.setText(CurrencyFormatter.getFormatter().format(Double.valueOf(price)));
                                binding.priceTv.setTextColor(Color.RED);

                            }
                            else {
                                binding.priceSymbolTv.setVisibility(View.VISIBLE);//hiện giá giảm
                                binding.priceTv.setVisibility(View.GONE); // đóng giá gốc lại
                                binding.priceSymbolTv.setTextColor(Color.RED);
                                binding.priceSymbolTv.setText(CurrencyFormatter.getFormatter().format(Double.valueOf(reducedprice)));

                            }


                            binding.dateTv.setText(formatteDate);

                            loadSellerDetails();
                        }
                        catch (Exception e){
                            Log.d(TAG, "onDataChange: "+e);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    //load thông tin người bán
    private void loadSellerDetails() {
        Log.d(TAG, "loadSellerDetails: ");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(sellerUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String phone ="" +snapshot.child("phone").getValue();
                        String name ="" +snapshot.child("name").getValue();
                        String profileImage ="" +snapshot.child("profileImage").getValue();
                        String timestamp = ""+snapshot.child("timestamp").getValue();

                        String formattedDate = Utils.formatTimestampDate(Long.valueOf(timestamp));

                        sellerPhone = phone;

                        binding.sellerNameTv.setText(name);
                        binding.memberSingleTv.setText(formattedDate);

                        try {
                            Glide.with(ShopAdDetailsActivity.this)
                                    .load(profileImage)
                                    .placeholder(R.drawable.ic_users)
                                    .into(binding.sellerProfileIv);
                            Log.d(TAG, "onDataChange: Load hình ảnh sản phẩm thành công");
                        }catch (Exception e){
                            Log.e(TAG, "onDataChange: ",e );
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    //load hình ảnh sản phẩm
    private void loadAdImages(){
        Log.d(TAG, "loadAdImages: ");

        imageSliderArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ProductAds");
        ref.child(id).child("Images")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        imageSliderArrayList.clear();

                        for (DataSnapshot ds: snapshot.getChildren()){
                            ModelImageSlider modelImageSlider = ds.getValue(ModelImageSlider.class);

                            imageSliderArrayList.add(modelImageSlider);
                        }
                        AdapterImageSlider adapterImageSlider = new AdapterImageSlider(ShopAdDetailsActivity.this, imageSliderArrayList);
                        binding.imageSliderVp.setAdapter(adapterImageSlider);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

}