package lab.kulebin.mydictionary.http;

import java.util.Map;

public interface IHttpClient {

    enum RequestType {GET, PUT, POST, DELETE}

    String get(String url);

    String get(String url, Map<String, String> headers);

    String put(String url, Map<String, String> headers, String body);

    String delete(String url);

    void setErrorHandler(IHttpErrorHandler errorHandler);

    class Impl {

        public static IHttpClient newInstance() {
            return new HttpClient();
        }

    }
}
