package lab.kulebin.mydictionary.json;


import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import lab.kulebin.mydictionary.model.Dictionary;
import lab.kulebin.mydictionary.model.Entry;
import lab.kulebin.mydictionary.utils.Converter;

import static lab.kulebin.mydictionary.model.Entry.EMPTY_DATE;

public class JsonHelper {

    public static final String TAG = JsonHelper.class.getSimpleName();

    @Nullable
    public static List parseJson(Class<?> clazz, String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        Iterator<String> iterator = jsonObject.keys();
        List<String> keys = new ArrayList<>();
        while (iterator.hasNext()) {
            keys.add(iterator.next());
        }
        if (clazz == Entry.class) {
            List<Entry> entryList = new ArrayList<>();
            for (String key : keys) {
                JSONObject jsonSubObject = jsonObject.getJSONObject(key);
                Entry entry = parseEntryJsonObject(jsonSubObject, key);
                if (entry != null) {
                    entryList.add(entry);
                }
            }
            return entryList;
        } else if (clazz == Dictionary.class) {
            List<Dictionary> dictionariesList = new ArrayList<>();
            for (String key : keys) {
                JSONObject jsonSubObject = jsonObject.getJSONObject(key);
                dictionariesList.add(parseDictionaryJsonObject(jsonSubObject, key));
            }
            return dictionariesList;
        }
        return null;
    }

    private static Entry parseEntryJsonObject(JSONObject pEntryJsonObject, String key) throws JSONException {
        return new Entry(
                Long.parseLong(key),
                pEntryJsonObject.getInt(Entry.DICTIONARY_ID),
                pEntryJsonObject.getString(Entry.VALUE),
                pEntryJsonObject.isNull(Entry.TRANSCRIPTION) ? null : pEntryJsonObject.getString(Entry.TRANSCRIPTION),
                pEntryJsonObject.getLong(Entry.CREATION_DATE),
                pEntryJsonObject.isNull(Entry.LAST_EDITION_DATE) ? EMPTY_DATE : pEntryJsonObject.getLong(Entry.LAST_EDITION_DATE),
                pEntryJsonObject.isNull(Entry.IMAGE_URL) ? null : pEntryJsonObject.getString(Entry.IMAGE_URL),
                pEntryJsonObject.isNull(Entry.SOUND_URL) ? null : pEntryJsonObject.getString(Entry.SOUND_URL),
                pEntryJsonObject.isNull(Entry.TRANSLATION) ? null : Converter.convertStringToStringArray(pEntryJsonObject.getString(Entry.TRANSLATION)),
                pEntryJsonObject.isNull(Entry.USAGE_CONTEXT) ? null : Converter.convertStringToStringArray(pEntryJsonObject.getString(Entry.USAGE_CONTEXT)));
    }

    private static Dictionary parseDictionaryJsonObject(JSONObject pDictionaryJsonObject, String key) throws JSONException {
        return new Dictionary(
                Integer.parseInt(key),
                pDictionaryJsonObject.getString(Dictionary.NAME),
                pDictionaryJsonObject.getLong(Dictionary.CREATION_DATE));
    }

    @Nullable
    public static String buildJson(List<?> pList) throws JSONException {
        JSONArray jsonArray = new JSONArray();
        if (pList.get(0).getClass() == Entry.class) {
            for (Entry entry : (List<Entry>) pList) {
                jsonArray.put(buildEntryJsonObject(entry));
            }
            return jsonArray.toString();
        } else if (pList.get(0).getClass() == Dictionary.class) {
            for (Dictionary dictionary : (List<Dictionary>) pList) {
                jsonArray.put(buildDictionaryJsonObject(dictionary));
            }
            return jsonArray.toString();
        }
        return null;
    }

    public static JSONObject buildEntryJsonObject(Entry pEntry) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(Entry.DICTIONARY_ID, pEntry.getDictionaryId());
        jsonObject.put(Entry.VALUE, pEntry.getValue());
        jsonObject.put(Entry.TRANSCRIPTION, pEntry.getTranscription());
        jsonObject.put(Entry.CREATION_DATE, pEntry.getCreationDate());
        jsonObject.put(Entry.LAST_EDITION_DATE, pEntry.getLastEditionDate());
        jsonObject.put(Entry.IMAGE_URL, pEntry.getImageUrl());
        jsonObject.put(Entry.SOUND_URL, pEntry.getSoundUrl());
        jsonObject.put(Entry.TRANSLATION, Converter.convertStringArrayToString(pEntry.getTranslation()));
        jsonObject.put(Entry.USAGE_CONTEXT, Converter.convertStringArrayToString(pEntry.getUsageContext()));
        return jsonObject;
    }

    public static JSONObject buildDictionaryJsonObject(Dictionary pDictionary) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(Dictionary.NAME, pDictionary.getName());
        jsonObject.put(Dictionary.CREATION_DATE, pDictionary.getCreationDate());
        return jsonObject;
    }

    public static long getEntryIdFromJson(String json) throws JSONException{
        JSONObject jsonObject = new JSONObject(json);
        return jsonObject.getLong(Entry.ID);
    }

    public static long getEntryLastEditionDateFromJson(String json) throws JSONException{
        JSONObject jsonObject = new JSONObject(json);
        return jsonObject.getLong(Entry.LAST_EDITION_DATE);
    }
}
