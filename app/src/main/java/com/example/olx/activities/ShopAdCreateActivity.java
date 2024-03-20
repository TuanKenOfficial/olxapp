package com.example.olx.activities;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;


import com.example.olx.R;
import com.example.olx.Utils;
import com.example.olx.adapter.AdapterImagePicked;
import com.example.olx.databinding.ActivityAdCreateBinding;
import com.example.olx.model.ModelImagePicked;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @noinspection deprecation
 */
public class ShopAdCreateActivity extends AppCompatActivity {

    private ActivityAdCreateBinding binding;
    private static final String TAG = "AdCreate";
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private Uri imageUri = null;
    private ArrayList<ModelImagePicked> imagePickedArrayList;

    private AdapterImagePicked adapterImagePicked;

    private boolean isEditMode = false; //
    private String adId = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdCreateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Vui lòng đợi");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();
        //load danh sách category
        ArrayAdapter<String> adapterCategories = new ArrayAdapter<>(this, R.layout.row_categore_createad, Utils.categories);
        binding.categoryEt.setAdapter(adapterCategories);

        //load danh sách condition
        ArrayAdapter<String> adapterConditions = new ArrayAdapter<>(this, R.layout.row_condition_createad, Utils.conditions);
        binding.conditionEt.setAdapter(adapterConditions);

        //load danh sách số lượng
        ArrayAdapter<String> adapterQuantitys = new ArrayAdapter<>(this, R.layout.row_quantitys, Utils.quantitys);
        binding.quantityEt.setAdapter(adapterQuantitys);

        Intent intent = getIntent();
        isEditMode = intent.getBooleanExtra("isEditMode", true);
        Log.d(TAG, "onCreate: isEditMode: " + isEditMode);

        if (isEditMode) {
            adId = intent.getStringExtra("adId");
            Log.d(TAG, "onCreate: adId: " + adId);

            binding.toolbarTitleTv.setText("Chỉnh sửa sản phẩm");
            binding.giamgiaSC.setChecked(false);
            loadAdDetail();
        } else {
            binding.giamgiaSC.setChecked(false);
            binding.toolbarTitleTv.setText("Thêm sản phẩm");
            binding.postAdBtn.setImageResource(R.drawable.upload);
            binding.raitoEt.setVisibility(View.GONE);
        }

        imagePickedArrayList = new ArrayList<>();
        loadImages();
        Log.d(TAG, "onCreate: loadImages");


        //quay lai
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //chọn hình ảnh
        binding.toolbarImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePickOption();
            }
        });

        //location liên quan đến LocationPickerActivity
        binding.locationEt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ShopAdCreateActivity.this, LocationPickerActivity.class);
                locationPickerActivityResultLauncher.launch(intent);
            }
        });

        //xử lý post
        binding.postAdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                validate();
            }
        });

        //khi tích vào giảm giá
        binding.giamgiaSC.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                //checked, show discountPriceEt, discountNoteEt
//                binding.edtGiamCon.setVisibility(View.VISIBLE);
                binding.raitoEt.setVisibility(View.VISIBLE);
            } else {
                //unchecked, hide discountPriceEt, discountNoteEt
//                binding.edtGiamCon.setVisibility(View.GONE);
                binding.raitoEt.setVisibility(View.GONE);
            }
        });

    }

    private void loadImages() {
        // xử lý chỗ này
        Log.d(TAG, "loadImages: ");
//        imagePickedArrayList = new ArrayList<>();
        adapterImagePicked = new AdapterImagePicked(this, imagePickedArrayList, adId);

        binding.imagesRv.setAdapter(adapterImagePicked);
    }

    private String brand;
    private String category;
    private String condition;
    private String address;
    private String price;
    private String title;
    private String description;
    private double latitude = 0;
    private double longitude = 0;

    private String reducedprice;

    private String raito;
    private String quantity;

    private int raitos, prices, reducedprices, quantitys;

    private boolean giamgia = false;

    private void validate() {
        brand = binding.brandEt.getText().toString().trim();
        category = binding.categoryEt.getText().toString().trim();
        condition = binding.conditionEt.getText().toString().trim();
        address = binding.locationEt.getText().toString().trim();
        price = binding.priceEt.getText().toString();// lưu ý chỗ này
        title = binding.titleEt.getText().toString().trim();
        description = binding.descriptionsEt.getText().toString().trim();
        quantity = binding.quantityEt.getText().toString();
//        reducedprice = binding.edtGiamCon.getText().toString();
        raito = binding.raitoEt.getText().toString();
//        tile = binding.txtTiLe.getText().toString().trim();
        giamgia = binding.giamgiaSC.isChecked();
        Log.d(TAG, "validate: giảm giá: " + giamgia);

        quantitys = Integer.parseInt(quantity);


        if (giamgia) {
            //product is with discount
            reducedprice = String.valueOf((Integer.parseInt(price) * Integer.parseInt(raito) / 100));
            Log.d(TAG, "validate: reducedprice: " + reducedprice);
            reducedprices = Integer.parseInt(price) - Integer.parseInt(reducedprice);
//            binding.edtGiamCon.setText(""+reducedprices);
            raitos = Integer.parseInt(raito);
            prices = Integer.parseInt(price);

        } else {
            //product is without discount
            reducedprice = "0";
            raito = "0";
            reducedprices = Integer.parseInt(reducedprice);
//            binding.edtGiamCon.setText(""+reducedprices);
            raitos = Integer.parseInt(raito);
            prices = Integer.parseInt(price);
        }
        if (brand.isEmpty()) {
            binding.brandEt.setError("Nhập thương hiệu của bạn");
            binding.brandEt.requestFocus();
        } else if (category.isEmpty()) {
            binding.categoryEt.setError("Bạn chưa chọn loại");
            binding.categoryEt.requestFocus();
        } else if (condition.isEmpty()) {
            binding.conditionEt.setError("Bạn chưa chọn tình trạng");
            binding.conditionEt.requestFocus();
        } else if (quantity.isEmpty()) {
            binding.quantityEt.setError("Nhập số lượng sản phẩm");
            binding.quantityEt.requestFocus();
        } else if (address.isEmpty()) {
            binding.locationEt.setError("Bạn chưa chọn vị trí maps cho sản phẩm");
            binding.locationEt.requestFocus();
        } else if (price.isEmpty()) {
            binding.priceEt.setError("Bạn chưa nhập giá cho sản phẩm");
            binding.priceEt.requestFocus();
        } else if (raito.isEmpty()) {
            binding.raitoEt.setError("Bạn chưa nhập % giá giảm");
            binding.raitoEt.requestFocus();
        } else if (title.isEmpty()) {
            binding.titleEt.setError("Bạn chưa nhập tiêu đề");
            binding.titleEt.requestFocus();
        } else if (description.isEmpty()) {
            binding.descriptionsEt.setError("Bạn chưa mô tả sản phẩm");
            binding.descriptionsEt.requestFocus();

        } else if (imagePickedArrayList.isEmpty()) {
            Utils.toastyInfo(this, "Chọn ít nhất trên hình ảnh");
        } else {
            if (isEditMode) {
                updateAd();
                Log.d(TAG, "validate: update: " + isEditMode);
                Log.d(TAG, "validate: updateAd");
            } else {
                Log.d(TAG, "validate: create: " + isEditMode);
                Log.d(TAG, "validate: postAd");
                postAd();
            }

        }
    }

    //thêm
    private void postAd() {
        Log.d(TAG, "postAd: ");

        progressDialog.setMessage("Đang upload quảng cáo");
        progressDialog.show();

        long timestamp = Utils.getTimestamp();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("ProductAds");
        String keyId = reference.push().getKey(); // giữ lại key ID mới tạo để sử dụng cho lần sau


        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id", "" + keyId); //id của Ad mình mới tao ra
        Log.d(TAG, "postAd: id: " + keyId);
        hashMap.put("uid", firebaseAuth.getUid()); // id của tài khoản user
        hashMap.put("brand", brand);
        hashMap.put("category", category);
        hashMap.put("condition", condition);
        hashMap.put("address", address);
        hashMap.put("price", prices);
        hashMap.put("reducedprice", reducedprices);
        hashMap.put("raito", raitos);
        hashMap.put("quantity", quantitys);
        hashMap.put("discount", giamgia);
        hashMap.put("title", title);
        hashMap.put("description", description);
        hashMap.put("timestamp", timestamp);
        hashMap.put("latitude", latitude);
        hashMap.put("longitude", longitude);
        hashMap.put("status", Utils.AD_STATUS_AVAILABLE);


        reference.child(keyId)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: quảng cáo được phát hành");
                        Utils.toastySuccess(ShopAdCreateActivity.this, "Tạo sản phẩm thành công");
                        uploadImageUrl(keyId); //lấy theo Id Ads

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: ", e);
                        progressDialog.dismiss();

                    }
                });

    }

    //chỉnh sửa
    private void updateAd() {
        Log.d(TAG, "updateAd: ");

        progressDialog.setMessage("Đang chỉnh sửa quảng cáo");
        progressDialog.show();
        long timestamp = Utils.getTimestamp();
        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("uid", firebaseAuth.getUid()); // id của tài khoản user
        hashMap.put("brand", brand);
        hashMap.put("category", category);
        hashMap.put("condition", condition);
        hashMap.put("address", address);
        hashMap.put("price", prices);
        hashMap.put("reducedprice", reducedprices);
        hashMap.put("raito", raitos);
        hashMap.put("quantity", quantitys);
        hashMap.put("discount", giamgia);
        hashMap.put("title", title);
        hashMap.put("description", description);
        hashMap.put("timestamp", timestamp);
        hashMap.put("latitude", latitude);
        hashMap.put("longitude", longitude);
        hashMap.put("status", Utils.AD_STATUS_AVAILABLE);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("ProductAds");
        reference.child(adId)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Log.d(TAG, "onSuccess: Chỉnh sửa sản phẩm thành công");
                        Utils.toastySuccess(ShopAdCreateActivity.this, "Chỉnh sửa sản phẩm thành công");
                        uploadImageUrl(adId); // chú ý cái này

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: Lỗi: " + e);
                        progressDialog.dismiss();
                    }
                });

    }

    private void uploadImageUrl(String keyId) {
        for (int i = 0; i < imagePickedArrayList.size(); i++) {

            ModelImagePicked modelImagePicked = imagePickedArrayList.get(i);

            if (!modelImagePicked.getFromInternet()) {
                Log.d(TAG, "uploadImageUrl: !modelImagePicked.getFromInternet()");
                String imageId = modelImagePicked.getId();

                String filePathName = "ProductAds/" + imageId;

                StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathName);
                int imageIndexForProgress = i + 1;

                Log.d(TAG, "uploadImagesStorage: " + imageId);
                Log.d(TAG, "uploadImagesStorage: " + filePathName);
                Log.d(TAG, "uploadImagesStorage: " + imageIndexForProgress);

                storageReference.putFile(modelImagePicked.getImageUri())
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                                double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();

                                String message = "Uploading " + imageIndexForProgress + " of " + imagePickedArrayList.size() + "hình ảnh...\n tiến trình "
                                        + (int) progress + "%";
                                progressDialog.setMessage(message);
                                progressDialog.show();
                            }
                        })
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Log.d(TAG, "onSuccess: ");
                                progressDialog.dismiss();
                                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                                while (!uriTask.isSuccessful()) ;
                                String uploadImageUrl = uriTask.getResult().toString();

                                if (uriTask.isSuccessful()) {
                                    HashMap<String, Object> hashMap = new HashMap<>();
                                    if (uploadImageUrl != null) {
                                        hashMap.put("imageUrl", "" + uploadImageUrl);
                                        hashMap.put("idImageAd", "" + imageId);
                                        Log.d(TAG, "UploadImageStorageUrl: imageUrl: " + uploadImageUrl);
                                        Log.d(TAG, "UploadImageStorageUrl: idImageAd: " + modelImagePicked.getId());
                                    }

                                    //lấy theo id Ads. Chia cây Ads-IdAd->Images->ImageId->ImageData
                                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("ProductAds");
                                    databaseReference.child(keyId).child("Images").child(imageId)
                                            .updateChildren(hashMap);

                                }
                                Utils.toastySuccess(ShopAdCreateActivity.this, "Upload hình ảnh thành công");
                                Intent intentImage = new Intent(ShopAdCreateActivity.this, MainSellerActivity.class);
                                startActivity(intentImage);


                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "onFailure: Lỗi", e);
                                progressDialog.dismiss();
                            }
                        });
            }

        }
    }


    private void showImagePickOption() {
        Log.d(TAG, "showImagePickOption: ");

        PopupMenu popupMenu = new androidx.appcompat.widget.PopupMenu(ShopAdCreateActivity.this, binding.toolbarImageBtn);
        popupMenu.getMenu().add(Menu.NONE, 1, 1, "Camera");
        popupMenu.getMenu().add(Menu.NONE, 2, 2, "Gallery");

        popupMenu.show();

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == 1) {
                    Log.d(TAG, "onMenuItemClick: Mở camera, check camera");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        requestCameraPemissions.launch(new String[]{Manifest.permission.CAMERA});
                    } else {
                        requestCameraPemissions.launch(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE});
                    }
                } else if (itemId == 2) {
                    Log.d(TAG, "onMenuItemClick: Mở storage, check storage");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        pickFromGallery();
                    } else {
                        requestStoragePemissions.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    }
                }
                return false;
            }
        });
    }


    private ActivityResultLauncher<String[]> requestCameraPemissions = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            new ActivityResultCallback<Map<String, Boolean>>() {

                @Override
                public void onActivityResult(Map<String, Boolean> result) {
                    Log.d(TAG, "onActivityResult: " + result.toString());
                    boolean areAllGranted = true;
                    for (Boolean isGranted : result.values()) {
                        areAllGranted = areAllGranted && isGranted;
                    }
                    if (areAllGranted) {
                        Log.d(TAG, "onActivityResult: Tất cả quyền camera & storage");
                        pickFromCamera();
                    } else {
                        Log.d(TAG, "onActivityResult: Tất cả hoặc chỉ có một quyền");
                        Toast.makeText(ShopAdCreateActivity.this, "Quyền camera hoặc storage", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private ActivityResultLauncher<String> requestStoragePemissions = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean isGranted) {
                    if (isGranted) {
                        pickFromGallery();
                    } else {
                        Toast.makeText(ShopAdCreateActivity.this, "Quyền Storage chưa cấp quyền", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private void pickFromGallery() {
        Log.d(TAG, "pickFromGallery: ");
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryActivityResultLaucher.launch(intent);
    }

    private ActivityResultLauncher<Intent> galleryActivityResultLaucher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Log.d(TAG, "onActivityResult: Hình ảnh thư viện: " + imageUri);
                        Intent data = result.getData();
                        imageUri = data.getData();

                        String timestamp = "" + Utils.getTimestamp();

                        ModelImagePicked modelImagePicked = new ModelImagePicked(timestamp, imageUri, null, false);
                        imagePickedArrayList.add(modelImagePicked);

                        loadImages();
                    } else {
                        Toast.makeText(ShopAdCreateActivity.this, "Hủy", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );


    private void pickFromCamera() {
        Log.d(TAG, "pickFromCamera: ");
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Temp_Image Title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp_Image Description");


        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraActivityResultLaucher.launch(intent);

    }

    private ActivityResultLauncher<Intent> cameraActivityResultLaucher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Log.d(TAG, "onActivityResult: Camera" + imageUri);
                        String timestamp = "" + System.currentTimeMillis();

                        ModelImagePicked modelImagePicked = new ModelImagePicked(timestamp, imageUri, null, false);
                        imagePickedArrayList.add(modelImagePicked);

                        loadImages();
                    } else {
                        Toast.makeText(ShopAdCreateActivity.this, "Hủy", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );


    //xử lý locationPickerActivityResultLauncher
    private ActivityResultLauncher<Intent> locationPickerActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Log.d(TAG, "onActivityResult: ");
                        Intent data = result.getData();

                        if (data != null) {
                            latitude = data.getDoubleExtra("latitude", 0.0);
                            longitude = data.getDoubleExtra("longitude", 0.0);
                            address = data.getStringExtra("address");
                            Log.d(TAG, "onActivityResult: latitude" + latitude);
                            Log.d(TAG, "onActivityResult: longitude" + longitude);
                            Log.d(TAG, "onActivityResult: address" + address);

                            binding.locationEt.setText(address);
                        } else {
                            Log.d(TAG, "onActivityResult: Lỗi");
                            Utils.toastyError(ShopAdCreateActivity.this, "Lỗi");
                        }
                    }
                }
            }
    );

    //load toàn bộ sản phẩm cho khi bấm chỉnh sửa
    private void loadAdDetail() {
        Log.d(TAG, "loadDetails: ");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("ProductAds");
        reference.child(adId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        String title = "" + snapshot.child("title").getValue();
                        String brand = "" + snapshot.child("brand").getValue();
                        String description = "" + snapshot.child("description").getValue();
                        String address = "" + snapshot.child("address").getValue();
                        String condition = "" + snapshot.child("condition").getValue();
                        String category = "" + snapshot.child("category").getValue();
                        boolean discount = Boolean.parseBoolean("" + snapshot.child("discount").getValue());
                        String quantity = "" + snapshot.child("quantity").getValue();
                        String reducedprice = "" + snapshot.child("reducedprice").getValue();
                        String raito = "" + snapshot.child("raito").getValue();
                        String price = "" + snapshot.child("price").getValue();
                        latitude = (Double) snapshot.child("latitude").getValue();
                        longitude = (Double) snapshot.child("longitude").getValue();

                        Log.d(TAG, "onDataChange: price: " + price);
                        Log.d(TAG, "onDataChange: price: " + reducedprice);


                        binding.brandEt.setText(brand);
                        binding.categoryEt.setText(category);
                        binding.conditionEt.setText(condition);
                        binding.priceEt.setText(price);
                        binding.locationEt.setText(address);
                        binding.descriptionsEt.setText(description);
                        binding.titleEt.setText(title);
                        binding.quantityEt.setText(quantity);

                        binding.raitoEt.setText(raito);

                        if (discount) {
                            binding.giamgiaSC.setChecked(true);
                            binding.raitoEt.setVisibility(View.VISIBLE);
                        } else {
                            binding.giamgiaSC.setChecked(false);
                            binding.raitoEt.setVisibility(View.GONE);
                        }


                        DatabaseReference ref = snapshot.child("Images").getRef();
                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot ds : snapshot.getChildren()) {
                                    String id = "" + ds.child("idImageAd").getValue();
                                    String imageUrl = "" + ds.child("imageUrl").getValue();

                                    Log.d(TAG, "onDataChange: idImage: " + id);
                                    Log.d(TAG, "onDataChange: imageUrl: " + imageUrl);

                                    ModelImagePicked modelImagePicked = new ModelImagePicked(id, null, imageUrl, true);
                                    imagePickedArrayList.add(modelImagePicked);

                                }
                                loadImages(); // xử lý chỗ này
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
    }

}