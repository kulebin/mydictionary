package lab.kulebin.mydictionary.http;

import android.content.Intent;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import lab.kulebin.mydictionary.Constants;
import lab.kulebin.mydictionary.R;
import lab.kulebin.mydictionary.app.ErrorDialogActivity;
import lab.kulebin.mydictionary.utils.ContextHolder;

import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;
import static java.net.HttpURLConnection.HTTP_UNAVAILABLE;
import static lab.kulebin.mydictionary.http.ErrorConstants.ERROR_VALUE_PERMISSION_DENIED;

public class HttpErrorHandler implements IHttpErrorHandler {

    private static final String TAG = HttpErrorHandler.class.getSimpleName();

    @Override
    public void handleError(final Exception pException) {
        if (pException instanceof HttpRequestException) {
            switch (((HttpRequestException) pException).getResponseCode()) {
                case HTTP_UNAUTHORIZED:
                    final String dialogText;
                    if (ERROR_VALUE_PERMISSION_DENIED.equals(parseError(pException.getMessage()))) {
                        dialogText = ContextHolder.get().getString(R.string.TEXT_DIALOG_ERROR_UNAUTHORIZED_REQUEST);
                    } else {
                        dialogText = parseError(pException.getMessage());
                    }
                    showErrorDialog(ContextHolder.get().getString(R.string.TITLE_DIALOG_ERROR_UNAUTHORIZED_REQUEST),
                            dialogText);
                    break;
                case HTTP_INTERNAL_ERROR:
                    showErrorDialog(ContextHolder.get().getString(R.string.TITLE_DIALOG_ERROR_SERVER_ERROR),
                            ContextHolder.get().getString(R.string.TEXT_DIALOG_ERROR_SERVER_ERROR));
                    break;
                case HTTP_UNAVAILABLE:
                    showErrorDialog(ContextHolder.get().getString(R.string.TITLE_DIALOG_ERROR_SERVER_UNAVAILABLE),
                            ContextHolder.get().getString(R.string.TEXT_DIALOG_ERROR_SERVER_UNAVAILABLE));
                    break;
                default:
                    showErrorDialog(ContextHolder.get().getString(R.string.TITLE_DIALOG_ERROR_DEFAULT),
                            parseError(pException.getMessage()));
            }
        } else if (pException instanceof IOException) {
            showErrorDialog(ContextHolder.get().getString(R.string.TITLE_DIALOG_ERROR_NO_INTERNET_CONNECTION),
                    ContextHolder.get().getString(R.string.TEXT_DIALOG_ERROR_NO_INTERNET_CONNECTION));
        } else {
            Log.e(TAG, pException.getMessage());
        }
    }

    private String parseError(final String pErrorResponse) {
        final String error;
        try {
            final JSONObject errorObject = new JSONObject(pErrorResponse);
            error = errorObject.getString(ErrorConstants.ERROR_KEY);
        } catch (final JSONException pE) {
            return pErrorResponse;
        }
        return error;
    }

    private void showErrorDialog(final CharSequence pTitle, final CharSequence pMessage) {
        final Intent intent = new Intent(ContextHolder.get(), ErrorDialogActivity.class)
                .putExtra(Constants.EXTRA_ERROR_DIALOG_TITLE, pTitle)
                .putExtra(Constants.EXTRA_ERROR_DIALOG_TEXT, pMessage)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ContextHolder.get().startActivity(intent);
    }
}
