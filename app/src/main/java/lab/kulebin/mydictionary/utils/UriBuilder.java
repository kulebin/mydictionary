package lab.kulebin.mydictionary.utils;


import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.support.annotation.Nullable;
import java.lang.reflect.AnnotatedElement;
import lab.kulebin.mydictionary.db.DbHelper;

import static android.R.attr.id;


public class UriBuilder {

    public enum ContentType {TABLE, ITEM}

    public static final String AUTHORITY = "lab.kulebin.mydictionary.app.EntryProvider";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    public static Uri getTableUri(final AnnotatedElement pClazz) {
        return BASE_CONTENT_URI.buildUpon().appendPath(DbHelper.getTableName(pClazz)).build();
    }

    public static Uri getItemUri(final AnnotatedElement pClazz, long pId) {
        return ContentUris.withAppendedId(getTableUri(pClazz), id);
    }

    @Nullable
    public static String getContentType(final AnnotatedElement pClazz, ContentType pType) {
        switch (pType) {
            case ITEM:
                return ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + AUTHORITY + "/" + DbHelper.getTableName(pClazz);
            case TABLE:
                return ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + AUTHORITY + "/" + DbHelper.getTableName(pClazz);
            default:
                return null;
        }
    }
}
