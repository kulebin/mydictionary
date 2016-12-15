package lab.kulebin.mydictionary.http;


import java.util.Map;

public interface IHttpClient {

    String get(String url) throws Exception;

    String get(String url, Map<String, String> headers) throws Exception;

    String put(String url, Map<String, String> headers, String body) throws Exception;

    String post(String url, Map<String, String> headers, String body) throws Exception;

    String delete(String url) throws Exception;
}
