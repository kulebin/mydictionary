package lab.kulebin.mydictionary.http;

public class HttpRequestException extends Exception {

    private static final long serialVersionUID = 5326458803268867891L;

    private final int mResponseCode;

    public HttpRequestException(final int pResponseCode, final String pErrorMessage) {
        super(pErrorMessage);
        this.mResponseCode = pResponseCode;
    }

    public int getResponseCode() {
        return mResponseCode;
    }
}
