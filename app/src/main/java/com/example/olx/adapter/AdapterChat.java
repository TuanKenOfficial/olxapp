package com.example.olx.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;

import com.example.olx.R;
import com.example.olx.Utils;
import com.example.olx.model.ModelChats;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class AdapterChat extends RecyclerView.Adapter<AdapterChat.HolderChats>{
    private static final  int MSG_TYPE_LEFT = 0;
    private static final  int MSG_TYPE_RIGHT = 1;
    Context context;
    ArrayList<ModelChats> mChat;
    private String imageUrl;
    private String  imageUri = null;
    private FirebaseUser fUser;

    private static final String TAG = "ADAPTERCHAT";


    public AdapterChat(Context context, ArrayList<ModelChats> mChat) {
        this.context = context;
        this.mChat = mChat;

        fUser = FirebaseAuth.getInstance().getCurrentUser();
    }


    @NonNull
    @Override
    public HolderChats onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        //xử lý chat layout_right and layout_left người gửi và người nhận lên inflate
        if (viewType==MSG_TYPE_RIGHT){
            View view = LayoutInflater.from(context).inflate(R.layout.row_chats_right,parent,false);
            return new HolderChats(view);

        }else {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chats_left,parent,false);
            return new HolderChats(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull HolderChats holder, int position) {
        ModelChats chat = mChat.get(position);

        String messageId = chat.getMessageId();
        String messageType = chat.getMessageType();
        String fromUid = chat.getFromUid();
        String toUid = chat.getToUid();
        String message = chat.getMessage();
        long timestamp = chat.getTimestamp();
        String call = chat.getCall();


//        holder.img_message.setImageURI(imageUri);

        String formattedDate = Utils.formatTimestampDateTime(timestamp);
//        holder.timedatetv.setText(""+formattedDate);
        holder.timedatetv.setText(formattedDate);


        if (messageType.equals(Utils.MESSAGE_TYPE_TEXT)){
            holder.imageIv.setVisibility(View.GONE);
            holder.txtMessage.setVisibility(View.VISIBLE);
            holder.txtMessage.setText(message);
        }
        else {
            holder.imageIv.setVisibility(View.VISIBLE);
            holder.txtMessage.setVisibility(View.GONE);
            try {
                /*
                * Ở đây có 2 cách dùng hình ảnh
                * cách 1: dùng Glide
                * cách 2: dùng Picasso
                * Các bạn có thể dùng 1 trong 2 cách trên nhá*/
                Glide.with(context)
                        .load(message)
                        .placeholder(R.drawable.image)
                        .into(holder.imageIv);
                //có thể dùng Picasso để load ảnh
//                Picasso.get().load(message).placeholder(R.drawable.image).error(R.drawable.ic_image_broken_gray).into(holder.imageIv);
                Log.d(TAG, "onBindViewHolder: Load thành công hình ảnh");

            }
            catch (Exception e){
                Log.e(TAG, "onBindViewHolder: Lỗi load hình ảnh: ",e);

            }

        }

    }

    //xử lý nếu người dùng chưa đăng nhập
    @Override
    public int getItemViewType(int position) {
        //người dùng đã được đăng nhập thành công
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mChat.get(position).getFromUid().equals(fUser.getUid())){
            return MSG_TYPE_RIGHT;
        }
        else {
            return MSG_TYPE_LEFT;
        }

    }

    @Override
    public int getItemCount() {
        return mChat.size();
    }
    public class HolderChats extends RecyclerView.ViewHolder{

        TextView txtMessage ,timedatetv;
        ImageView imageIv;

        public HolderChats(@NonNull View itemView) {
            super(itemView);
            txtMessage = (TextView) itemView.findViewById(R.id.txtMessage);
            timedatetv = (TextView) itemView.findViewById(R.id.timedatetv);
            imageIv = (ImageView) itemView.findViewById(R.id.imageIv);

        }
    }
}

