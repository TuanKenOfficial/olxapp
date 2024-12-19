package com.example.olx.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.olx.Utils;
import com.example.olx.adapter.AdapterAddProduct;
import com.example.olx.databinding.FragmentMyAdsFavBinding;
import com.example.olx.model.ModelAddProduct;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class MyAdsFavFragment extends Fragment {

    private FragmentMyAdsFavBinding binding;

    private FirebaseAuth firebaseAuth;

    private ArrayList<ModelAddProduct> adArrayList;

    private AdapterAddProduct adapterAddProduct;

    private Context mContext;

    private static final String TAG = "MYFAV";

    private String id;

    public MyAdsFavFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        mContext = context;
        super.onAttach(context);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentMyAdsFavBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();

        loadAdsFav();


        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    String query = s.toString();
                    adapterAddProduct.getFilter().filter(query);
                } catch (Exception e) {
                    Log.d(TAG, "onTextChanged: Lỗi: " + e);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    //load mục yêu thích
    private void loadAdsFav() {
        Log.d(TAG, "loadAds: ");

        adArrayList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Favorites");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                adArrayList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String idAds = ""+ds.child("idAds").getValue();
                    Log.d(TAG, "onDataChange: idAds: "+idAds);

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ProductAds");
                    ref.orderByChild("id").equalTo(idAds)
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {

                                    for (DataSnapshot ds : snapshot.getChildren()){
                                        try {
                                            Log.d(TAG, "onDataChange: Load sản pẩm thành công");
                                            ModelAddProduct modelAddProduct = ds.getValue(ModelAddProduct.class);
                                            modelAddProduct.setId(idAds);
                                            adArrayList.add(modelAddProduct);
                                        }catch (Exception e){
                                            Utils.toastyError(mContext,"Lỗi: "+e);
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });


                }

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        adapterAddProduct = new AdapterAddProduct(mContext, adArrayList);
                        binding.favRv.setAdapter(adapterAddProduct);
                    }
                }, 500);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}