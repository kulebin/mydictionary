package lab.kulebin.mydictionary.http;

import java.util.Map;

public interface IHttpClient {

    String get(String url);

    String get(String url, Map<String, String> headers);

    String put(String url, Map<String, String> headers, String body);

    String delete(String url);

    void setErrorHandler(IHttpErrorHandler errorHandler);

    enum RequestType {GET, PUT, POST, DELETE}
}
