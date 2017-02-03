package lab.kulebin.mydictionary.http;

public interface IHttpErrorHandler {

    void handleError(final Exception e);

    class Impl {
        public static IHttpErrorHandler newInstance() {
            return new HttpErrorHandler();
        }
    }
}
