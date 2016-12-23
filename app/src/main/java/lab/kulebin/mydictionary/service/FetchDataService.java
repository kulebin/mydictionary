package lab.kulebin.mydictionary.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;

import java.util.List;
import java.util.Vector;

import lab.kulebin.mydictionary.app.Constants;
import lab.kulebin.mydictionary.db.Contract;
import lab.kulebin.mydictionary.http.Api;
import lab.kulebin.mydictionary.http.HttpClient;
import lab.kulebin.mydictionary.json.JsonHelper;
import lab.kulebin.mydictionary.model.Dictionary;
import lab.kulebin.mydictionary.model.Entry;
import lab.kulebin.mydictionary.utils.Converter;
import lab.kulebin.mydictionary.utils.UriBuilder;

public class FetchDataService extends IntentService {

    private static final String TAG = FetchDataService.class.getSimpleName();

    public FetchDataService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        SharedPreferences shp = getSharedPreferences(Constants.APP_PREFERENCES, Context.MODE_PRIVATE);
        final String token = shp.getString(Constants.APP_PREFERENCES_USER_TOKEN, null);
        for (Class model : Contract.MODELS) {
            String path;
            if (model == Entry.class) {
                path = Api.ENTRIES;
            } else if (model == Dictionary.class) {
                path = Api.DICTIONARIES;
            } else {
                continue;
            }
            Uri uri = Uri.parse(Api.getBaseUrl()).buildUpon()
                    .appendPath(path + Api.JSON_FORMAT)
                    .appendQueryParameter(Api.PARAM_AUTH, token)
                    .build();
            List<?> list = fetchData(uri.toString(), model);
            int storeResult = -1;
            if (list != null && list.size() > 0) {
                storeResult = storeData(list);
            }

            if (storeResult != -1) {
                Toast toast = Toast.makeText(this,
                        "Success! " + storeResult + " entries have been stored.",
                        Toast.LENGTH_SHORT);
                toast.show();
                Log.v(TAG, String.valueOf(storeResult));
            } else {
                Log.v(TAG, "result is null");
            }
        }
    }

    private List fetchData(String pUrl, Class pClazz) {
        HttpClient httpClient = new HttpClient();
        try {
            return JsonHelper.parseJson(pClazz, httpClient.get(pUrl));
        } catch (JSONException pE) {
            Log.v(TAG, "Parsing error");
        } catch (Exception e) {
            Log.v(TAG, "Fetching data error!");
        }
        return null;
    }

    private int storeData(List<?> pList) {

        Vector<ContentValues> valuesVector = new Vector<>(pList.size());
        Class clazz = null;
        if (pList.get(0).getClass() == Entry.class) {
            for (Entry entry : (List<Entry>) pList) {
                clazz = Entry.class;
                ContentValues values = new ContentValues();
                values.put(Entry.ID, entry.getId());
                values.put(Entry.DICTIONARY_ID, entry.getDictionaryId());
                values.put(Entry.VALUE, entry.getValue());
                values.put(Entry.TRANSCRIPTION, entry.getTranscription());
                values.put(Entry.CREATION_DATE, entry.getCreationDate());
                values.put(Entry.LAST_EDITION_DATE, entry.getLastEditionDate());
                values.put(Entry.IMAGE_URL, entry.getImageUrl());
                values.put(Entry.SOUND_URL, entry.getSoundUrl());
                values.put(Entry.TRANSLATION, Converter.convertStringArrayToString(entry.getTranslation()));
                values.put(Entry.USAGE_CONTEXT, Converter.convertStringArrayToString(entry.getUsageContext()));
                valuesVector.add(values);
            }

        } else if (pList.get(0).getClass() == Dictionary.class) {
            for (Dictionary dictionary : (List<Dictionary>) pList) {
                clazz = Dictionary.class;
                ContentValues values = new ContentValues();
                values.put(Dictionary.ID, dictionary.getId());
                values.put(Dictionary.NAME, dictionary.getName());
                values.put(Dictionary.CREATION_DATE, dictionary.getCreationDate());
                valuesVector.add(values);
            }
        } else {
            return -1;
        }
        if (valuesVector.size() > 0) {
            ContentValues[] valuesArray = new ContentValues[valuesVector.size()];
            valuesVector.toArray(valuesArray);
            this.getContentResolver().delete(UriBuilder.getTableUri(clazz), null, null);
            this.getContentResolver().bulkInsert(UriBuilder.getTableUri(clazz), valuesArray);
        }
        return valuesVector.size();
    }
}
