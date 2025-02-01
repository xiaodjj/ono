package moe.ono.common;

import com.alibaba.fastjson2.JSON;
import com.google.gson.JsonParser;

public class CheckUtils {

    private CheckUtils() {
        throw new UnsupportedOperationException("No instances");
    }

    public static void checkNonNull(Object obj, String message) {
        if (obj == null) {
            throw new NullPointerException(message);
        }
    }

    public static boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isJSON(String text) {
        try {
            JSON.parse(text);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
