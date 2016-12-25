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

public class HttpClient implements IHttpClient {

    public static final String DELETE_RESPONSE_OK = "null";
    private static final String TAG = HttpClient.class.getSimpleName();

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

            final boolean isSuccess = connection.getResponseCode() >= 200 && connection.getResponseCode() < 300;
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
                throw new Exception(response);
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
    }

    private void applyBody(final HttpURLConnection httpURLConnection, final String body) throws Exception {
        final byte[] outputInBytes = body.getBytes("UTF-8");
        final OutputStream os = httpURLConnection.getOutputStream();
        os.write(outputInBytes);
        os.close();
    }

    private enum RequestType {GET, PUT, POST, DELETE}
}