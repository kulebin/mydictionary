package lab.kulebin.mydictionary.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public final class ConnectionUtils {

    public static boolean isNetworkAvailable() {
        final ConnectivityManager connectivityManager
                = (ConnectivityManager) ContextHolder.get().getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
