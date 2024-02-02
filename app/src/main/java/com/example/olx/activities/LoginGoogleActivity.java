package com.example.olx.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.olx.R;
import com.example.olx.Utils;
import com.example.olx.databinding.ActivityLoginGoogleBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
/** @noinspection ALL*/
public class LoginGoogleActivity extends AppCompatActivity {

    private ActivityLoginGoogleBinding binding;
    private FirebaseAuth firebaseAuth;
    private GoogleSignInClient googleSignInClient;
    private GoogleSignInOptions signInOptions;
    private static final String TAG ="LOGINGOOGLE";

    public LoginGoogleActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginGoogleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        firebaseAuth = FirebaseAuth.getInstance();

        signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(LoginGoogleActivity.this,signInOptions);
        binding.btnThoatGG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        binding.btnLoginGG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleSingin();
            }
        });

    }

    private void googleSingin() {
        Log.d(TAG, "googleSingin: ");
        Intent intent = googleSignInClient.getSignInIntent();
        startActivityForResult(intent,0);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0){
            Log.d(TAG, "onActivityResult: ");
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                Log.d(TAG, "onActivityResult: try");
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuth(account.getIdToken());
            }catch (Exception e){
                Utils.toastyInfo(LoginGoogleActivity.this,""+e.getMessage());
            }
        }
    }

    private void firebaseAuth(String idToken) {
        Log.d(TAG, "firebaseAuth: ");
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken,null);
        firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        if (authResult.getAdditionalUserInfo().isNewUser()){
                            Log.d(TAG, "onSuccess: Đang update dữ liệu và tạo tài khoản");
                            Utils.toastySuccess(LoginGoogleActivity.this, "Đang update dữ liệu");
                            updateDatabase();
                        }
                        else {
                            Log.d(TAG, "onDataChange: tài khoản google đã đăng nhập");
//                            firebaseAuth.signOut();
                            Utils.toastySuccess(LoginGoogleActivity.this, "Tài khoản google đã đăng nhập trước đó");
                            startActivity(new Intent(LoginGoogleActivity.this,MainUserActivity.class));
                        }

                    }


                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: Lỗi: ", e);
                    }
                });

    }

    private void updateDatabase() {

        String timestamp = String.valueOf(Utils.getTimestamp());
        String registerUserEmail = firebaseAuth.getCurrentUser().getEmail();
        String registerUserUid = firebaseAuth.getUid();

        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("shopName", "shopName");
        hashMap.put("phone", "+84123456789");
        hashMap.put("address", "");
        hashMap.put("latitude", 0.0);
        hashMap.put("longitude", 0.0);
        hashMap.put("timestamp", "" + timestamp);
        hashMap.put("online", "true");
        hashMap.put("shopOpen", "true");
        hashMap.put("name","Google");
        hashMap.put("profileImageUrl","");
        hashMap.put("dob","Google");
        hashMap.put("accountType","Google");
        hashMap.put("timestamp",timestamp);
        hashMap.put("email", registerUserEmail);
        hashMap.put("uid", registerUserUid);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(registerUserUid)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Utils.toastySuccess(LoginGoogleActivity.this, "Tạo tài khoản thành công");
                        Log.d(TAG, "onSuccess: Thành công");
                        startActivity(new Intent(LoginGoogleActivity.this, MainUserActivity.class));
                        finishAffinity();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Utils.toastyError(LoginGoogleActivity.this,"Lỗi: "+e);
                        Log.d(TAG, "onFailure: Lỗi: "+e);
                    }
                });
    }
}