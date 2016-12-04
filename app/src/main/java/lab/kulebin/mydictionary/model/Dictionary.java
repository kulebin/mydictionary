package lab.kulebin.mydictionary.model;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;

import lab.kulebin.mydictionary.EntryProvider;
import lab.kulebin.mydictionary.db.annotations.Table;
import lab.kulebin.mydictionary.db.annotations.dbInteger;
import lab.kulebin.mydictionary.db.annotations.dbString;

import static lab.kulebin.mydictionary.EntryProvider.BASE_CONTENT_URI;

@Table(name = Dictionary.TABLE_NAME)
public class Dictionary {

    public static final String TABLE_NAME = "dictionary";
    public static final Uri DICTIONARY_URI =
            BASE_CONTENT_URI.buildUpon().appendPath(TABLE_NAME).build();
    public static final String CONTENT_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + EntryProvider.AUTHORITY + "/" + TABLE_NAME;
    public static final String CONTENT_ITEM_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + EntryProvider.AUTHORITY + "/" + TABLE_NAME;

    @dbInteger
    public static final String ID = "id";
    @dbString
    public static final String NAME = "name";

    private int mId;
    private String mName;

    public int getId() {
        return mId;
    }

    public void setId(final int pId) {
        mId = pId;
    }

    public String getName() {
        return mName;
    }

    public void setName(final String pName) {
        mName = pName;
    }

    public static Uri buildDictionaryUri(long id) {
        return ContentUris.withAppendedId(DICTIONARY_URI, id);
    }
}
