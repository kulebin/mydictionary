package lab.kulebin.mydictionary.http;


import android.net.Uri;

import com.google.firebase.auth.FirebaseAuth;

public class Api {

    private static final String BASE_URL = "https://my-dictionary-a2be8.firebaseio.com/users";
    public static final String ENTRIES = "entries";
    public static final String DICTIONARIES = "dictionaries";
    public static final String JSON_FORMAT = ".json";
    public static final String PARAM_AUTH = "auth";

    public static String getBaseUrl() {
        String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Uri uri = Uri.parse(Api.BASE_URL).buildUpon()
                .appendPath(userUid)
                .build();
        return uri.toString();
    }
}
