package com.example.olx.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import com.example.olx.R;
import com.example.olx.Utils;
import com.example.olx.databinding.ActivityLocationPickerBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class LocationPickerActivity extends AppCompatActivity implements OnMapReadyCallback {

    private ActivityLocationPickerBinding binding;

    private static final String TAG = "LOCATION_PICKER_TAG";

    private static final int DEFAULT_ZOOM = 15;

    private GoogleMap mGoogleMap = null;

    private PlacesClient mPlacesClient;

    private FusedLocationProviderClient mFusedLocationProviderClient;

    private Location mLastKnownLocation = null;

    private Double selectedLatitude = null;
    private Double selectedLongitude = null;
    private String selectedAddress = "";
    private String title;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLocationPickerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.doneLl.setVisibility(View.GONE);

        //maps
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);
        Log.d(TAG, "onCreate: " + mapFragment);

        //địa điểm
        Places.initialize(LocationPickerActivity.this, getString(R.string.google_map_api_key)); //trong video là google_api_key

        mPlacesClient = Places.createClient(this);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);


        //xử lý tìm kiếm maps => ở đây bị lỗi do tôi chưa co tiền để bật thanh toán google cloud
//        AutocompleteSupportFragment autocompleteSupportFragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
//        Place.Field[] placeList = new Place.Field[]{Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS,
//                Place.Field.LAT_LNG};
//        autocompleteSupportFragment.setPlaceFields(Arrays.asList(placeList));
//        if (autocompleteSupportFragment != null) {
//
//            autocompleteSupportFragment.setPlaceFields(Arrays.asList(placeList));
//
//            Log.d(TAG, "onCreate: " + autocompleteSupportFragment);
//            Log.d(TAG, "onCreate: " + Place.Field.ID);
//            Log.d(TAG, "onCreate: " + Place.Field.NAME);
//            Log.d(TAG, "onCreate: " + Place.Field.ADDRESS);
//            Log.d(TAG, "onCreate: " + Place.Field.LAT_LNG);
//
//
//            autocompleteSupportFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
//                @Override
//                public void onError(@NonNull Status status) {
//                    Log.d(TAG, "onError: Lỗi google map: " + status);
//                    Utils.toastyError(LocationPickerActivity.this, "Lỗi google map: " + status);
//                }
//
//                @Override
//                public void onPlaceSelected(@NonNull Place place) {
//
//                    String id = place.getId();
//                    String title = place.getName();
//                    LatLng latLng = place.getLatLng();
//                    selectedLatitude = latLng.latitude;
//                    selectedLongitude = latLng.longitude;
//                    selectedAddress = place.getAddress();
//
//                    Log.d(TAG, "onPlaceSelected: ID: " + id);
//                    Log.d(TAG, "onPlaceSelected: Title: " + title);
//                    Log.d(TAG, "onPlaceSelected: latitude: " + selectedLatitude);
//                    Log.d(TAG, "onPlaceSelected: longitude: " + selectedLongitude);
//                    Log.d(TAG, "onPlaceSelected: address: " + selectedAddress);
//                    Log.d(TAG, "onPlaceSelected: latLng: " + latLng);
//
//                    addMarker(latLng, title, selectedAddress); // liên quan đến addMarker bên dưới
//                }
//            });
//        }

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        binding.toolbarGpsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isGPSEnabled()) {
                    Log.d(TAG, "onClick: ");
                    requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION);
                } else {
                    Utils.toastyInfo(LocationPickerActivity.this, "Bạn chưa bật vị trí, vui lòng bật để lấy vị trí hiện tại");
                }
            }
        });

        binding.doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("latitude", selectedLatitude);
                intent.putExtra("longitude", selectedLongitude);
                intent.putExtra("address", selectedAddress);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        Log.d(TAG, "onMapReady: ");

        mGoogleMap = googleMap;
        Log.d(TAG, "onMapReady: " + mGoogleMap);

        requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION);

        mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {

                selectedLatitude = latLng.latitude;
                selectedLongitude = latLng.longitude;

                Log.d(TAG, "onMapClick: selectedLatitude: " + selectedLatitude);
                Log.d(TAG, "onMapClick: selectedLongitude: " + selectedLongitude);

                addressFromLatlng(latLng);
            }
        });

    }

    @SuppressLint("MissingPermission")
    private ActivityResultLauncher<String> requestLocationPermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean isGranted) {
                    Log.d(TAG, "onActivityResult: isGranted");
                    if (isGranted) {
                        mGoogleMap.setMyLocationEnabled(true);
                        pickCurrentPlace();
                    } else {
                        mGoogleMap.setMyLocationEnabled(false);
                        Utils.toastyError(LocationPickerActivity.this, "Chưa cấp quyền...!");
                    }
                }
            }
    );

    private void addressFromLatlng(LatLng latLng) {
        Log.d(TAG, "addressFromLatlng: ");

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        try {
            List<Address> addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 10);
            if (addressList.size() > 0) {
                String addressLine = addressList.get(0).getAddressLine(0);
                String countryName = addressList.get(0).getCountryName();
                String adminArea = addressList.get(0).getAdminArea();
                String subAdminArea = addressList.get(0).getSubAdminArea();
                String locationName = addressList.get(0).getFeatureName();

                selectedAddress = "" + addressLine;
                title = "" + locationName + "-" + subAdminArea + "-" + adminArea + "-" + countryName;
                addMarker(latLng, title, selectedAddress);
                Log.d(TAG, "addressFromLatlng: title: " + title);
                Log.d(TAG, "addressFromLatlng: selectedAddress: " + selectedAddress);
                Log.d(TAG, "addressFromLatlng: countryName: " + countryName);
                Log.d(TAG, "addressFromLatlng: adminArea: " + adminArea);
                Log.d(TAG, "addressFromLatlng: subAdminArea: " + subAdminArea);

            }

        } catch (Exception e) {
            Log.d(TAG, "addressFromLatlng: ", e);
        }
    }

    private void pickCurrentPlace() {
        Log.d(TAG, "pickCurrentPlace: ");
        if (mGoogleMap == null) {
            return;
        }

        detectAndShowDeviceLocationMap();

    }

    @SuppressLint("MissingPermission")
    private void detectAndShowDeviceLocationMap() {
        Log.d(TAG, "detectAndShowDeviceLocationMap: ");
        try {

            Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
            locationResult.addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if(location!=null){

                                mLastKnownLocation = location;

                                selectedLatitude = location.getLatitude();
                                selectedLongitude = location.getLongitude();

                                Log.d(TAG, "onSuccess: selectedLatitude" +selectedLatitude);
                                Log.d(TAG, "onSuccess: selectedLongitude" +selectedLongitude);

                                LatLng latLng = new LatLng(selectedLatitude,selectedLongitude);
                                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,DEFAULT_ZOOM));
                                mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM));

                                addressFromLatlng(latLng);
                            }
                            else {
                                Log.d(TAG, "onSuccess: Location is null");
                                Utils.toastyInfo(LocationPickerActivity.this,"Vị trí bị null");
                            }

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
        }catch (Exception e){
            Log.d(TAG, "detectAndShowDeviceLocationMap: ",e);
        }
    }

    //quyền cung cấp GPS vị trí
    private boolean isGPSEnabled(){

        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);

        boolean gpsEnabled = false;
        boolean networkEnabled = false;

        try {
            gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            Log.d(TAG, "isGPSEnabled: "+gpsEnabled);
        }
        catch (Exception e){
            Log.d(TAG, "isGPSEnabled: ",e);
            Utils.toastyError(LocationPickerActivity.this,"Lỗi: "+e);
        }

        try {
            networkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            Log.d(TAG, "isGPSEnabled: "+networkEnabled);
        }
        catch (Exception e){
            Log.d(TAG, "isGPSEnabled: ",e);
            Utils.toastyError(LocationPickerActivity.this,"Lỗi: "+e);
        }
        return !(!gpsEnabled && !networkEnabled);
    }

    private void addMarker(LatLng latLng, String title, String address) {
        Log.d(TAG, "addMarker: latitude: "+latLng.latitude);
        Log.d(TAG, "addMarker: longitude: "+latLng.longitude);
        Log.d(TAG, "addMarker: title: "+title);
        Log.d(TAG, "addMarker: address: "+address);

        mGoogleMap.clear();

        try {
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title(title);
            markerOptions.snippet(address);
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

            mGoogleMap.addMarker(markerOptions);
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,DEFAULT_ZOOM));

            binding.doneLl.setVisibility(View.VISIBLE);
            binding.selectedPlaceTv.setText(address);
        }
        catch (Exception e){
            Log.d(TAG, "addMarker: "+e);
            Utils.toastyError(LocationPickerActivity.this,"Lỗi addMarker: "+e);
        }



    }


}