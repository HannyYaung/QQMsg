package qqmsg.qqmsg;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Until {
    public static char HEX_DIGITS[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F'};
    //将字符串转化为位
    public static String toHexString(byte[] b){
        StringBuilder stringBuilder = new StringBuilder(b.length * 2);
        for (int i = 0; i < b.length; i++) {
            stringBuilder.append(HEX_DIGITS[(b[i] & 0xf0) >>> 4]);
            stringBuilder.append(HEX_DIGITS[b[i] & 0x0f]);
        }
        return stringBuilder.toString();
    }
    public static String md5(String string){
        try {
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(string.getBytes());
            byte messageDigest[] = digest.digest();
            return toHexString(messageDigest);
        }catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }
        return "";
    }
}
