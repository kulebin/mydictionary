package lab.kulebin.mydictionary.utils;


import android.support.annotation.Nullable;

public class Converter {


    private static final String STRING_SEPARATOR = "\\|\\|";

    @Nullable
    public static String[] convertStringToStringArray(String pString) {
        if(pString != null){
            return pString.split(STRING_SEPARATOR);
        }
        return null;
    }

    @Nullable
    public static String convertStringArrayToString(String[] pStringArray) {
        if (pStringArray != null) {
            if (pStringArray.length == 1) {
                return pStringArray[0];
            } else if (pStringArray.length > 1) {
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < pStringArray.length; i++) {
                    builder.append(pStringArray[i]);
                    if (i < pStringArray.length - 1) {
                        builder.append(STRING_SEPARATOR);
                    }
                }
                return builder.toString();
            }
        }
        return null;
    }
}
