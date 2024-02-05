package com.example.olx.fragment;

import android.content.Context;
import android.os.Bundle;
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
import com.example.olx.databinding.FragmentMyAdsAdBinding;
import com.example.olx.model.ModelAddProduct;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class MyAdsAdFragment extends Fragment {

    private FragmentMyAdsAdBinding binding;

    private FirebaseAuth firebaseAuth;

    private ArrayList<ModelAddProduct> adArrayList;

    private AdapterAddProduct adapterAd;

    private Context mContext;

    private static final String TAG = "MYADS";

    public MyAdsAdFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        mContext =context;
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentMyAdsAdBinding.inflate(inflater, container,false);


        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();

        loadAds();

        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    String query = s.toString();
                    adapterAd.getFilter().filter(query);
                }
                catch (Exception e){
                    Log.d(TAG, "onTextChanged: Lỗi: "+e);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void loadAds() {
        Log.d(TAG, "loadAds: ");

        adArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ProductAds");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        adArrayList.clear();

                        for (DataSnapshot ds : snapshot.getChildren()){
                            try {
                                Log.d(TAG, "onDataChange: Load sản pẩm thành công");
                                ModelAddProduct modelAddProduct = ds.getValue(ModelAddProduct.class);
                                adArrayList.add(modelAddProduct);
                            }catch (Exception e){
                                Utils.toastyError(mContext,"Lỗi: "+e);
                            }
                        }
                        adapterAd = new AdapterAddProduct(mContext,adArrayList);
                        binding.adsRv.setAdapter(adapterAd);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}