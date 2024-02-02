package com.example.olx.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.olx.R;
import com.example.olx.Utils;
import com.example.olx.activities.LocationPickerActivity;
import com.example.olx.adapter.AdapterAddProduct;

import com.example.olx.databinding.FragmentHomeSellerBinding;
import com.example.olx.databinding.FragmentHomeUserBinding;
import com.example.olx.model.ModelAddProduct;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class HomeUserFragment extends Fragment {

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private FragmentHomeUserBinding binding;

    private static final String TAG = "HOME_TAG";

    private Context mContext;

    private ArrayList<ModelAddProduct> productList;
    private AdapterAddProduct adapterAddProduct;

//    private ArrayList<ModelOrderShop> orderShopArrayList;
//    private AdapterOrderShop adapterOrderShop;

    private static final int MAX_DISTANCE_TO_LOAD_ADS_KM = 10;
    private double currentLatitude = 0.0;
    private double currentLongitude = 0.0;
    private String currentAddress = "";

    private String category;

    private SharedPreferences locationSp;

    @Override
    public void onAttach(@NonNull Context context) {
        mContext = context;
        super.onAttach(context);
    }

    public HomeUserFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // Inflate the layout for this fragment
        binding = FragmentHomeUserBinding.inflate(LayoutInflater.from(mContext), container, false);
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d(TAG, "onViewCreated: ");
        firebaseAuth = FirebaseAuth.getInstance();
        //hiển thị vị trí & địa chỉ
        locationSp = mContext.getSharedPreferences("LOCATION_SP", Context.MODE_PRIVATE);
        currentLatitude = locationSp.getFloat("CURRENT_LATITUDE", 0.0f);
        currentLongitude = locationSp.getFloat("CURRENT_LONGITUDE", 0.0f);
        currentAddress = locationSp.getString("CURRENT_ADDRESS", "");

        if (currentLatitude != 0.0 && currentLongitude != 0.0) {
            Log.d(TAG, "onViewCreated: " + currentAddress);
            binding.locationTv.setText(currentAddress);
        }

        loadAllAdProducts();
        showProductsUI();

        binding.tabProductsTv.setOnClickListener(v -> {
            //load products
            showProductsUI();
        });
        binding.tabOrdersTv.setOnClickListener(v -> {
            //load orders
            showOrdersUI();
        });
        binding.searchProductEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                try {
                    Log.d(TAG, "onTextChanged: CharSequence: " + s);
                    String query = s.toString();
                    Log.d(TAG, "onTextChanged: query:" + query);
                    adapterAddProduct.getFilter().filter(query);
                } catch (Exception e) {
                    Log.d(TAG, "onTextChanged: Lỗi: " + e);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.filterProductBtn.setOnClickListener(v -> {
            Log.d(TAG, "onViewCreated: " + binding.filteredProductsTv);
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle("Sản phẩm:")
                    .setItems(Utils.categories, (dialog, which) -> {
                        //get selected item
                        String selected = Utils.categories[which];
                        binding.filteredProductsTv.setText(selected);
                        loadFilteredProducts(selected);
                    })
                    .show();
        });
        binding.filterOrderBtn.setOnClickListener(v -> {
            //options to display in dialog
            Log.d(TAG, "onViewCreated: ");
            final String[] options = {"Tất cả", "Chưa duyệt", "Đã duyệt", "Đã hủy"};
            //dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle("Đơn hàng:")
                    .setItems(options, (dialog, which) -> {
                        //handle item clicks
                        if (which == 0) {
                            //All clicked
//                            filteredOrdersTv.setText("Hiển thị tất cả các đơn đặt hàng");
//                            adapterOrderShop.getFilter().filter(""); //show all orders
                        } else {
                            String optionClicked = options[which];
//                            filteredOrdersTv.setText("Hiển thị "+optionClicked+" Đơn hàng"); //e.g. Showing Completed Orders
//                            adapterOrderShop.getFilter().filter(optionClicked);
                        }
                    })
                    .show();
        });


        binding.locationCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: LocationCv");
                Intent intent = new Intent(mContext, LocationPickerActivity.class);
                locationPickerActivityResult.launch(intent);
            }
        });


    }


    private void showProductsUI() {
//show products ui and hide orders ui
        binding.productsRl.setVisibility(View.VISIBLE);
        binding.ordersRl.setVisibility(View.GONE);
        binding.tabProductsTv.setTextColor(getResources().getColor(R.color.colorred));
        binding.tabProductsTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        binding.tabOrdersTv.setTextColor(getResources().getColor(R.color.colorblack));
        binding.tabOrdersTv.setBackgroundResource(R.drawable.shape_rec04);

    }

    private void showOrdersUI() {
        binding.productsRl.setVisibility(View.GONE);
        binding.ordersRl.setVisibility(View.VISIBLE);
        binding.tabProductsTv.setTextColor(getResources().getColor(R.color.colorblack));
        binding.tabProductsTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        binding.tabOrdersTv.setTextColor(getResources().getColor(R.color.colorgold));
        binding.tabOrdersTv.setBackgroundResource(R.drawable.shape_rec04);
    }

    private void loadFilteredProducts(String selected) {
        Log.d(TAG, "loadFilteredProducts: ");
        productList = new ArrayList<>();

        //get all products
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("ProductAds");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //before getting reset list
                productList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelAddProduct modelAddProduct = ds.getValue(ModelAddProduct.class);
                    double distance = calculateDistanceKm(modelAddProduct.getLatitude(), modelAddProduct.getLongitude());
                    Log.d(TAG, "onDataChange: distance: " + distance);
                    //if selected category matches product category then add in list

                    if (selected.equals(modelAddProduct.getCategory())) {
                        if (distance <= MAX_DISTANCE_TO_LOAD_ADS_KM) {
                            Log.d(TAG, "onDataChange: category: " + modelAddProduct.getCategory());
                            productList.add(modelAddProduct);
                        }
                    }


                }
                //setup adapter
                adapterAddProduct = new AdapterAddProduct(mContext, productList);
                //set adapter
                binding.productsRv.setAdapter(adapterAddProduct);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private ActivityResultLauncher<Intent> locationPickerActivityResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Log.d(TAG, "onActivityResult: RESULT_OK");
                        Intent data = result.getData();
                        if (data != null) {
                            Log.d(TAG, "onActivityResult: LocationPicker");
                            currentLatitude = data.getDoubleExtra("latitude", 0.0);
                            currentLongitude = data.getDoubleExtra("longitude", 0.0);
                            currentAddress = data.getStringExtra("address");

                            locationSp.edit().putFloat("CURRENT_LATITUDE", Float.parseFloat("" + currentLatitude))
                                    .putFloat("CURRENT_LONGITUDE", Float.parseFloat("" + currentLongitude))
                                    .putString("CURRENT_ADDRESS", currentAddress).apply();

                            Log.d(TAG, "onActivityResult: " + locationSp);

                            binding.locationTv.setText(currentAddress);
                            loadAllAdProducts();
                        }
                    }
                }
            }
    );

    //load tất cả sản phẩm
    private void loadAllAdProducts() {
        Log.d(TAG, "loadAllAdProducts: ");
        productList = new ArrayList<>();
        //get all products
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("ProductAds");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //before getting reset list
                productList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                    ModelAddProduct modelProduct = ds.getValue(ModelAddProduct.class);
                    productList.add(modelProduct);

                }
                //setup adapter
                adapterAddProduct = new AdapterAddProduct(mContext, productList);
                //set adapter
                binding.productsRv.setAdapter(adapterAddProduct);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private double calculateDistanceKm(double adlatitude, double adlongitude) {
        Log.d(TAG, "calculateDistanceKm: currentLatitude: " + currentLatitude);
        Log.d(TAG, "calculateDistanceKm: currentLongitude: " + currentLongitude);
        Log.d(TAG, "calculateDistanceKm: adlatitude: " + adlatitude);
        Log.d(TAG, "calculateDistanceKm: adlongitude: " + adlongitude);

        //vị trí nguồn vị trí của người dùng
        Location startPoint = new Location(LocationManager.NETWORK_PROVIDER);
        startPoint.setLatitude(currentLatitude);
        startPoint.setLongitude(currentLongitude);

        //vị trí cuối, vị trí quảng cáo
        Location endPoint = new Location(LocationManager.NETWORK_PROVIDER);
        endPoint.setLatitude(adlatitude);
        endPoint.setLongitude(adlongitude);

        //tính khoảng cách
        double distanceInMeters = startPoint.distanceTo(endPoint);
        double distanceInKm = distanceInMeters / 1000; //1km = 1000m
        Log.d(TAG, "calculateDistanceKm: distanceInMeters: " + distanceInMeters);
        Log.d(TAG, "calculateDistanceKm: distanceInKm: " + distanceInKm);

        return distanceInKm;
    }
}