package lab.kulebin.mydictionary;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;

import lab.kulebin.mydictionary.db.DbHelper;
import lab.kulebin.mydictionary.model.Dictionary;
import lab.kulebin.mydictionary.model.Entry;


public class EntryProvider extends ContentProvider {

    public static final String AUTHORITY = "lab.kulebin.mydictionary.app.EntryProvider";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    private DbHelper mDbHelper;
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    static final int ENTRY = 100;
    static final int ENTRY_BY_DICTIONARY_ID = 101;
    static final int DICTIONARY = 200;

    private static final SQLiteQueryBuilder sEntryByDictionaryQueryBuilder;

    static{
        sEntryByDictionaryQueryBuilder = new SQLiteQueryBuilder();

        sEntryByDictionaryQueryBuilder.setTables(
                Entry.TABLE_NAME + " INNER JOIN " +
                        Dictionary.TABLE_NAME +
                        " ON " + Entry.TABLE_NAME +
                        "." + Entry.DICTIONARY_ID +
                        " = " + Dictionary.TABLE_NAME +
                        "." + Dictionary.ID);
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new DbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(final Uri pUri, final String[] pStrings, final String pS, final String[] pStrings1, final String pSortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(pUri)) {
            case ENTRY_BY_DICTIONARY_ID:
            {
                retCursor = sEntryByDictionaryQueryBuilder.query(mDbHelper.getReadableDatabase(),
                        null,
                        null,
                        null,
                        null,
                        null,
                        pSortOrder
                );
                break;
            }
            // "weather"
            case ENTRY: {
                retCursor = mDbHelper.getReadableDatabase().query(
                        Entry.TABLE_NAME,
                        null,
                        null,
                        null,
                        null,
                        null,
                        pSortOrder
                );
                break;
            }

            case DICTIONARY: {
                retCursor = mDbHelper.getReadableDatabase().query(
                        Dictionary.TABLE_NAME,
                        null,
                        null,
                        null,
                        null,
                        null,
                        pSortOrder
                );
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + pUri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), pUri);
        return retCursor;
    }

    @Nullable
    @Override
    public String getType(final Uri pUri) {

        final int match = sUriMatcher.match(pUri);

        switch (match) {
            case ENTRY:
                return Entry.CONTENT_TYPE;
            case DICTIONARY:
                return Dictionary.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + pUri);
        }
    }

    @Nullable
    @Override
    public Uri insert(final Uri pUri, final ContentValues pContentValues) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(pUri);
        Uri returnUri;

        switch (match) {
            case ENTRY: {
                long id = db.insert(Entry.TABLE_NAME, null, pContentValues);
                if ( id > 0 )
                    returnUri = Entry.buildEntryUri(id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + pUri);
                break;
            }
            case DICTIONARY: {
                long id = db.insert(Dictionary.TABLE_NAME, null, pContentValues);
                if ( id > 0 )
                    returnUri = Dictionary.buildDictionaryUri(id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + pUri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + pUri);
        }
        getContext().getContentResolver().notifyChange(pUri, null);
        return returnUri;
    }

    @Override
    public int delete(final Uri pUri, final String pSel, final String[] pArgs) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(pUri);
        int rowsDeleted;
        switch (match) {
            case ENTRY:
                rowsDeleted = db.delete(
                        Entry.TABLE_NAME, pSel, pArgs);
                break;
            case DICTIONARY:
                rowsDeleted = db.delete(
                        Dictionary.TABLE_NAME, pSel, pArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + pUri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(pUri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(
            Uri pUri, ContentValues pValues, String pSelection, String[] pArgs) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(pUri);
        int rowsUpdated;

        switch (match) {
            case ENTRY:
                rowsUpdated = db.update(Entry.TABLE_NAME, pValues, pSelection,
                        pArgs);
                break;
            case DICTIONARY:
                rowsUpdated = db.update(Dictionary.TABLE_NAME, pValues, pSelection,
                        pArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + pUri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(pUri, null);
        }
        return rowsUpdated;
    }

    static UriMatcher buildUriMatcher() {

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

        matcher.addURI(AUTHORITY, Entry.TABLE_NAME, ENTRY);
        matcher.addURI(AUTHORITY, Entry.TABLE_NAME + "/#", ENTRY_BY_DICTIONARY_ID);
        matcher.addURI(AUTHORITY, Dictionary.TABLE_NAME, DICTIONARY);
        return matcher;
    }

    @Override
    public int bulkInsert(Uri pUri, ContentValues[] pContentValues) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(pUri);
        switch (match) {
            case ENTRY:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : pContentValues) {
                        long id = db.insert(Entry.TABLE_NAME, null, value);
                        if (id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(pUri, null);
                return returnCount;
            default:
                return super.bulkInsert(pUri, pContentValues);
        }
    }
}
