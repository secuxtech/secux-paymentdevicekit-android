package com.secuxtech.paymentdevicekit;

import android.util.Log;

/**
 * Created by maochuns.sun@gmail.com on 2020-03-02
 */
public class SecuXPaymentUtility {
    public static String dataToHexString(byte[] bytes) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (bytes == null || bytes.length <= 0) {
            return null;
        }
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        String result = stringBuilder.toString();
        return result;
    }

    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    public static String getDefaultValue(String inputString, String vlaue) {
        String returnString = "";
        if (isEmpty(inputString) || "null".equals(inputString)) {
            returnString = vlaue;
        } else {
            returnString = inputString;
        }
        return returnString;
    }

    public static void debug(String message) {
        if (BuildConfig.DEBUG) {
            StackTraceElement call = Thread.currentThread().getStackTrace()[3];
            String className = call.getClassName();
            className = className.substring(className.lastIndexOf('.') + 1);
            Log.d(className + "." + call.getMethodName(), message);

        }
    }

    public static String byte2Hex(byte[] b) {
        String result = "";
        for (int i=0 ; i<b.length ; i++)
            result += Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );

        return result;
    }

    /**
     * 將字串轉換為數字，如果轉換失敗則回傳預設值
     * <ul>
     * <li>ConvertUtils.str2Int(null) = 0</li>
     * <li>ConvertUtils.str2Int("abc") = 0</li>
     * <li>ConvertUtils.str2Int("1") = 1</li>
     * </ul>
     *
     * @param sValue
     *            待轉換之字串
     * @param iDefaultValue
     *            轉換失敗時之預設值
     * @return 轉換後之數字，失敗則傳回iDefaultValue
     *
     */
    public static int str2Int(String sValue, int iDefaultValue) {
        int iValue = iDefaultValue;
        try {
            iValue = Integer.parseInt(sValue);
        }
        catch (Exception e) {
            iValue = iDefaultValue;
        }

        return iValue;
    }

    /**
     * 將字串轉成Integer
     *
     * @param sValue
     *            待轉換的字串
     * @return 轉換後的數字，如果無法轉換時回傳0
     */
    public static int str2Int(String sValue) {
        return str2Int(sValue, 0);
    }
}
