package com.example.olx.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.olx.CurrencyFormatter;
import com.example.olx.R;
import com.example.olx.Utils;
import com.example.olx.adapter.AdapterCart;
import com.example.olx.adapter.AdapterImageSlider;
import com.example.olx.databinding.ActivityShopAdDetailsBinding;
import com.example.olx.model.ModelAddProduct;
import com.example.olx.model.ModelCart;
import com.example.olx.model.ModelImageSlider;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class ShopAdDetailsActivity extends AppCompatActivity {
    private ActivityShopAdDetailsBinding binding;

    private static final String TAG = "AD_DETAILS";

    private FirebaseAuth firebaseAuth;

    private String id = ""; // id của Model Ad

    private ProgressDialog progressDialog;
    private double latitude;
    private double longitude;
    private  String address;

    private String sellerUid = null; // tài khoản user = null


    private boolean favorite = false;

    private RatingBar ratingBar;

    private ArrayList<ModelImageSlider> imageSliderArrayList;
    private ArrayList<ModelAddProduct> addProductArrayList;
    //cart
    private ArrayList<ModelCart> cartItemList;

    private EasyDB easyDB;

    private String idHD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityShopAdDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.editBtn.setVisibility(View.GONE);
        binding.deleteBtn.setVisibility(View.GONE);
        binding.smsBtn.setVisibility(View.GONE);

        id = getIntent().getStringExtra("adId");
        Log.d(TAG, "onCreate: id: " + id);
        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {
            checkIsFavorite();
        }
        loadAdImages();
        loadDetails();
        loadReviews();
        //init progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Vui lòng đợi");
        progressDialog.setCanceledOnTouchOutside(false);


        // mỗi cửa hàng có sản phẩm và đơn hàng riêng nên nếu người dùng thêm mặt hàng vào giỏ hàng và quay lại và mở giỏ hàng ở cửa hàng khác thì giỏ hàng sẽ khác
        // vì vậy hãy xóa dữ liệu giỏ hàng bất cứ khi nào người dùng mở hoạt động này
        // xóa tất cả sản phẩm  khỏi giỏ hàng
//        deleteCartData();

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
                                Utils.toastySuccess(ShopAdDetailsActivity.this, "Xóa thành công");
                            }
                        })
                        .setNegativeButton("Thoát", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Utils.toastyInfo(ShopAdDetailsActivity.this, "Thoát");
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
                Utils.toast(ShopAdDetailsActivity.this, "Sản phẩm yêu thích");
                if (favorite) {
                    Log.d(TAG, "onClick: removeFavorite");
                    Utils.removeFavorite(ShopAdDetailsActivity.this, id);
                } else {
                    Log.d(TAG, "onClick: addToFavorite");
                    Utils.addToFavorite(ShopAdDetailsActivity.this, id);
                }
            }
        });
        binding.sellerProfileCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //seller profile

                if (firebaseAuth.getCurrentUser() == null) {
                    Utils.toast(ShopAdDetailsActivity.this, "Bạn cần đăng nhập tài khoản");
                } else {
                    Intent intentProfileSeller = new Intent(ShopAdDetailsActivity.this, MainSellerProfileActivity.class);
                    intentProfileSeller.putExtra("sellerUid", sellerUid);
                    startActivity(intentProfileSeller);
                    Log.d(TAG, "onClick: sellerUid: " + sellerUid);
                }

            }
        });
        binding.mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //map
                if (firebaseAuth.getCurrentUser() == null) {
                    Utils.toast(ShopAdDetailsActivity.this, "Bạn cần đăng nhập tài khoản");
                } else {
                    Utils.mapIntent(ShopAdDetailsActivity.this, latitude, longitude);
                }

            }
        });
        //các bạn gọi điện

        binding.smsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //sms
                if (firebaseAuth.getCurrentUser() == null) {
                    Utils.toast(ShopAdDetailsActivity.this, "Bạn cần đăng nhập tài khoản");
                } else {
                    Utils.startSMSIntent(ShopAdDetailsActivity.this, phone);
                }

            }
        });
        //bấm vào dấu ba chấm

        //popup menu
        final PopupMenu popupMenu = new PopupMenu(ShopAdDetailsActivity.this, binding.moreBtn);
        //add menu items to our menu
        popupMenu.getMenu().add("Gọi điện");
        popupMenu.getMenu().add("Chats");
        popupMenu.getMenu().add("Doanh Thu");
        //handle menu item click
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            if (menuItem.getTitle() == "Gọi điện") {
                //start settings screen
                Utils.toast(ShopAdDetailsActivity.this, "Chức năng đang code, đợi clip sau");
                //call
                if (firebaseAuth.getCurrentUser() == null) {
                    Utils.toast(ShopAdDetailsActivity.this, "Bạn cần đăng nhập tài khoản");
                } else {
                    Utils.callIntent(ShopAdDetailsActivity.this, phone);
                }
            } else if (menuItem.getTitle() == "Chats") {
                //open same reviews activity as used in user main page
                Utils.toast(ShopAdDetailsActivity.this, "Chats");
                //chat
                Intent intent = new Intent(ShopAdDetailsActivity.this, ChatActivity.class);
                intent.putExtra("receiptUid", sellerUid);
                startActivity(intent);

            }
            else if (menuItem.getTitle() == "Doanh Thu") {
                //open same reviews activity as used in user main page
                Utils.toast(ShopAdDetailsActivity.this, "Doanh thu");
                //chat
                Intent intent = new Intent(ShopAdDetailsActivity.this, DoanhThuSellerActivity.class);
                intent.putExtra("idHD", idHD);
                startActivity(intent);

            }

            return true;
        });
        //show more options: Gọi điện, Chat, Đánh giá sản phẩm
        binding.moreBtn.setOnClickListener(view -> {
            //show popup menu
            popupMenu.show();
        });
        binding.cartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("");
                showCartDialog();
            }
        });

    }


    public int tongtien = 0; // tạo một biến tạm để tính tổng các mặt hàng
    public int sluong = 0;//tạo một biến tạm để tính số lượng đã đặt tổng các mặt hàng
    public String tenSP = ""; //tạo một biến tạm để lấy tên các mặt hàng
    public String uidNguoiBan;
    public String uidNguoiMua;
    public TextView finalPriceTv;
    public String shopNames;
    public String phone;
    @SuppressLint("MissingInflatedId")
    private void showCartDialog() {
        cartItemList = new ArrayList<>();
        //inflate cart layout
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_cart, null);
        //init views
        TextView shopNameTv = view.findViewById(R.id.shopNameTv);
        TextView sdtTv = view.findViewById(R.id.sdtTv);
        RecyclerView cartItemsRv = view.findViewById(R.id.cartItemsRv);
        finalPriceTv = view.findViewById(R.id.finalPriceTv);// tổng giá
        Button checkoutBtn = view.findViewById(R.id.checkoutBtn);
        //bất cứ khi nào hộp thoại giỏ hàng hiển thị, hãy kiểm tra xem mã khuyến mãi có được áp dụng hay không
        //dialog
        android.app.AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //set view to dialog
        builder.setView(view);
        shopNameTv.setText(shopNames);
        sdtTv.setText(phone);

        //declare it to class level and init in onCreate
        EasyDB easyDB = EasyDB.init(ShopAdDetailsActivity.this, "GIOHANG_DB")
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
        //show dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        //get all records from db
        Cursor res = easyDB.getAllData();
        while (res.moveToNext()){
            int id = res.getInt(1);
            String pId = res.getString(2);
            tenSP = res.getString(3);
            int price = res.getInt(4);
            int quantity = res.getInt(5);
            int tongtienSP = res.getInt(6);
            uidNguoiBan = res.getString(7);
            uidNguoiMua = res.getString(8);


            tongtien = tongtien + tongtienSP;

            finalPriceTv.setText(""+CurrencyFormatter.getFormatter().format(Double.valueOf(tongtien)));
            ModelCart modelCart = new ModelCart(
                    id,
                    ""+pId,
                    ""+tenSP,
                    price,
                    quantity,
                    tongtienSP,
                    ""+uidNguoiBan,
                    ""+uidNguoiMua
            );
            cartItemList.add(modelCart);
        }
        //setup adapter
        AdapterCart adapterCart = new AdapterCart(this, cartItemList);
        //set to recyclerview
        cartItemsRv.setAdapter(adapterCart);

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                xoaGioHang();
                finalPriceTv.setText("0đ");
            }
        });

        checkoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cartItemList.size() == 0){
                    Utils.toastyInfo(ShopAdDetailsActivity.this,"Không có mặt hàng nào được đặt trong giỏ hàng");

                }else {
                    submitOrder();
                    xoaGioHang();// xóa sạch giỏ hàng sau khi thoát activity
                }
            }
        });

    }


    private void submitOrder() {
        //show progress dialog
        progressDialog.setMessage("Vui lòng đợi...");
        progressDialog.show();

        long timestamp = Utils.getTimestamp();
        //setup oder data

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");

        idHD = reference.push().getKey();

        //setup oder data
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("orderId", ""+idHD);
        hashMap.put("orderMaHD", timestamp);
        hashMap.put("orderStatus", "Chưa duyệt"); //In Progress/Completed/Cancelled
        hashMap.put("orderTongTien", tongtien);
        hashMap.put("orderBy", ""+uidNguoiMua);
        hashMap.put("orderTo", ""+uidNguoiBan);
        hashMap.put("address", ""+address);
        hashMap.put("timestamp", timestamp);
        hashMap.put("latitude", latitude);
        hashMap.put("longitude",longitude);

        //add to db
        reference.child(uidNguoiBan).child("Order").child(idHD).setValue(hashMap)
                .addOnSuccessListener(aVoid -> {
                    //order info added now add order items
                    for ( int i=0; i<cartItemList.size(); i++){
                        String pId = cartItemList.get(i).getProductAdsId();
                        int id = cartItemList.get(i).getId();
                        tenSP = cartItemList.get(i).getTen();
                        int price = cartItemList.get(i).getPrice();
                        int soluongdadat = cartItemList.get(i).getSoluongdadat();
                        int tongtien = cartItemList.get(i).getTongtien();
                        String uidNguoiMua1 = cartItemList.get(i).getUidNguoiMua();
                        String uidNguoiBan1 = cartItemList.get(i).getUidNguoiBan();
                        Log.d(TAG, "submitOrder: id: "+id);
                        Log.d(TAG, "submitOrder: pId: "+pId);
                        Log.d(TAG, "submitOrder: tenSP: "+tenSP);
                        Log.d(TAG, "submitOrder: price: "+price);
                        Log.d(TAG, "submitOrder: quantity: "+soluongdadat);
                        Log.d(TAG, "submitOrder: tongtienSP: "+tongtien);
                        Log.d(TAG, "submitOrder: uidNguoiMua1: "+uidNguoiMua1);
                        Log.d(TAG, "submitOrder: uidNguoiBan1: "+uidNguoiBan1);
                        HashMap<String, Object> hashMap1 = new HashMap<>();
                        hashMap1.put("productAdsId", pId);
                        hashMap1.put("ten", tenSP);
                        hashMap1.put("tongtien", tongtien);
                        hashMap1.put("price",price);
                        hashMap1.put("soluongdadat", soluongdadat);
                        hashMap1.put("uidNguoiMua", ""+uidNguoiMua1);
                        hashMap1.put("uidNguoiBan", ""+uidNguoiBan1);
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                        ref.child(uidNguoiBan).child("Order").child(idHD).child("GioHang").child(pId).setValue(hashMap1);
                    }
                    progressDialog.dismiss();
                    Utils.toastySuccess(ShopAdDetailsActivity.this,"Đặt hàng thành công...");

                })
                .addOnFailureListener(e -> {
                    //failed placing order
                    progressDialog.dismiss();
                    Utils.toast(ShopAdDetailsActivity.this,"Lỗi"+e.getMessage());
                });

        Utils.toast(ShopAdDetailsActivity.this,"Tạo hoá đơn thành công");


    }


    private void editOptions() {
        Log.d(TAG, "editOptions: ");

        PopupMenu popupMenu = new PopupMenu(this, binding.editBtn);

        popupMenu.getMenu().add(Menu.NONE, 0, 0, "Chỉnh sửa");
        popupMenu.getMenu().add(Menu.NONE, 1, 1, "Đánh dấu là đã bán");

        popupMenu.show();

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == 0) {
                    Intent intent = new Intent(ShopAdDetailsActivity.this, ShopAdCreateActivity.class);
                    intent.putExtra("isEditMode", true);
                    intent.putExtra("adId", id);
                    startActivity(intent);
                } else if (itemId == 1) {
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
                        hashMap.put("status", "" + Utils.AD_STATUS_SOLD);

                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ProductAds");
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


                        Utils.toastySuccess(getApplicationContext(), "Đã bán thành công");

                    }
                })
                .setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "onClick: Hủy bỏ...");
                        dialog.dismiss();
                        Utils.toastyInfo(getApplicationContext(), "Hủy bỏ");
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

                        Log.d(TAG, "onDataChange: favorite: " + favorite);

                        if (favorite) {
                            binding.favBtn.setImageResource(R.drawable.ic_favorite_yes);
                        } else {
                            binding.favBtn.setImageResource(R.drawable.ic_fav);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    //đánh giá sản phẩm
    private float ratingSum = 0;

    private void loadReviews() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String uid = "" + ds.getRef().getKey();
                    Log.d(TAG, "onDataChange: uid: " + uid);
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(uid);
                    ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    //clear list before adding data into it
                                    ratingSum = 0;
                                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                        float rating = Float.parseFloat("" + ds.child("ratings").getValue()); //e.g. 4.3
                                        ratingSum = ratingSum + rating; //for avg rating, add(addition of) all ratings, later will divide it by number of reviews
                                    }

                                    long numberOfReviews = dataSnapshot.getChildrenCount();
                                    float avgRating = ratingSum / numberOfReviews;

                                    binding.ratingBar.setRating(avgRating);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    //load sản phẩm quảng cáo
    private void loadDetails() {
        addProductArrayList = new ArrayList<>();
        Log.d(TAG, "loadDetails: ");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("ProductAds");
        reference.child(id)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        addProductArrayList.clear();
                        try {
                            ModelAddProduct modelAd = snapshot.getValue(ModelAddProduct.class);

                            sellerUid = modelAd.getUid(); // tài khoản user
                            String title = modelAd.getTitle();
                            String brand = modelAd.getBrand();
                            String description = modelAd.getDescription();
                            address = modelAd.getAddress();
                            String condition = modelAd.getCondition();
                            String category = modelAd.getCategory();
                            int reducedprice = modelAd.getReducedprice();
                            int price = modelAd.getPrice();
                            latitude = modelAd.getLatitude();
                            longitude = modelAd.getLongitude();

                            long timestamp = modelAd.getTimestamp();

                            String formatteDate = Utils.formatTimestampDate(timestamp);

                            if (sellerUid.equals(firebaseAuth.getUid())) {
                                binding.editBtn.setVisibility(View.VISIBLE);
                                binding.deleteBtn.setVisibility(View.VISIBLE);
                                binding.smsBtn.setVisibility(View.GONE);
                                binding.receiptProfileLabelTv.setVisibility(View.GONE);
                                binding.sellerProfileCv.setVisibility(View.GONE);
                            } else {
                                binding.editBtn.setVisibility(View.GONE);
                                binding.deleteBtn.setVisibility(View.GONE);
                                binding.smsBtn.setVisibility(View.VISIBLE);
                                binding.receiptProfileLabelTv.setVisibility(View.VISIBLE);
                                binding.sellerProfileCv.setVisibility(View.VISIBLE);

                            }

                            binding.titleTv.setText(title);
                            binding.brandTv.setText(brand);
                            binding.descriptionTv.setText(description);
                            binding.addressTv.setText(address);
                            binding.conditionTv.setText(condition);
                            binding.categoryTv.setText(category);
                            if (reducedprice == 0) {
                                Log.d(TAG, "onDataChange: reducedprice");
                                binding.priceTv.setVisibility(View.VISIBLE); //hiện giá gốc
                                binding.priceSymbolTv.setVisibility(View.GONE);
                                binding.priceTv.setText("Giá: "+CurrencyFormatter.getFormatter().format(Double.valueOf(price)));
                                binding.priceTv.setTextColor(Color.RED);

                            } else {
                                binding.priceSymbolTv.setVisibility(View.VISIBLE);//hiện giá giảm
                                binding.priceTv.setVisibility(View.GONE); // đóng giá gốc lại
                                binding.priceSymbolTv.setTextColor(Color.RED);
                                binding.priceSymbolTv.setText("Giá: "+CurrencyFormatter.getFormatter().format(Double.valueOf(reducedprice)));

                            }


                            binding.dateTv.setText("Thời gian: "+formatteDate);

                            loadSellerDetails();
                        } catch (Exception e) {
                            Log.d(TAG, "onDataChange: " + e);
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
                        String name = "" + snapshot.child("name").getValue();
                        shopNames = ""+snapshot.child("shopName").getValue();
                        phone = ""+snapshot.child("phone").getValue();
                        String profileImage = "" + snapshot.child("profileImage").getValue();
                        String timestamp = "" + snapshot.child("timestamp").getValue();

                        String formattedDate = Utils.formatTimestampDate(Long.valueOf(timestamp));

                        binding.sellerNameTv.setText(name);
                        binding.memberSingleTv.setText(formattedDate);

                        try {
                            Glide.with(ShopAdDetailsActivity.this)
                                    .load(profileImage)
                                    .placeholder(R.drawable.ic_users)
                                    .into(binding.sellerProfileIv);
                            Log.d(TAG, "onDataChange: Load hình ảnh sản phẩm thành công");
                        } catch (Exception e) {
                            Log.e(TAG, "onDataChange: ", e);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    //load hình ảnh sản phẩm
    private void loadAdImages() {
        Log.d(TAG, "loadAdImages: ");

        imageSliderArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ProductAds");
        ref.child(id).child("Images")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        imageSliderArrayList.clear();

                        for (DataSnapshot ds : snapshot.getChildren()) {
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
    public void xoaGioHang() {
        // Xóa hết sp khỏi giỏ
        //declare it to class level and init in onCreate
        easyDB = EasyDB.init(ShopAdDetailsActivity.this, "GIOHANG_DB")
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
        easyDB.deleteAllDataFromTable();
    }



}