package qqmsg.qqmsg;

import android.content.Context;
import android.telephony.TelephonyManager;

import static android.content.Context.TELEPHONY_SERVICE;

public class Utils {

    public static final String getImei(Context mContext) {
        String imei = null;
        try {
            imei = ((TelephonyManager) mContext.getSystemService(TELEPHONY_SERVICE)).getDeviceId();
        } catch (SecurityException e) {
            //e.printStackTrace();
        }
        return imei;
    }
}
