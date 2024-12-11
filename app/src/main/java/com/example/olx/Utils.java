package com.example.olx;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.format.DateFormat;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import es.dmoral.toasty.Toasty;

/*Class chứa hàm tĩnh, hằng, biến mà chúng ta sử dụng cho toàn bộ dự án này*/
public class Utils  {

    public static final String AD_STATUS_AVAILABLE="Sản phẩm chưa bán"; // còn trong cửa hàng
    public static final String MESSAGE_TYPE_TEXT="TEXT"; // message đoạn chat
    public static final String MESSAGE_TYPE_IMAGE="IMAGE"; // message hình ảnh
    public static final String AD_STATUS_SOLD="Sản phẩm đã bán"; // update cơ sở dữ liệu sản phẩm đã bán

    public static final String NOTIFICATION_TYPE_NEW_MESSAGE="NEW_MESSAGE"; // TYPE Thông báo

    //FCM key message chat
    public static final String FCM_SERVER_KEY ="AAAAVjDXN_A:APA91bEK144sVx5V9RW1eoXZxZSdleimvqPF1dlfto2ljjpCbiPjEzMiONwZlApAV6IknAj5n0J7QbjrSpW-87cXQErsyW3hLFyx1hPZJutNGYyapMbyFjsFA_llsvGUK2UPGS5reDkh";

    //danh sách sản phẩm
    public static final String[] categories = {
            "Quán cafe & Trà sửa",
            "Điện thoại",
            "Máy tính/Laptop",
            "Cửa hàng tạp hoá",
            "Cửa hàng xe",
            "Sách",
            "Thời trang",
            "Thú cưng",
            "Bất động sản",
            "Nông nghiệp",
            "Thể loại khác",
    };
    //danh sách sản phẩm liên quan đến HomeUserFragment và HomeSellerFragment
    public static final String[] categoriess = {
            "Hiển thị tất cả sản phẩm",
            "Quán cafe & Trà sửa",
            "Điện thoại",
            "Máy tính/Laptop",
            "Cửa hàng tạp hoá",
            "Cửa hàng xe",
            "Sách",
            "Thời trang",
            "Thú cưng",
            "Bất động sản",
            "Nông nghiệp",
            "Thể loại khác",
    };

    public static final String[] orders = {
            "Hiển thị tất cả đơn hàng",
            "Chưa thanh toán",
            "Đã thanh toán",
            "Đã hủy"
    };



    //điều kiện sử dụng sản phầm
    public static final String[] conditions ={"Mới", "Đã sử dụng","Đã tân trang"};

    //số lượng sản phẩm từ 1 đến 20 sản phẩm đổ lại
    public static final String[] quantitys = {"1", "2", "3", "4",
            "5", "6", "7", "8",
            "9", "10", "11", "12",
            "13", "14", "15", "16",
            "17", "18", "19", "20"};

    //chat path
    public static final String chatPath(String receiptUid, String yourUid){
        //mảng uid chat
        String[] arrayUids = new String[]{receiptUid,yourUid};
        //sắp xếp mảng chat
        Arrays.sort(arrayUids);
        //nối cả 2 mảng sau khi sắp xếp
        String chatPath = arrayUids[0] + "_" + arrayUids[1];
        return chatPath;
    }
    //thông báo toast chung
    public static  void toast(Context context, String message){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
    // thông báo toast success
    public static  void toastySuccess(Context context, String message){
        Toasty.success(context, message, Toast.LENGTH_SHORT, true).show();//thành công
    }

    // thông báo toast error
    public static  void toastyError(Context context, String message){
        Toasty.error(context, message, Toast.LENGTH_SHORT, true).show(); // thông báo lỗi
    }

    //thông báo toast info
    public static void toastyInfo(Context context, String message){
        Toasty.info(context, message, Toast.LENGTH_SHORT, true).show();
    }
    public static  long getTimestamp(){
        return System.currentTimeMillis();
    }

    //định dạng ngày/tháng/năm
    public static String formatTimestampDate(Long timestamp){
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(timestamp);

        String date = DateFormat.format("dd/MM/yyyy",calendar).toString();
        return date;
    }

    //định dạng ngày/tháng/năm
    public static String formatTimestampDates(Long timestamp){
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(timestamp);

        String date = DateFormat.format("MM/yyyy",calendar).toString();
        return date;
    }

    //định dạng ngày/tháng/năm giờ/phút/giây
    public static String formatTimestampDateTime(Long timestamp){
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(timestamp);

        String date = DateFormat.format("dd/MM/yyyy HH:mm",calendar).toString();
        return date;
    }

    //add Favorites
    public static void addToFavorite(Context context, String adId){


        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null){
            Utils.toast(context,"Bạn cần đăng nhập tài khoản");
        }
        else {
            long timestamp = Utils.getTimestamp();

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("id", adId); // id của ModelAd
            hashMap.put("timestamp", timestamp);

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).child("Favorites").child(adId)
                    .setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Utils.toastySuccess(context, "Thêm vào mục yêu thích Thành công");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Utils.toastyError(context,"Thất bại: "+e);
                        }
                    });
        }
    }


    //remove Favorites
    public static void removeFavorite(Context context, String adId){

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null){
            Utils.toast(context,"Bạn cần đăng nhập tài khoản");
        }
        else {

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).child("Favorites").child(adId)
                    .removeValue()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Utils.toastySuccess(context, "Xóa khỏi mục yêu thích thành công");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Utils.toastyError(context,"Thất bại: "+e);
                        }
                    });
        }
    }

    //điện thọại
    public  static void callIntent(Context context, String phone){
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("tel:"+Uri.encode(phone)));
        context.startActivity(intent);
    }
    //gửi sms dùng cách này
    public static void startSMSIntent(Context context, String phone) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:"+Uri.encode(phone)));
        context.startActivity(intent);

    }
    //gửi sms vẫn được dùng cách này  nhưng bị lỗi không đúng sdt
//    public  static void smsIntent(Context context, String phone){
//        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.fromParts("mms-sms", Uri.encode(phone), null));
//        context.startActivity(intent);
//    }

    // google maps
    public static  void mapIntent(Context context, double latitude, double longitude){
        Uri gmsIntentUri = Uri.parse("http://maps.google.com/maps?daddr=" + latitude +","+longitude);

        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmsIntentUri);

        mapIntent.setPackage("com.google.android.apps.maps");

        if (mapIntent.resolveActivity(context.getPackageManager()) != null){
            context.startActivity(mapIntent);
        }
        else {
            Utils.toastyInfo(context, "Google MAP chưa cài đặt");
        }
    }


}
