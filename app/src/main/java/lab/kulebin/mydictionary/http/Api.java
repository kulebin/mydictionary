package lab.kulebin.mydictionary.http;

import android.net.Uri;

import com.google.firebase.auth.FirebaseAuth;

public final class Api {

    public static final String JSON_FORMAT = ".json";
    public static final String PARAM_AUTH = "auth";
    public static final String IMAGES_FOLDER = "images/";
    private static final String BASE_URL = "https://my-dictionary-a2be8.firebaseio.com/users";
    public static final String USERS = "users";

    public static String getBaseUrl() {
        final String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final Uri uri = Uri.parse(Api.BASE_URL).buildUpon()
                .appendPath(userUid)
                .build();
        return uri.toString();
    }
}
