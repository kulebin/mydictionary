package lab.kulebin.mydictionary.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.util.Log;

import java.lang.reflect.AnnotatedElement;
import java.util.Collection;
import java.util.List;

import lab.kulebin.mydictionary.Constants;
import lab.kulebin.mydictionary.db.Contract;
import lab.kulebin.mydictionary.db.DbHelper;
import lab.kulebin.mydictionary.http.HttpErrorHandler;
import lab.kulebin.mydictionary.http.IHttpClient;
import lab.kulebin.mydictionary.http.IHttpErrorHandler;
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

        final IHttpClient httpClient = IHttpClient.Impl.newInstance();
        final IHttpErrorHandler httpErrorHandler = new HttpErrorHandler();
        httpClient.setErrorHandler(httpErrorHandler);

        for (final Class model : Contract.FETCH_DATA_SET) {

            final String url = UrlBuilder.getUrl(new String[]{DbHelper.getTableName(model)}, null);

            if (DataCache.isDataRefreshNeeded(url) || serviceMode == FetchDataServiceMode.REFRESH) {

                final String personalisedUrl = UrlBuilder.getPersonalisedUrl(url);
                final String result = httpClient.get(personalisedUrl);
                DataCache.updateLastRequestedTime(url);

                List<ContentValues> list = null;
                try {
                    list = JsonHelper.parseJson(model, result);
                } catch (final Exception e) {
                    Log.v(TAG, "Fetching data error!");
                }
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
