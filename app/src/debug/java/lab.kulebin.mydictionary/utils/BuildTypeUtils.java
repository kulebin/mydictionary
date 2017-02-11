package lab.kulebin.mydictionary.utils;

import android.content.Context;

import com.facebook.stetho.Stetho;

public final class BuildTypeUtils {

    public static void initStetho(final Context pContext) {
        Stetho.initializeWithDefaults(pContext);
    }
}
