package lab.kulebin.mydictionary.json;

import android.content.ContentValues;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import lab.kulebin.mydictionary.Constants;
import lab.kulebin.mydictionary.db.annotations.dbInteger;
import lab.kulebin.mydictionary.db.annotations.dbLong;
import lab.kulebin.mydictionary.db.annotations.dbString;
import lab.kulebin.mydictionary.model.Entry;

public final class JsonHelper {

    @Nullable
    public static List<ContentValues> parseJson(final Class<?> clazz, final String json) throws JSONException, IllegalAccessException {
        final JSONObject jsonObject = new JSONObject(json);
        final Iterator<String> iterator = jsonObject.keys();
        final Collection<String> keys = new ArrayList<>();
        while (iterator.hasNext()) {
            keys.add(iterator.next());
        }
        if (!keys.isEmpty()) {
            final List<ContentValues> valuesList = new ArrayList<>();
            for (final String key : keys) {
                final JSONObject jsonSubObject = jsonObject.getJSONObject(key);
                final ContentValues values = new ContentValues();

                final Field[] fields = clazz.getFields();

                for (final Field field : fields) {
                    final Annotation[] annotations = field.getAnnotations();

                    for (final Annotation annotation : annotations) {
                        final String fieldName;
                        if (annotation instanceof dbInteger) {
                            fieldName = (String) field.get(null);
                            values.put(fieldName, jsonSubObject.getInt(fieldName));
                        } else if (annotation instanceof dbString) {
                            fieldName = (String) field.get(null);
                            values.put(fieldName, jsonSubObject.isNull(fieldName) ? null : jsonSubObject.getString(fieldName));
                        } else if (annotation instanceof dbLong) {
                            fieldName = (String) field.get(null);
                            if (fieldName.equals(Constants.ID_COLUMN)) {
                                values.put(fieldName, key);
                            } else {
                                values.put(fieldName, jsonSubObject.getLong(fieldName));
                            }
                        }
                    }
                }
                valuesList.add(values);
            }
            return valuesList;
        } else {
            return null;
        }
    }

    public static long getEntryLastEditionDateFromJson(final String json) throws JSONException {
        final JSONObject jsonObject = new JSONObject(json);
        return jsonObject.getLong(Entry.LAST_EDITION_DATE);
    }
}
