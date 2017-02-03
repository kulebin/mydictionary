package lab.kulebin.mydictionary;

import android.content.Context;
import android.content.SharedPreferences;

import lab.kulebin.mydictionary.utils.ContextHolder;

public class TokenHolder {

    public static final String APP_PREFERENCES_USER_TOKEN = "AP_TOKEN";
    private String mToken;

    public TokenHolder() {
        final SharedPreferences shp = ContextHolder.get().getSharedPreferences(Constants.APP_PREFERENCES, Context.MODE_PRIVATE);
        mToken = shp.getString(APP_PREFERENCES_USER_TOKEN, null);
    }

    public String getToken() {
        return mToken;
    }

    public void refreshToken(final String pToken){
        mToken = pToken;
        storeToken();
    }

    private void storeToken() {
        final SharedPreferences appPreferences = ContextHolder.get().getSharedPreferences(Constants.APP_PREFERENCES, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = appPreferences.edit();
        editor.putString(APP_PREFERENCES_USER_TOKEN, mToken);
        editor.apply();
    }

}
