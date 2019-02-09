package jp.co.azz.maps;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AppContract {
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm";
//    private static final String DATE_FORMAT = "yyyy年MM月dd日 hh時mm分";


   public static String now() {
       SimpleDateFormat sdf = new SimpleDateFormat(AppContract.DATE_FORMAT);
       return sdf.format(new Date());
   }
}
