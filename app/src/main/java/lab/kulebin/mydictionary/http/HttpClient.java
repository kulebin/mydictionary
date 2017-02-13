package lab.kulebin.mydictionary.http;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

class HttpClient implements IHttpClient {

    private static final String TAG = HttpClient.class.getSimpleName();
    private static final int READ_TIMEOUT = 10000;
    private static final int CONNECTION_TIMEOUT = 15000;

    public void doRequest(final HttpRequest pHttpRequest, final IOnResult pIOnResult) {

        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            final URL reqUrl = new URL(pHttpRequest.getUrl());
            connection = ((HttpURLConnection) reqUrl.openConnection());
            connection.setRequestMethod(pHttpRequest.getRequestType().name());
            connection.setReadTimeout(READ_TIMEOUT);
            connection.setConnectTimeout(CONNECTION_TIMEOUT);
            final Map<String, String> headers = pHttpRequest.getHeaders();
            if (headers != null) {
                for (final String key : pHttpRequest.getHeaders().keySet()) {
                    connection.addRequestProperty(key, headers.get(key));
                }
            }
            if (pHttpRequest.getBody() != null) {
                applyBody(connection, pHttpRequest.getBody());
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
            final String response = stringBuilder.toString();

            inputStream.close();

            if (isSuccess) {
                pIOnResult.onSuccess(response);
            } else {
                pIOnResult.onError(new HttpRequestException(responseCode, response, pHttpRequest, pIOnResult));
            }

        } catch (final IOException e) {
            pIOnResult.onError(e);
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
    }

    private void applyBody(final HttpURLConnection httpURLConnection, final String body) throws IOException {
        final byte[] outputInBytes = body.getBytes("UTF-8");
        final OutputStream os = httpURLConnection.getOutputStream();
        os.write(outputInBytes);
        os.close();
    }
}