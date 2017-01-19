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
import lab.kulebin.mydictionary.model.DataCache;
import lab.kulebin.mydictionary.model.Dictionary;
import lab.kulebin.mydictionary.model.Entry;
import lab.kulebin.mydictionary.model.EntryTagMap;
import lab.kulebin.mydictionary.model.Tag;
import lab.kulebin.mydictionary.utils.UriBuilder;

import static android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE;
import static lab.kulebin.mydictionary.utils.UriBuilder.AUTHORITY;

public class EntryProvider extends ContentProvider {

    private static final int ENTRY = 100;
    private static final int ENTRY_BY_DICTIONARY_ID = 101;
    private static final int ENTRY_WITH_TAGS_RAW_QUERY = 102;
    private static final int DICTIONARY = 200;
    private static final int DICTIONARY_BY_DICTIONARY_MENU_ID = 201;
    private static final int DATA_CACHE = 300;
    private static final int ENTRY_TAG = 400;
    private static final int TAG = 500;
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private static final SQLiteQueryBuilder sEntryByDictionaryIdQueryBuilder;

    static {
        sEntryByDictionaryIdQueryBuilder = new SQLiteQueryBuilder();

        sEntryByDictionaryIdQueryBuilder.setTables(
                DbHelper.getTableName(Entry.class) + " INNER JOIN " +
                        DbHelper.getTableName(Dictionary.class) +
                        " ON " + DbHelper.getTableName(Entry.class) +
                        "." + Entry.DICTIONARY_MENU_ID +
                        " = " + DbHelper.getTableName(Dictionary.class) +
                        "." + Dictionary.ID);
    }

    public static final String SQL_ENTRY_WITH_TAGS_WITHOUT_SORT_PARAM = "SELECT " +
            "e." + Entry.ID + ", " +
            "e." + Entry.VALUE + ", " +
            "e." + Entry.TRANSLATION + ", " +
            "e." + Entry.IMAGE_URL + ", " +
            "GROUP_CONCAT(" +
            "t." + Tag.NAME + ") AS " + Constants.CURSOR_COLUMN_TAGS +
            " FROM " +
            DbHelper.getTableName(Entry.class) + " AS e " +
            "LEFT JOIN " + DbHelper.getTableName(EntryTagMap.class) + " AS et " +
            "ON e." + Entry.ID + " = et." + EntryTagMap.ENTRY_ID +
            " LEFT JOIN " + DbHelper.getTableName(Tag.class) + " AS t " +
            "ON et." + EntryTagMap.TAG_ID + " = t." + Tag.ID +
            " WHERE e." + Entry.DICTIONARY_MENU_ID + "=?" +
            " GROUP BY " +
            "e." + Entry.ID +
            " ORDER BY " +
            "e.";

    private DbHelper mDbHelper;

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(AUTHORITY, DbHelper.getTableName(Entry.class), ENTRY);
        matcher.addURI(AUTHORITY, DbHelper.getTableName(Entry.class) + "/" + DbHelper.getTableName(Dictionary.class), ENTRY_BY_DICTIONARY_ID);
        matcher.addURI(AUTHORITY, DbHelper.getTableName(Entry.class) +
                "/" + DbHelper.getTableName(Tag.class), ENTRY_WITH_TAGS_RAW_QUERY);
        matcher.addURI(AUTHORITY, DbHelper.getTableName(Dictionary.class) + "/#", DICTIONARY_BY_DICTIONARY_MENU_ID);
        matcher.addURI(AUTHORITY, DbHelper.getTableName(Dictionary.class), DICTIONARY);
        matcher.addURI(AUTHORITY, DbHelper.getTableName(DataCache.class), DATA_CACHE);
        matcher.addURI(AUTHORITY, DbHelper.getTableName(EntryTagMap.class), ENTRY_TAG);
        matcher.addURI(AUTHORITY, DbHelper.getTableName(Tag.class), TAG);
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
        final Cursor retCursor;
        switch (sUriMatcher.match(pUri)) {
            case ENTRY_BY_DICTIONARY_ID:
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
            case ENTRY_WITH_TAGS_RAW_QUERY:
                retCursor = mDbHelper.getReadableDatabase().rawQuery(
                        SQL_ENTRY_WITH_TAGS_WITHOUT_SORT_PARAM + pSortOrder,
                        pSelArgs
                );
                break;
            default:
                retCursor = mDbHelper.getReadableDatabase().query(
                        DbHelper.getTableName(getTable(pUri)),
                        pProjection,
                        pSelection,
                        pSelArgs,
                        null,
                        null,
                        pSortOrder
                );
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
        final Uri returnUri;
        final Class tableClass = getTable(pUri);
        final long id;
        switch (sUriMatcher.match(pUri)) {
            case DATA_CACHE:
                id = db.insertWithOnConflict(
                        DbHelper.getTableName(getTable(pUri)),
                        null,
                        pContentValues,
                        CONFLICT_REPLACE
                );
                break;
            default:
                id = db.insert(DbHelper.getTableName(tableClass), null, pContentValues);
        }
        if (id > 0) {
            returnUri = UriBuilder.getItemUri(tableClass, id);
        } else {
            throw new android.database.SQLException("Failed to insert row into " + pUri);
        }
        getContext().getContentResolver().notifyChange(pUri, null);
        return returnUri;
    }

    @Override
    public int bulkInsert(@NonNull final Uri pUri, @NonNull final ContentValues[] pContentValues) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();

        db.beginTransaction();
        int returnCount = 0;
        try {
            for (final ContentValues value : pContentValues) {
                final long id = db.insert(DbHelper.getTableName(getTable(pUri)), null, value);
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
            case DICTIONARY_BY_DICTIONARY_MENU_ID:
                final String dictionaryMenuId = pUri.getLastPathSegment();
                db.beginTransaction();
                try {
                    final int dictionariesDeleted = db.delete(DbHelper.getTableName(Dictionary.class), Dictionary.MENU_ID + "=" + dictionaryMenuId, null);
                    db.delete(DbHelper.getTableName(Entry.class), Entry.DICTIONARY_MENU_ID + "=" + dictionaryMenuId, null);
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
    public int update(@NonNull final Uri pUri, final ContentValues pValues, final String pSelection, final String[] pArgs) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int rowsUpdated;
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
            case DATA_CACHE:
                return DataCache.class;
            case TAG:
                return Tag.class;
            case ENTRY_TAG:
                return EntryTagMap.class;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + pUri);
        }
    }
}