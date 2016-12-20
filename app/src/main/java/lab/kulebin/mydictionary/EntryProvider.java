package lab.kulebin.mydictionary;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import lab.kulebin.mydictionary.db.DbHelper;
import lab.kulebin.mydictionary.model.Dictionary;
import lab.kulebin.mydictionary.model.Entry;
import lab.kulebin.mydictionary.utils.UriBuilder;

import static lab.kulebin.mydictionary.utils.UriBuilder.AUTHORITY;


public class EntryProvider extends ContentProvider {

    static final int ENTRY = 100;
    static final int DICTIONARY_BY_DICTIONARY_ID = 101;
    static final int DICTIONARY = 200;
    static final int ENTRY_BY_DICTIONARY_ID = 300;
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private static final SQLiteQueryBuilder sEntryByDictionaryIdQueryBuilder;

    static {
        sEntryByDictionaryIdQueryBuilder = new SQLiteQueryBuilder();

        sEntryByDictionaryIdQueryBuilder.setTables(
                DbHelper.getTableName(Entry.class) + " INNER JOIN " +
                        DbHelper.getTableName(Dictionary.class) +
                        " ON " + DbHelper.getTableName(Entry.class) +
                        "." + Entry.DICTIONARY_ID +
                        " = " + DbHelper.getTableName(Dictionary.class) +
                        "." + Dictionary.ID);
    }

    private DbHelper mDbHelper;

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(AUTHORITY, DbHelper.getTableName(Entry.class), ENTRY);
        matcher.addURI(AUTHORITY, DbHelper.getTableName(Entry.class) + "/" + DbHelper.getTableName(Dictionary.class), ENTRY_BY_DICTIONARY_ID);
        matcher.addURI(AUTHORITY, DbHelper.getTableName(Dictionary.class) + "/#", DICTIONARY_BY_DICTIONARY_ID);
        matcher.addURI(AUTHORITY, DbHelper.getTableName(Dictionary.class), DICTIONARY);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new DbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull final Uri pUri, final String[] pProjection, final String pSelection, final String[] pSelArgs, final String pSortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(pUri)) {
            case ENTRY: {
                retCursor = mDbHelper.getReadableDatabase().query(
                        DbHelper.getTableName(Entry.class),
                        pProjection,
                        pSelection,
                        pSelArgs,
                        null,
                        null,
                        pSortOrder
                );
                break;
            }
            case ENTRY_BY_DICTIONARY_ID: {
                retCursor = sEntryByDictionaryIdQueryBuilder.query(
                        mDbHelper.getReadableDatabase(),
                        pProjection,
                        pSelection,
                        pSelArgs,
                        null,
                        null,
                        pSortOrder
                );
                break;
            }
            case DICTIONARY: {
                retCursor = mDbHelper.getReadableDatabase().query(
                        DbHelper.getTableName(Dictionary.class),
                        pProjection,
                        pSelection,
                        pSelArgs,
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
    public String getType(@NonNull final Uri pUri) {
        return UriBuilder.getContentType(getTable(pUri), UriBuilder.ContentType.TABLE);
    }

    @Nullable
    @Override
    public Uri insert(@NonNull final Uri pUri, final ContentValues pContentValues) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Uri returnUri;
        Class tableClass = getTable(pUri);
        long id = db.insert(DbHelper.getTableName(tableClass), null, pContentValues);
        if (id > 0)
            returnUri = UriBuilder.getItemUri(tableClass, id);
        else {
            throw new android.database.SQLException("Failed to insert row into " + pUri);
        }
        getContext().getContentResolver().notifyChange(pUri, null);
        return returnUri;
    }

    @Override
    public int bulkInsert(@NonNull Uri pUri, ContentValues[] pContentValues) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();

        db.beginTransaction();
        int returnCount = 0;
        try {
            for (ContentValues value : pContentValues) {
                long id = db.insert(DbHelper.getTableName(getTable(pUri)), null, value);
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
    }

    @Override
    public int delete(@NonNull final Uri pUri, final String pSel, final String[] pArgs) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int rowsDeleted;
        switch (sUriMatcher.match(pUri)) {
            case DICTIONARY_BY_DICTIONARY_ID:
                String dictionaryId = pUri.getLastPathSegment();
                db.beginTransaction();
                try {
                    int dictionariesDeleted = db.delete(DbHelper.getTableName(Dictionary.class), Dictionary.ID + "=" + dictionaryId, null);
                    db.delete(DbHelper.getTableName(Entry.class), Entry.DICTIONARY_ID + "=" + dictionaryId, null);
                    db.setTransactionSuccessful();
                    rowsDeleted = dictionariesDeleted;
                } finally {
                    db.endTransaction();
                }
                break;
            default:
                rowsDeleted = db.delete(DbHelper.getTableName(getTable(pUri)), pSel, pArgs);
        }


        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(pUri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri pUri, ContentValues pValues, String pSelection, String[] pArgs) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int rowsUpdated;
        rowsUpdated = db.update(DbHelper.getTableName(getTable(pUri)), pValues, pSelection, pArgs);

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(pUri, null);
        }
        return rowsUpdated;
    }

    private Class<?> getTable(final Uri pUri) {
        final int match = sUriMatcher.match(pUri);
        switch (match) {
            case ENTRY:
                return Entry.class;
            case DICTIONARY:
                return Dictionary.class;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + pUri);
        }
    }
}