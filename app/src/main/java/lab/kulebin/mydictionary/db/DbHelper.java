package lab.kulebin.mydictionary.db;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;
import android.util.Log;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.annotation.Annotation;

import lab.kulebin.mydictionary.db.annotations.Table;
import lab.kulebin.mydictionary.db.annotations.dbInteger;
import lab.kulebin.mydictionary.db.annotations.dbLong;
import lab.kulebin.mydictionary.db.annotations.dbString;

import java.util.List;
import java.util.Locale;

import static android.R.attr.id;
import static android.R.attr.value;

public class DbHelper extends SQLiteOpenHelper {

    private static final String SQL_TABLE_CREATE_TEMPLATE = "CREATE TABLE IF NOT EXISTS %s (%s);";
    private static final String SQL_TABLE_CREATE_FIELD_TEMPLATE = "%s %s";
    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "my_dictionary.db";

    public DbHelper(final Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Nullable
    private static String getTableName(final AnnotatedElement clazz) {
        final Table table = clazz.getAnnotation(Table.class);

        if (table != null) {
            return table.name();
        } else {
            return null;
        }
    }

    @Nullable
    private static String getTableCreateQuery(final Class<?> clazz) {
        final Table table = clazz.getAnnotation(Table.class);

        if (table != null) {
            try {
                final String name = table.name();

                final StringBuilder builder = new StringBuilder();
                final Field[] fields = clazz.getFields();
                boolean isFirstFieldAdded = false;

                for (int i = 0; i < fields.length; i++) {
                    final Field field = fields[i];

                    final Annotation[] annotations = field.getAnnotations();
                    String type = null;

                    for (final Annotation annotation : annotations) {
                        if (annotation instanceof dbInteger) {
                            type = ((dbInteger) annotation).value();
                        } else if (annotation instanceof dbString) {
                            type = ((dbString) annotation).value();
                        } else if (annotation instanceof dbLong){
                            type = ((dbLong) annotation).value();
                        }
                    }

                    if (type == null) {
                        continue;
                    }

                    final String value = (String) field.get(null);
                    if (isFirstFieldAdded) {
                        builder.append(",");
                    }

                    builder.append(String.format(Locale.US, SQL_TABLE_CREATE_FIELD_TEMPLATE, value, type));
                    isFirstFieldAdded = true;
                }

                return String.format(Locale.US, SQL_TABLE_CREATE_TEMPLATE, name, builder);
            } catch (final Exception e) {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
        Log.v("onCreateDb","Model is creared");
        for (final Class<?> clazz : Contract.MODELS) {
            final String sql = getTableCreateQuery(clazz);

            if (sql != null) {
                db.execSQL(sql);
                Log.v("onCreateDb", sql);
            }
        }
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {

    }
}
