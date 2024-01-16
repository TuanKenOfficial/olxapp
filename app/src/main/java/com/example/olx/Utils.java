package com.example.olx;

import android.content.Context;
import android.text.format.DateFormat;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Locale;

import es.dmoral.toasty.Toasty;

public class Utils {  //thông báo toast chung
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
