package com.example.olx;

import android.content.Context;
import android.text.format.DateFormat;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Locale;

import es.dmoral.toasty.Toasty;

public class Utils {

    public static final String AD_STATUS_AVAILABLE="Sản phẩm chưa bán"; // còn trong cửa hàng
    public static final String AD_STATUS_SOLD="Sản phẩm đã bán"; // update cơ sở dữ liệu sản phẩm đã bán


    //danh sách sản phẩm
    public static final String[] categories = {
            "Tất cả",
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
    //điều kiện sử dụng sản phầm
    public static final String[] conditions ={"Mới", "Đã sử dụng","Đã tân trang"};

    //số lượng sản phẩm từ 1 đến 20 sản phẩm đổ lại
    public static final String[] quantitys = {"1", "2", "3", "4",
            "5", "6", "7", "8",
            "9", "10", "11", "12",
            "13", "14", "15", "16",
            "17", "18", "19", "20"};
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

    //định dạng ngày/tháng/năm giờ/phút/giây
    public static String formatTimestampDateTime(Long timestamp){
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(timestamp);

        String date = DateFormat.format("dd/MM/yyyy HH:mm",calendar).toString();
        return date;
    }

}
