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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.olx.R;
import com.example.olx.Utils;
import com.example.olx.databinding.ActivityProfileEditBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class ProfileEditActivity extends AppCompatActivity {
    //view binding
    private ActivityProfileEditBinding binding;

    //firebase user
    private FirebaseUser firebaseUser;
    private FirebaseAuth firebaseAuth;

    private ProgressDialog progressDialog;

    private static final String TAG = "ProfileEdit";

    //image picked uri
    private Uri image_uri;
    private String imageUrl = "" ;

    private String name ="";
    private String email="";
    private String dob="";
    private String password="";
    private String phoneCode="";
    private String phoneNumber="";
    private String address="";


    private  String mUserType="";
    private double latitude = 0;
    private double longitude = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Vui lòng đợi trong giây lát");
        progressDialog.setCanceledOnTouchOutside(false);

        //setup firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        //load tất cả hồ sơ profile
        loadUserInfo();



        //handle click, button back
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileEditActivity.this,MainUserActivity.class));
            }
        });

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: bấm vào thay đổi ảnh");
                imageDialog();
            }
        });

        //handle click, button upload
        binding.updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validate();

            }
        });
        binding.addressEt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileEditActivity.this, LocationPickerActivity.class);
                locationPickerActivityResultLauncher.launch(intent);
            }
        });
    }
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

                            binding.addressEt.setText(address);
                        } else {
                            Log.d(TAG, "onActivityResult: Lỗi");
                            Utils.toastyError(ProfileEditActivity.this, "Lỗi");
                        }
                    }
                }
            }
    );
    private String phone;

    private void validate() {
        email = binding.emailEt.getText().toString().trim();
        name = binding.nameEt.getText().toString().trim();
        address = binding.addressEt.getText().toString().trim();
        dob = binding.dobEt.getText().toString().trim();
        phoneNumber = binding.phoneNumber.getText().toString().trim();
        phone = phoneCode + phoneNumber;
        //coi nghiên cứu sửa khúc này, có liên quan đến dòng 209
        if (imageUrl == null){
            Log.d(TAG, "validate: null");
            uploadProfileDb("https://firebasestorage.googleapis.com/v0/b/olxs-36d58.appspot.com/o/olx_trangbia.png?alt=media&token=84013b87-58da-401a-aa0d-cfced0202739");
        }
        else {
            Log.d(TAG, "validate: ");
            uploadProfileImageStorageDb();
            uploadProfileDb(imageUrl);
        }

    }

    private void uploadProfileImageStorageDb() {
        if (image_uri !=null){
            progressDialog.setMessage("Upload");
            progressDialog.show();
            Log.d(TAG, "updateAnh: Đã vô tới update ảnh");
            //name and path of image
            String filePathAndName = "profile_images/" + "profile_" + firebaseAuth.getUid();
            Log.d(TAG, "updateAnh: " + filePathAndName);
            //upload image
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(filePathAndName);
            storageReference.putFile(image_uri)
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                            double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                            Log.d(TAG, "onProgress: " + progress);
                            progressDialog.setMessage("Upload hình ảnh. Tiến triển: " + (int) progress + "%");

                        }
                    })
                    .addOnSuccessListener(taskSnapshot -> {
                        //get url of uploaded image
                        Log.d(TAG, "uploadProfileImageStorageDb: Upload thành công...");
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful()) ;
                        String uploadImageUrl = uriTask.getResult().toString();
                        if (uriTask.isSuccessful()) {
                            uploadProfileDb(uploadImageUrl);
                            Utils.toastySuccess(ProfileEditActivity.this, "Đang load ảnh");
                        }


                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Log.d(TAG, "onFailure: Lỗi: " + e);
                            Utils.toastyError(ProfileEditActivity.this, "Lỗi" + e);
                        }
                    });
        }

    }

    private void uploadProfileDb(String uploadImageUrl) {
        Log.d(TAG, "uploadProfileDb: ");
        progressDialog.setMessage("Update user");
        progressDialog.show();

        String registerUserUid = firebaseAuth.getUid();
        String timestamp = String.valueOf(Utils.getTimestamp());
        String registerUserEmail = firebaseAuth.getCurrentUser().getEmail();
        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("name", "" + name);
        hashMap.put("dob", "" + dob);
        hashMap.put("latitude", latitude);
        hashMap.put("longitude", longitude);
        hashMap.put("address", "" + address);
        hashMap.put("timestamp", timestamp);
        hashMap.put("online", "true");
        hashMap.put("uid", registerUserUid);
        hashMap.put("shopOpen", "true");
        hashMap.put("phone", "" + phone);
        hashMap.put("profileImage", "" + uploadImageUrl);

        if (mUserType.equalsIgnoreCase("User")){
            hashMap.put("accountType", "User");
            hashMap.put("email", registerUserEmail);
        } else if (mUserType.equalsIgnoreCase("Google")) {
            hashMap.put("accountType", "Google");
            hashMap.put("email", email);
        } else if (mUserType.equalsIgnoreCase("Phone")) {
            hashMap.put("accountType", "Phone");
            hashMap.put("email", email);
        }
        Log.d(TAG, "uploadProfileDb: profileImage:" + imageUrl);
        Log.d(TAG, "uploadProfileDb: name:" + name);
        Log.d(TAG, "uploadProfileDb: dob:" + dob);
        Log.d(TAG, "uploadProfileDb: latitude:" + latitude);
        Log.d(TAG, "uploadProfileDb: longitude:" + longitude);
        Log.d(TAG, "uploadProfileDb: timestamp:" + timestamp);
        Log.d(TAG, "uploadProfileDb: address:" + address);
        Log.d(TAG, "uploadProfileDb: registerUserUid:" + registerUserUid);
        Log.d(TAG, "uploadProfileDb: registerUserEmail:" + registerUserEmail);
        Log.d(TAG, "uploadProfileDb: phone:" + phone);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(firebaseAuth.getUid())
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Utils.toastySuccess(ProfileEditActivity.this, " Upload thành công hồ sơ");
                        Log.d(TAG, "onSuccess: Thành công");

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Utils.toastyError(ProfileEditActivity.this, "Lỗi: " + e);
                        Log.d(TAG, "onFailure: Lỗi: " + e);
                    }
                });
    }



    //Xử lý hình ảnh chỉnh sửa hồ sơ, cập nhật ảnh
    private void imageDialog(){
        PopupMenu popupMenu = new PopupMenu(ProfileEditActivity.this,binding.profileIv);
        popupMenu.getMenu().add(Menu.NONE,1,1,"Camera");
        popupMenu.getMenu().add(Menu.NONE,2,2,"Gallery");

        popupMenu.show();

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId();
                if (itemId ==1){
                    Log.d(TAG, "onMenuItemClick: Mở camera, check camera");
                    if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.TIRAMISU){
                        requestCameraPemissions.launch(new String[]{Manifest.permission.CAMERA});
                    }else {
                        requestCameraPemissions.launch(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE});
                    }
                }
                else if (itemId==2){
                    Log.d(TAG, "onMenuItemClick: Mở storage, check storage");
                    if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.TIRAMISU){
                        pickFromGallery1();
                    }else {
                        requestStoragePemissions.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    }
                }
                return false;
            }
        });
    }
    private ActivityResultLauncher<String[]> requestCameraPemissions = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            new ActivityResultCallback<Map<String,Boolean>>(){

                @Override
                public void onActivityResult(Map<String, Boolean> result) {
                    Log.d(TAG, "onActivityResult: "+result.toString());
                    boolean areAllGranted = true;
                    for (Boolean isGranted: result.values()){
                        areAllGranted = areAllGranted && isGranted;
                    }
                    if (areAllGranted){
                        Log.d(TAG, "onActivityResult: Tất cả quyền camera & storage");
                        pickFromCamera1();
                    }
                    else {
                        Log.d(TAG, "onActivityResult: Tất cả hoặc chỉ có một quyền");
                        Toast.makeText(ProfileEditActivity.this, "Quyền camera hoặc storage", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private ActivityResultLauncher<String> requestStoragePemissions = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean isGranted) {
                    if (isGranted){
                        pickFromGallery1();
                    }
                    else {
                        Toast.makeText(ProfileEditActivity.this, "Quyền Storage chưa cấp quyền", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private void pickFromGallery1() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryActivityResultLaucher.launch(intent);
    }
    private ActivityResultLauncher<Intent> galleryActivityResultLaucher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK){
                        Log.d(TAG, "onActivityResult: Hình ảnh thư viện: "+image_uri);
                        Intent data = result.getData();
                        image_uri = data.getData();
                        try {
                            Glide.with(ProfileEditActivity.this)
                                    .load(image_uri)
                                    .placeholder(R.drawable.ic_users)
                                    .into(binding.profileIv);
                        }catch (Exception e){
                            Log.d(TAG, "onActivityResult: "+e);
                            Toast.makeText(ProfileEditActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {
                        Toast.makeText(ProfileEditActivity.this, "Hủy", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );


    private void pickFromCamera1() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Temp_Image Title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp_Image Description");

        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        cameraActivityResultLaucher.launch(intent);

    }
    private ActivityResultLauncher<Intent> cameraActivityResultLaucher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK){
                        Log.d(TAG, "onActivityResult: Hình ảnh: "+image_uri);
                        try {
                            Glide.with(ProfileEditActivity.this)
                                    .load(image_uri)
                                    .placeholder(R.drawable.ic_users)
                                    .into(binding.profileIv);
                        }catch (Exception e){
                            Log.d(TAG, "onActivityResult: "+e);
                            Toast.makeText(ProfileEditActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {
                        Toast.makeText(ProfileEditActivity.this, "Hủy", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    //load tất cả những thông tin cần có của phần profile
    private void loadUserInfo() {
        Log.d(TAG, "loadUserInfo: Đang tải thông tin người dùng của người dùng");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get all info of user here from snapshot
                        String email = "" + snapshot.child("email").getValue();
                        String name = "" + snapshot.child("name").getValue();
                        String timestamp = "" + snapshot.child("timestamp").getValue();
                        String profileImage = "" + snapshot.child("profileImage").getValue();
                        String address = "" + snapshot.child("address").getValue();
                        String uid = "" + snapshot.child("uid").getValue();
                        String dob = "" + snapshot.child("dob").getValue();
                        mUserType = "" + snapshot.child("accountType").getValue();
                        String phone = "" + snapshot.child("phone").getValue();

                        if (timestamp.equals("null")){
                            timestamp ="0";
                        }

                        //format date
                        String formattedDate = Utils.formatTimestampDate(Long.parseLong(timestamp));

                        /*Có 3 loại tài khoản
                         * Nếu tài khoản Email thì sau khi đăng ký tài khoản xong người dùng không sửa đổi Email của mình
                         * Nếu tài khoản Phone thì sau khi đăng nhập tài khoản bằng sđt xong người dùng không sửa đổi Phone của mình
                         * Nếu tài khoản Facebook thì sau khi đăng nhập tài khoản xong người dùng có thể sửa đổi Email và Phone của mình
                         * Ở đây chỗ tài khoản facebook mình muốn nếu người dùng đăng nhập bằng sdt không chỉnh sửa sdt, và ngược lại email củng vậy,
                         * nhưng mình chưa có thời gian làm
                         */

                        if(mUserType.equalsIgnoreCase("User")){
                            binding.emailEt.setEnabled(false); //không cho người dùng thay đổi email
                            binding.addressEt.setEnabled(true);
                            binding.phoneNumber.setEnabled(true);
                            binding.phoneInputRl.setEnabled(true);
                            binding.memberSingleEt.setEnabled(false);

                        } else if (mUserType.equalsIgnoreCase("Phone")){
                            binding.emailEt.setEnabled(true);
                            binding.addressEt.setEnabled(true);
                            binding.phoneNumber.setEnabled(false);
                            binding.phoneInputRl.setEnabled(false);
                            binding.memberSingleEt.setEnabled(false);
                        }
                        else if (mUserType.equalsIgnoreCase("Google")){
                            binding.phoneNumber.setEnabled(true);
                            binding.phoneInputRl.setEnabled(true);
                            binding.addressEt.setEnabled(true);
                            binding.emailEt.setEnabled(false);
                            binding.memberSingleEt.setEnabled(false);
                        }
                        //set data to ui
                        binding.nameEt.setText(name);
                        binding.emailEt.setText(email);
                        binding.dobEt.setText(dob);
                        binding.phoneNumber.setText(phone);
                        binding.addressEt.setText(address);
                        binding.memberSingleEt.setText(formattedDate);

                        try {
                            Glide.with(ProfileEditActivity.this)
                                    .load(profileImage)
                                    .placeholder(R.drawable.shop)
                                    .into(binding.profileIv);
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

}