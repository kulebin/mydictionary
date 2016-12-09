package lab.kulebin.mydictionary.model;


import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import lab.kulebin.mydictionary.utils.Converter;

import static lab.kulebin.mydictionary.model.Entry.EMPTY_DATE;

public class JsonHelper {

    public static final String TAG = JsonHelper.class.getSimpleName();

    @Nullable
    public static List parseJson(Class<?> clazz, String json) throws JSONException {
        JSONArray jsonArray = new JSONArray(json);
        JSONObject jsonObject;
        if (clazz == Entry.class) {
            List<Entry> entryList = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                Entry entry = parseEntryJsonObject(jsonObject);
                if (entry != null) {
                    entryList.add(entry);
                }
            }
            return entryList;
        } else if (clazz == Dictionary.class) {
            List<Dictionary> dictionariesList = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                dictionariesList.add(parseDictionaryJsonObject(jsonObject));
            }
            return dictionariesList;
        }
        return null;
    }

    private static Entry parseEntryJsonObject(JSONObject pEntryJsonObject) throws JSONException {
        if (pEntryJsonObject.isNull(Entry.ID)) {
            return null;
        }
        return new Entry(
                pEntryJsonObject.getInt(Entry.ID),
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

    private static Dictionary parseDictionaryJsonObject(JSONObject pDictionaryJsonObject) throws JSONException {
        return new Dictionary(
                pDictionaryJsonObject.getInt(Dictionary.ID),
                pDictionaryJsonObject.getString(Dictionary.NAME));
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
        jsonObject.put(Entry.ID, pEntry.getId());
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
        jsonObject.put(Dictionary.ID, pDictionary.getId());
        jsonObject.put(Dictionary.NAME, pDictionary.getName());
        return jsonObject;
    }
}
