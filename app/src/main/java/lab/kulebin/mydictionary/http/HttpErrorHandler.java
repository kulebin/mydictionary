package lab.kulebin.mydictionary.http;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.Semaphore;

import lab.kulebin.mydictionary.App;
import lab.kulebin.mydictionary.R;
import lab.kulebin.mydictionary.app.SignInActivity;
import lab.kulebin.mydictionary.utils.ContextHolder;

import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;
import static java.net.HttpURLConnection.HTTP_UNAVAILABLE;
import static lab.kulebin.mydictionary.http.ErrorConstants.ERROR_VALUE_PERMISSION_DENIED;

class HttpErrorHandler implements IHttpErrorHandler {

    private static final String TAG = HttpErrorHandler.class.getSimpleName();
    private boolean isTokenRefreshedSuccessfully;
    private final Context mContext;

    HttpErrorHandler(final Context pContext) {
        this.mContext = pContext;
    }

    @Override
    public void handleError(final IOException pException) {
        if (pException instanceof HttpRequestException) {
            final HttpRequestException httpRequestException = (HttpRequestException) pException;
            switch (httpRequestException.getResponseCode()) {
                case HTTP_UNAUTHORIZED:
                    final String errorMessage = parseError(httpRequestException.getMessage());
                    if (isTokenExpiredError(errorMessage)) {
                        refreshToken();
                        if (isTokenRefreshedSuccessfully) {
                            repeatRequest(httpRequestException);
                        } else {
                            final Intent intent = new Intent(mContext, SignInActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            mContext.startActivity(intent);
                        }
                    } else {
                        final String dialogText;
                        if (ERROR_VALUE_PERMISSION_DENIED.equals(errorMessage)) {
                            dialogText = ContextHolder.get().getString(R.string.TEXT_DIALOG_ERROR_UNAUTHORIZED_REQUEST);
                        } else {
                            dialogText = errorMessage;
                        }
                        showErrorDialog(ContextHolder.get().getString(R.string.TITLE_DIALOG_ERROR_UNAUTHORIZED_REQUEST),
                                dialogText);
                    }
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
        } else {
            showErrorDialog(ContextHolder.get().getString(R.string.TITLE_DIALOG_ERROR_NO_INTERNET_CONNECTION),
                    ContextHolder.get().getString(R.string.TEXT_DIALOG_ERROR_NO_INTERNET_CONNECTION));
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

        final Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {

            @Override
            public void run() {
                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
                alertDialogBuilder
                        .setTitle(pTitle)
                        .setMessage(pMessage)
                        .setCancelable(true)
                        .setPositiveButton(mContext.getString(R.string.BUTTON_DIALOG_POSITIVE), new DialogInterface.OnClickListener() {

                            public void onClick(final DialogInterface dialog, final int id) {
                                dialog.dismiss();
                            }
                        });

                final AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        });

    }

    private void refreshToken() {
        final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        isTokenRefreshedSuccessfully = false;

        final Semaphore semaphore = new Semaphore(0);

        firebaseUser.getToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {

            public void onComplete(@NonNull final Task<GetTokenResult> task) {
                if (task.isSuccessful()) {
                    final String token = task.getResult().getToken();
                    if (token != null) {
                        ((App) ContextHolder.get()).getTokenHolder().refreshToken(token);
                    }
                    isTokenRefreshedSuccessfully = true;
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

    private boolean isTokenExpiredError(final String pErrorMessage) {
        return ErrorConstants.ERROR_VALUE_AUTH_TOKEN_IS_EXPIRED.equals(pErrorMessage);
    }

    private void repeatRequest(final HttpRequestException pE) {
        final HttpRequest oldHttpRequest = pE.getHttpRequest();
        final HttpRequest newHttpRequest = new HttpRequest.Builder()
                .setRequestType(oldHttpRequest.getRequestType())
                .setUrl(UrlBuilder.replaceTokenInUrl(oldHttpRequest.getUrl()))
                .setHeaders(oldHttpRequest.getHeaders())
                .setBody(oldHttpRequest.getBody())
                .build();
        ((App) ContextHolder.get()).getHttpClient().doRequest(newHttpRequest, pE.getIOnResult());
    }
}
