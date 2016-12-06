package lab.kulebin.mydictionary.model;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class JsonParser {

    public static List parse(Class<?> clazz, String json) throws JSONException {
        JSONArray jsonArray = new JSONArray(json);
        if (clazz == Entry.class) {
            List<Entry> entryList = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject entryObject = jsonArray.getJSONObject(i);
                entryList.add(new Entry(
                        entryObject.getInt(Entry.ID),
                        entryObject.getInt(Entry.DICTIONARY_ID),
                        entryObject.getString(Entry.VALUE),
                        entryObject.isNull(Entry.TRANSCRIPTION) ? null : entryObject.getString(Entry.TRANSCRIPTION),
                        entryObject.getLong(Entry.CREATION_DATE),
                        entryObject.isNull(Entry.LAST_EDITION_DATE) ? -1 : entryObject.getLong(Entry.LAST_EDITION_DATE),
                        entryObject.isNull(Entry.IMAGE_URL) ? null : entryObject.getString(Entry.IMAGE_URL),
                        entryObject.isNull(Entry.SOUND_URL) ? null : entryObject.getString(Entry.SOUND_URL),
                        entryObject.isNull(Entry.TRANSLATION) ? null : Entry.convertStringToStirngArray(entryObject.getString(Entry.TRANSLATION)),
                        entryObject.isNull(Entry.USAGE_CONTEXT) ? null : Entry.convertStringToStirngArray(entryObject.getString(Entry.USAGE_CONTEXT))
                ));
            }
            return entryList;
        } else if (clazz == Dictionary.class) {
            List<Dictionary> dictionariesList = new ArrayList<>();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject entryObject = jsonArray.getJSONObject(i);
                dictionariesList.add(new Dictionary(
                        entryObject.getInt(Dictionary.ID),
                        entryObject.getString(Dictionary.NAME)
                ));
            }
            return dictionariesList;
        } else {
            return null;
        }
    }
}
