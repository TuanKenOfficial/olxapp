package com.example.olx.fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.example.olx.adapter.AdapterChats;
import com.example.olx.databinding.FragmentChatsBinding;
import com.example.olx.model.ModelChats;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class ChatsFragment extends Fragment {

    private FragmentChatsBinding binding;

    private static final String TAG ="CHATS_FRAGMENT";

    private FirebaseAuth firebaseAuth;

    private String myUid;

    private Context mContext;

    private ArrayList<ModelChats> chatsArrayList;

    private AdapterChats adapterChats;
    public ChatsFragment (){
        //contrustor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        this.mContext = context;
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentChatsBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();
        myUid = firebaseAuth.getUid();

        Log.d(TAG, "onViewCreated: myUid: "+myUid);

        loadChats(); // load tất cả tin nhắn chat lên fragment
        binding.searchEdt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                try {
                    Log.d(TAG, "onTextChanged: Query: "+s);
                    String query = s.toString();
                    adapterChats.getFilter().filter(query);
                }catch (Exception e){
                    Log.d(TAG, "onTextChanged: Lỗi: "+e);

                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


    }

    private void loadChats() {
        chatsArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Chats");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    String chatKey = ""+ds.getKey();

                    Log.d(TAG, "onDataChange: chatKey: "+chatKey);
                    if (chatKey.contains(myUid)){
                        Log.d(TAG, "onDataChange: Contains");

                        ModelChats modelChats = new ModelChats();
                        modelChats.setChatKey(chatKey);

                        chatsArrayList.add(modelChats);
                    }
                    else {
                        Log.d(TAG, "onDataChange: Not contains");
                    }
                }
                adapterChats = new AdapterChats(mContext,chatsArrayList);
                binding.chatsRv.setAdapter(adapterChats);
                /*load danh sách tin nhắn, chúng tôi sắp xếp những danh sách tin nhắn
                theo dấu thời gian của từng tin nhắn bị mất, hiển thị cuộc trò chuyện mới*/
                sort();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sort() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Collections.sort(chatsArrayList,(model1, model2)->Long.compare(model2.getTimestamp(), model1.getTimestamp()));

                adapterChats.notifyDataSetChanged();

            }
        },1000);
    }
}