package lab.kulebin.mydictionary.http;

import android.net.Uri;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Map;

import lab.kulebin.mydictionary.App;
import lab.kulebin.mydictionary.utils.ContextHolder;

public final class UrlBuilder {

    private UrlBuilder() {
    }

    public static String getUrl(final String[] pathSegments, final Map<String, String> queries) {
        final Uri.Builder uriBuilder = Uri.parse(Api.BASE_URL)
                .buildUpon()
                .appendPath(FirebaseAuth.getInstance().getCurrentUser().getUid());

        for (int i = 0; i < pathSegments.length; i++) {
            String pathSegment = pathSegments[i];
            if (i == pathSegments.length - 1) {
                pathSegment += Api.JSON_FORMAT;
            }
            uriBuilder.appendPath(pathSegment);
        }

        if (queries != null) {
            for (final String queryKey : queries.keySet()) {
                uriBuilder.appendQueryParameter(queryKey, queries.get(queryKey));
            }
        }

        return uriBuilder.build().toString();
    }

    public static String getPersonalisedUrl(final String[] pathSegments, final Map<String, String> queries) {
        return Uri.parse(getUrl(pathSegments, queries))
                .buildUpon()
                .appendQueryParameter(Api.PARAM_AUTH, ((App) ContextHolder.get()).getTokenHolder().getToken())
                .build()
                .toString();
    }

    public static String getPersonalisedUrl(final String url) {
        return Uri.parse(url)
                .buildUpon()
                .appendQueryParameter(Api.PARAM_AUTH, ((App) ContextHolder.get()).getTokenHolder().getToken())
                .build()
                .toString();
    }

    public static String replaceTokenInUrl(final String pUrl) {
        final String expiredToken = Uri.parse(pUrl).getQueryParameter(Api.PARAM_AUTH);
        return pUrl.replace(expiredToken, ((App) ContextHolder.get()).getTokenHolder().getToken());
    }
}
