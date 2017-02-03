package lab.kulebin.mydictionary.http;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
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

import lab.kulebin.mydictionary.App;
import lab.kulebin.mydictionary.utils.ContextHolder;

import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;

public class HttpClient implements IHttpClient {

    private static final String TAG = HttpClient.class.getSimpleName();

    private IHttpErrorHandler mErrorHandler;

    @Override
    public String get(final String url) {
        return doRequest(url, RequestType.GET, null, null);
    }

    @Override
    public String get(final String url, final Map<String, String> headers) {
        return doRequest(url, RequestType.GET, headers, null);
    }

    @Override
    public String put(final String url, final Map<String, String> headers, final String body) {
        return doRequest(url, RequestType.PUT, headers, body);
    }

    @Override
    public String delete(final String url) {
        return doRequest(url, RequestType.DELETE, null, null);
    }

    private String doRequest(final String url, final RequestType type, final Map<String, String> header, final String body) {
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
                    if (responseCode == HTTP_UNAUTHORIZED && isTokenExpiredError(response)) {
                        refreshToken();
                        response = doRequest(UrlBuilder.replaceTokenInUrl(url), type, header, body);
                    } else {
                        mErrorHandler.handleError(new HttpRequestException(responseCode, response));
                    }
                }

            } catch (final Exception e) {
                mErrorHandler.handleError(e);
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
            mErrorHandler.handleError(new IOException());
            return null;
        }
    }

    private void applyBody(final HttpURLConnection httpURLConnection, final String body) throws Exception {
        final byte[] outputInBytes = body.getBytes("UTF-8");
        final OutputStream os = httpURLConnection.getOutputStream();
        os.write(outputInBytes);
        os.close();
    }

    //TODO it is not Client role -> Utils
    private boolean isNetworkAvailable() {
        final ConnectivityManager connectivityManager
                = (ConnectivityManager) ContextHolder.get().getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    //TODO Move to error handling
    private void refreshToken() {
        final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        final Semaphore semaphore = new Semaphore(0);

        firebaseUser.getToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {

            public void onComplete(@NonNull final Task<GetTokenResult> task) {
                if (task.isSuccessful()) {
                    final String token = task.getResult().getToken();
                    if (token != null) {
                        ((App) ContextHolder.get()).getTokenHolder().refreshToken(token);
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
            Log.e(TAG, "Semaphore acquire error!");
        }
    }

    //TODO Move to error handling
    private boolean isTokenExpiredError(final String pErrorResponse) {
        final String error;
        try {
            final JSONObject errorObject = new JSONObject(pErrorResponse);
            error = errorObject.getString(ErrorConstants.ERROR_KEY);
        } catch (final JSONException pE) {
            return false;
        }
        return ErrorConstants.ERROR_VALUE_AUTH_TOKEN_IS_EXPIRED.equals(error);
    }

    @Override
    public void setErrorHandler(final IHttpErrorHandler pErrorHandler) {
        mErrorHandler = pErrorHandler;
    }
}