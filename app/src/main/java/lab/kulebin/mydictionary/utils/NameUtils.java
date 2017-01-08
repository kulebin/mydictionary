package lab.kulebin.mydictionary.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class NameUtils {

    public static String createImageFileName(){
        final String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        return  "JPEG_" + timeStamp + ".jpg";
    }

}
