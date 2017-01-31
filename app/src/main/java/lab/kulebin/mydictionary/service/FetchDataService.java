package lab.kulebin.mydictionary.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.util.Log;

import org.json.JSONException;

import java.lang.reflect.AnnotatedElement;
import java.util.Collection;
import java.util.List;

import lab.kulebin.mydictionary.Constants;
import lab.kulebin.mydictionary.db.Contract;
import lab.kulebin.mydictionary.db.DbHelper;
import lab.kulebin.mydictionary.http.HttpClient;
import lab.kulebin.mydictionary.http.IHttpClient;
import lab.kulebin.mydictionary.http.UrlBuilder;
import lab.kulebin.mydictionary.json.JsonHelper;
import lab.kulebin.mydictionary.model.DataCache;
import lab.kulebin.mydictionary.utils.UriBuilder;

public class FetchDataService extends IntentService {

    public enum FetchDataServiceMode {SYNCHRONIZE, REFRESH}

    private static final String TAG = FetchDataService.class.getSimpleName();

    public FetchDataService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        FetchDataServiceMode serviceMode = FetchDataServiceMode.SYNCHRONIZE;
        if (intent.hasExtra(Constants.EXTRA_FETCH_DATA_SERVICE_MODE)) {
            serviceMode = FetchDataServiceMode.valueOf(
                    intent.getStringExtra(Constants.EXTRA_FETCH_DATA_SERVICE_MODE));
        }

        for (final Class model : Contract.FETCH_DATA_SET) {

            final String url = UrlBuilder.getUrl(new String[]{DbHelper.getTableName(model)}, null);

            if (DataCache.isDataRefreshNeeded(url) || serviceMode == FetchDataServiceMode.REFRESH) {

                final List<ContentValues> list = fetchData(url, model);
                int storeResult = -1;
                if (list != null && !list.isEmpty()) {
                    storeResult = storeData(list, model);
                }

                if (storeResult == -1) {
                    Log.v(TAG, "result is null");
                }
            }
        }
    }

    private List<ContentValues> fetchData(final String pUrl, final Class pClazz) {
        final IHttpClient httpClient = new HttpClient();
        try {
            final String personalisedUrl = UrlBuilder.getPersonalisedUrl(pUrl);
            final String result = httpClient.get(personalisedUrl);
            DataCache.updateLastRequestedTime(pUrl);

            return JsonHelper.parseJson(pClazz, result);
        } catch (final JSONException pE) {
            Log.v(TAG, "Parsing error");
        } catch (final Exception e) {
            Log.v(TAG, "Fetching data error!");
        }
        return null;
    }

    private int storeData(final Collection<ContentValues> pList, final AnnotatedElement pClazz) {

        if (pList != null) {
            final ContentValues[] valuesArray = new ContentValues[pList.size()];
            pList.toArray(valuesArray);
            this.getContentResolver().delete(UriBuilder.getTableUri(pClazz), null, null);
            this.getContentResolver().bulkInsert(UriBuilder.getTableUri(pClazz), valuesArray);
            return pList.size();
        }
        return -1;
    }
}
