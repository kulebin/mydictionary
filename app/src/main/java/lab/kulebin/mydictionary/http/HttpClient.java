package lab.kulebin.mydictionary.http;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.Semaphore;

import lab.kulebin.mydictionary.Constants;
import lab.kulebin.mydictionary.utils.ContextHolder;

public class HttpClient implements IHttpClient {

    private enum RequestType {GET, PUT, POST, DELETE}

    public static final String DELETE_RESPONSE_OK = "null";
    public static final String BROADCAST_EVENT_TOKEN_REFRESHED = "TOKEN_REFRESHED";

    private static final String TAG = HttpClient.class.getSimpleName();
    private static final int ERROR_UNAUTHORIZED = 401;
    private static final String ERROR_AUTH_TOKEN_IS_EXPIRED = "Auth token is expired";
    private static final String RESPONSE_KEY_ERROR = "error";

    private String mToken;

    @Override
    public String get(final String url) throws Exception {
        return doRequest(url, RequestType.GET, null, null);
    }

    @Override
    public String get(final String url, final Map<String, String> headers) throws Exception {
        return doRequest(url, RequestType.GET, headers, null);
    }

    @Override
    public String put(final String url, final Map<String, String> headers, final String body) throws Exception {
        return doRequest(url, RequestType.PUT, headers, body);
    }

    @Override
    public String delete(final String url) throws Exception {
        return doRequest(url, RequestType.DELETE, null, null);
    }

    private String doRequest(final String url, final RequestType type, final Map<String, String> header, final String body) throws Exception {
        if (isNetworkAvailable()) {
            String response = null;
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                final URL reqUrl = new URL(url);
                connection = ((HttpURLConnection) reqUrl.openConnection());
                connection.setRequestMethod(type.name());
                if (header != null) {
                    for (final String key : header.keySet()) {
                        connection.addRequestProperty(key, header.get(key));
                    }
                }
                if (body != null) {
                    applyBody(connection, body);
                }

                final InputStream inputStream;
                final int responseCode = connection.getResponseCode();

                final boolean isSuccess = connection.getResponseCode() >= 200 && responseCode < 300;
                if (isSuccess) {
                    inputStream = connection.getInputStream();
                } else {
                    inputStream = connection.getErrorStream();
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));
                final StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                response = stringBuilder.toString();

                inputStream.close();

                if (!isSuccess) {
                    if (responseCode == ERROR_UNAUTHORIZED && isTokenExpiredError(response)) {
                        refreshToken();
                        final String newUrl = replaceTokenInUrl(url, mToken);
                        response = doRequest(newUrl, type, header, body);
                    } else {
                        throw new Exception(response);
                    }
                }

            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(TAG, "Error closing stream", e);
                    }
                }
            }
            return response;
        } else {
            throw new IOException();
        }

    }

    private void applyBody(final HttpURLConnection httpURLConnection, final String body) throws Exception {
        final byte[] outputInBytes = body.getBytes("UTF-8");
        final OutputStream os = httpURLConnection.getOutputStream();
        os.write(outputInBytes);
        os.close();
    }

    private boolean isNetworkAvailable() {
        final ConnectivityManager connectivityManager
                = (ConnectivityManager) ContextHolder.get().getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void refreshToken() {
        final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        final Semaphore semaphore = new Semaphore(0);

        firebaseUser.getToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {

            public void onComplete(@NonNull final Task<GetTokenResult> task) {
                if (task.isSuccessful()) {
                    final String token = task.getResult().getToken();
                    if (token != null) {
                        mToken = token;
                        final SharedPreferences appPreferences = ContextHolder.get().getSharedPreferences(Constants.APP_PREFERENCES, Context.MODE_PRIVATE);
                        final SharedPreferences.Editor editor = appPreferences.edit();
                        editor.putString(Constants.APP_PREFERENCES_USER_TOKEN, token);
                        editor.apply();
                        notifyTokenRefreshed();
                    }
                } else {
                    Log.e(TAG, "Refresh token error!");
                }

                semaphore.release();
            }
        });

        try {
            semaphore.acquire();
        } catch (final InterruptedException pE) {
            // ignore
        }
    }

    private boolean isTokenExpiredError(final String pErrorResponse) {
        final String error;
        try {
            final JSONObject errorObject = new JSONObject(pErrorResponse);
            error = errorObject.getString(RESPONSE_KEY_ERROR);
        } catch (final JSONException pE) {
            return false;
        }
        return ERROR_AUTH_TOKEN_IS_EXPIRED.equals(error);
    }

    private String replaceTokenInUrl(final String pUrl, final CharSequence pNewToken) {
        final String token = Uri.parse(pUrl).getQueryParameter(Api.PARAM_AUTH);
        return pUrl.replace(token, pNewToken);
    }

    private void notifyTokenRefreshed() {
        final Intent intent = new Intent(BROADCAST_EVENT_TOKEN_REFRESHED);
        LocalBroadcastManager.getInstance(ContextHolder.get()).sendBroadcast(intent);
    }
}