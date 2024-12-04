package com.example.olx.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.example.olx.R;
import com.example.olx.Utils;
import com.example.olx.activities.ChangePasswordActivity;
import com.example.olx.activities.DeleteAccountActivity;
import com.example.olx.activities.LoginOptionActivity;
import com.example.olx.activities.ProfileEditSellerActivity;
import com.example.olx.activities.ProfileEditActivity;
import com.example.olx.activities.ShopAdDetailsActivity;
import com.example.olx.databinding.FragmentProfileBinding;
import com.example.olx.model.ModelUsers;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;


public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";

    private FragmentProfileBinding binding;

    private ProgressDialog progressDialog;
    private Context mContext;

    private FirebaseAuth firebaseAuth;

    private EasyDB easyDB;
    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        mContext = context;
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(LayoutInflater.from(mContext), container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(mContext);
        progressDialog.setMessage("Vui lòng đợi trong giây lát");
        progressDialog.setCanceledOnTouchOutside(false);
        loadMyInfo();

        //log out
        binding.logoutCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //còn ko muốn thì đóng dòng xoaGioHang lại thì nó sẽ vẫn load sản phẩm người này đặt
                //khi đăng nhập tài khoản khác
                firebaseAuth.signOut();
//                xoaGioHang(); //đăng xuất xóa giỏ hàng
                startActivity(new Intent(mContext, LoginOptionActivity.class));
                getActivity().finishAffinity();

            }
        });

        //chỉnh sửa proflie
        binding.editprofileCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, ProfileEditActivity.class));
            }
        });

        //đổi mật khẩu
        binding.changePasswordCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, ChangePasswordActivity.class));
                getActivity().finishAffinity();
            }
        });

        //xác minh tài khoản
        binding.verifiedCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifiedAccount();
            }
        });

        //xoá tài khoản
        binding.deleteAccountCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, DeleteAccountActivity.class));
                getActivity().finishAffinity();
            }
        });



        return binding.getRoot();
    }
    //Xoá giỏ hàng
    public void xoaGioHang() {
        // Xóa hết sp khỏi giỏ
        //declare it to class level and init in onCreate
        easyDB = EasyDB.init(mContext, "GIOHANG_DB")
                .setTableName("GIOHANG_TABLE")
                .addColumn(new Column("GH_Id", "text", "unique"))
                .addColumn(new Column("GH_PID", "text", "not null"))
                .addColumn(new Column("GH_Title", "text", "not null"))
                .addColumn(new Column("GH_Price", "text", "not null"))
                .addColumn(new Column("GH_Quantity", "text", "not null"))
                .addColumn(new Column("GH_Soluongdadat", "text", "not null"))
                .addColumn(new Column("GH_FinalPrice", "text", "not null"))
                .addColumn(new Column("GH_UidNguoiBan", "text", "not null"))
                .addColumn(new Column("GH_UidNguoiMua", "text", "not null"))
                .doneTableColumn();
        easyDB.deleteAllDataFromTable();
    }

    private void loadMyInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ModelUsers user = snapshot.getValue(ModelUsers.class);
                        String name = "" + snapshot.child("name").getValue();
                        String email = "" + snapshot.child("email").getValue();
                        String dob = "" + snapshot.child("dob").getValue();
                        String online = "" + snapshot.child("online").getValue();
                        String shopOpen = "" + snapshot.child("shopOpen").getValue();
                        String profileImage = "" + snapshot.child("profileImage").getValue();
                        String accountType = "" + snapshot.child("accountType").getValue();
                        String timestamp = "" + snapshot.child("timestamp").getValue();
                        String phone = "" + snapshot.child("phone").getValue();
                        String address = "" + snapshot.child("address").getValue();


                        //thời gian
                        if (timestamp.equals("null")) {
                            timestamp = "0";
                        }
                        //ngày-tháng-năm
                        String formattedDate = Utils.formatTimestampDate(Long.parseLong(timestamp));

                        //set data
                        binding.nameTv.setText(name);
                        binding.emailTv.setText(email);
                        binding.dobTv.setText(dob);
                        binding.phoneTv.setText(phone);
                        binding.addressTv.setText(address);
                        binding.memberSignleTv.setText(formattedDate);

                        //xử lý trạng thái tài khoản
                        if (accountType.equals("User")) {
                            boolean isVerified = firebaseAuth.getCurrentUser().isEmailVerified();
                            if (isVerified) {
                                binding.verifiedCv.setVisibility(View.GONE);
                                binding.verifiedTv.setText("Đã xác minh");
                            } else {
                                binding.verifiedCv.setVisibility(View.VISIBLE);
                                binding.verifiedTv.setText("Chưa xác minh");

                            }
                        }
                        else {
                            binding.verifiedCv.setVisibility(View.GONE);
                            binding.verifiedTv.setText("Đã xác minh");
                        }

                        //hình ảnh profile
                        try {
                            Glide.with(mContext)
                                    .load(profileImage)
                                    .placeholder(R.drawable.olx_trangbia)
                                    .into(binding.profileIv);
                        } catch (Exception e) {
                            Log.d(TAG, "onDataChange: " + e);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void verifiedAccount() {
        Log.d(TAG, "verifiedAccount: ");
        progressDialog.setMessage("Xác minh tài khoản email");
        progressDialog.show();

        firebaseAuth.getCurrentUser().sendEmailVerification()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: Send");
                        progressDialog.dismiss();
                        Utils.toastySuccess(mContext, "Xác minh thành công");

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onSuccess: Send");
                        progressDialog.dismiss();
                        Utils.toastySuccess(mContext, "Thất bại: " + e);
                    }
                });
    }
}