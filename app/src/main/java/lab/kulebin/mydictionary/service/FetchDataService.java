package lab.kulebin.mydictionary.service;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONException;

import java.lang.reflect.AnnotatedElement;
import java.util.Collection;
import java.util.List;

import lab.kulebin.mydictionary.Constants;
import lab.kulebin.mydictionary.db.Contract;
import lab.kulebin.mydictionary.db.DbHelper;
import lab.kulebin.mydictionary.http.Api;
import lab.kulebin.mydictionary.http.HttpClient;
import lab.kulebin.mydictionary.http.IHttpClient;
import lab.kulebin.mydictionary.json.JsonHelper;
import lab.kulebin.mydictionary.model.DataCache;
import lab.kulebin.mydictionary.utils.UriBuilder;

public class FetchDataService extends IntentService {

    public enum FetchDataServiceMode {SYNCHRONIZE, REFRESH}

    private static final String TAG = FetchDataService.class.getSimpleName();

    private final BroadcastReceiver mRefreshTokenReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(final Context context, final Intent intent) {

            mToken = getToken();
        }
    };

    public FetchDataService() {
        super(TAG);
    }

    private String mToken;

    @Override
    public void onCreate() {
        super.onCreate();
        LocalBroadcastManager.getInstance(this).registerReceiver(mRefreshTokenReceiver,
                new IntentFilter(HttpClient.BROADCAST_EVENT_TOKEN_REFRESHED));
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        FetchDataServiceMode serviceMode = FetchDataServiceMode.SYNCHRONIZE;
        if (intent.hasExtra(Constants.EXTRA_FETCH_DATA_SERVICE_MODE)) {
            serviceMode = FetchDataServiceMode.valueOf(
                    intent.getStringExtra(Constants.EXTRA_FETCH_DATA_SERVICE_MODE));
        }
        mToken = getToken();
        for (final Class model : Contract.FETCH_DATA_SET) {

            final String url = Uri.parse(Api.getBaseUrl()).buildUpon()
                    .appendPath(DbHelper.getTableName(model) + Api.JSON_FORMAT)
                    .build()
                    .toString();
            if (DataCache.isDataRefreshNeeded(this, url) || serviceMode == FetchDataServiceMode.REFRESH) {
                final String fullUrl = Uri.parse(url).buildUpon()
                        .appendQueryParameter(Api.PARAM_AUTH, mToken)
                        .build()
                        .toString();
                final List<ContentValues> list = fetchData(fullUrl, model);
                int storeResult = -1;
                if (list != null && !list.isEmpty()) {
                    storeResult = storeData(list, model);
                }

                if (storeResult != -1) {
                    DataCache.updateLastRequestedTime(this, url);

                } else {
                    Log.v(TAG, "result is null");
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRefreshTokenReceiver);
        super.onDestroy();
    }

    private String getToken() {
        final SharedPreferences shp = getSharedPreferences(Constants.APP_PREFERENCES, Context.MODE_PRIVATE);
        return shp.getString(Constants.APP_PREFERENCES_USER_TOKEN, null);
    }

    private List<ContentValues> fetchData(final String pUrl, final Class pClazz) {
        final IHttpClient httpClient = new HttpClient();
        try {
            return JsonHelper.parseJson(pClazz, httpClient.get(pUrl));
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
