package lab.kulebin.mydictionary.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.format.DateUtils;

import lab.kulebin.mydictionary.db.annotations.Table;
import lab.kulebin.mydictionary.db.annotations.dbLong;
import lab.kulebin.mydictionary.db.annotations.dbString;
import lab.kulebin.mydictionary.db.annotations.dbUnique;
import lab.kulebin.mydictionary.utils.UriBuilder;

@Table(name = "dataCache")
public final class DataCache {

    @dbUnique
    @dbString
    public static final String URL = "url";
    @dbLong
    public static final String LAST_REQUESTED_TIME = "lastRequestedTime";

    private static final long LIVE_TIME = DateUtils.MINUTE_IN_MILLIS * 120;

    public static boolean isDataRefreshNeeded(final Context pContext, final String pUrl) {
        final Cursor cursor = pContext.getContentResolver().query(
                UriBuilder.getTableUri(DataCache.class),
                null,
                DataCache.URL + "=?",
                new String[]{pUrl},
                null
        );
        if (cursor != null && cursor.moveToFirst()) {
            final long lastRequestedTime = cursor.getLong(cursor.getColumnIndex(DataCache.LAST_REQUESTED_TIME));
            cursor.close();
            return (System.currentTimeMillis() - lastRequestedTime) > LIVE_TIME;
        } else if (cursor != null) {
            cursor.close();
        }
        return true;
    }

    public static void updateLastRequestedTime(final Context pContext, final String pUrl) {
        final ContentValues values = new ContentValues();
        values.put(DataCache.URL, pUrl);
        values.put(DataCache.LAST_REQUESTED_TIME, System.currentTimeMillis());
        pContext.getContentResolver().insert(
                UriBuilder.getTableUri(DataCache.class),
                values);
    }

}
