package com.example.olx.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import com.example.olx.FilterChats;
import com.example.olx.R;
import com.example.olx.Utils;
import com.example.olx.activities.ChatActivity;
import com.example.olx.databinding.RowChatsBinding;
import com.example.olx.model.ModelChats;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdapterChats extends RecyclerView.Adapter<AdapterChats.HolderChats> implements Filterable {

    private RowChatsBinding binding;

    public ArrayList<ModelChats> chatsArrayList;
    private ArrayList<ModelChats> filterList;
    private FilterChats filterChats;


    private static final String TAG = "ADAPTER_CHATS";

    private Context context;

    private FirebaseAuth firebaseAuth;

    private String myUid;

    public AdapterChats(Context context,ArrayList<ModelChats> chatsArrayList) {
        this.chatsArrayList = chatsArrayList;
        this.filterList = chatsArrayList;
        this.context = context;

        firebaseAuth = FirebaseAuth.getInstance();
        myUid = firebaseAuth.getUid();
    }

    @NonNull
    @Override
    public HolderChats onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding =RowChatsBinding.inflate(LayoutInflater.from(context),parent,false);
        return new HolderChats(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderChats holder, int position) {
        ModelChats modelChats = chatsArrayList.get(position);

        loadLastMessage(modelChats,holder);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String receiptUid = modelChats.getReceiptUid();

                if (receiptUid != null){
                    Intent intentChat = new Intent(context, ChatActivity.class);
                    intentChat.putExtra("receiptUid",receiptUid);
                    context.startActivity(intentChat);
                }
            }
        });
    }

    private void loadLastMessage(ModelChats modelChats, HolderChats holder) {

        String chatKey = modelChats.getChatKey();

        Log.d(TAG, "loadLastMessage: chatKey: "+chatKey);

        // coi lại chỗ này
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Chats");
        ref.child(chatKey)
                .limitToLast(1)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()){
                            String fromUid = "" + ds.child("fromUid").getValue();
                            String toUid = "" + ds.child("toUid").getValue();
                            String message = "" + ds.child("message").getValue();
                            String messageId = "" + ds.child("messageId").getValue();
                            String messageType = "" + ds.child("messageType").getValue();
                            long timestamp = (Long)  ds.child("timestamp").getValue();

                            String fomattedDate = Utils.formatTimestampDateTime(timestamp);

                            modelChats.setMessage(message);
                            modelChats.setMessageId(messageId);
                            modelChats.setMessageType(messageType);
                            modelChats.setFromUid(fromUid);
                            modelChats.setToUid(toUid);
                            modelChats.setTimestamp(timestamp);

                            //gán dữ liệu cho dd/MM/yyyy HH:mm
                            holder.dateTimeTv.setText(fomattedDate);

                            if (messageType.equals(Utils.MESSAGE_TYPE_TEXT)){
                                holder.lastMessageTv.setText(message);
                            }else {
                                holder.lastMessageTv.setText("Gửi tệp đính kèm : Hình ảnh");
                            }

                        }
                        loadReceiptUserInfo(modelChats,holder);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadReceiptUserInfo(ModelChats modelChats, HolderChats holder) {

        String fromUid = modelChats.getFromUid();
        String toUid = modelChats.getToUid();

        String receiptUid;

        if (fromUid.equals(myUid)){
            receiptUid = toUid;
        }
        else {
            receiptUid = fromUid;
        }

        modelChats.setReceiptUid(receiptUid);


        Log.d(TAG, "loadReceiptUserInfo: fromUid: "+fromUid);
        Log.d(TAG, "loadReceiptUserInfo: toUid: "+toUid);
        Log.d(TAG, "loadReceiptUserInfo: receiptUid: "+receiptUid);
        // coi lại chỗ này
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(receiptUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String name = ""+snapshot.child("name").getValue();
                            String profileImageUrl = ""+snapshot.child("profileImageUrl").getValue();

//                            modelChats.setName(name);
//                            modelChats.setProfileImageUrl(profileImageUrl);

                            holder.nameTv.setText(name);
                            Log.d(TAG, "onDataChange: name: "+name);

                            try {
                                Glide.with(context)
                                        .load(profileImageUrl)
                                        .placeholder(R.drawable.ic_users)
                                        .into(holder.profileIv);
                                Log.d(TAG, "onDataChange: Load thành công tên hình ảnh");


                            }catch (Exception e){
                                Log.d(TAG, "onDataChange: Lỗi hiển thị tên và hình ảnh profile lên  chats");
                            }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return chatsArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if(filterChats == null){
            filterChats = new FilterChats(this,filterList);
        }
        return filterChats;
    }

    public class HolderChats extends RecyclerView.ViewHolder{
        ShapeableImageView profileIv;
        TextView nameTv,lastMessageTv, dateTimeTv;
        public HolderChats(@NonNull View itemView) {
            super(itemView);
            profileIv = (ShapeableImageView) itemView.findViewById(R.id.profileIv);
            nameTv = (TextView) itemView.findViewById(R.id.nameTv);
            lastMessageTv = (TextView) itemView.findViewById(R.id.lastMessageTv);
            dateTimeTv = (TextView) itemView.findViewById(R.id.dateTimeTv);
        }
    }
}
