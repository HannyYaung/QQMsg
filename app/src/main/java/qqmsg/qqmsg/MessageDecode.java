package qqmsg.qqmsg;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MessageDecode {
    public String imeiID;
    public int imeiLen;
    public MessageDecode(String imeiID)
    {
        this.imeiID = imeiID;
        this.imeiLen = imeiID.length();
    }

    public boolean isChinese(byte ch) {
        int res = ch & 0x80;
        if(res != 0)
            return true;
        return false;
    }

    public String timeDecode(String time)
    {
        String datetime = "1970-01-01 08:00:00";
        SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            long second = Long.parseLong(time);
            Date dt = new Date(second * 1000);
            datetime = sdFormat.format(dt);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return datetime;
    }

    public String nameDecode(String name)
    {
        byte nbyte[] = name.getBytes();
        byte ibyte[] = imeiID.getBytes();
        byte xorName[] = new byte[nbyte.length];
        int index = 0;
        for(int i = 0; i < nbyte.length; i++) {
            if(isChinese(nbyte[i])){
                xorName[i] = nbyte[i];
                i++;
                xorName[i] = nbyte[i];
                i++;
                xorName[i] = (byte)(nbyte[i] ^ ibyte[index % imeiLen]);
                index++;
            } else {
                xorName[i] = (byte)(nbyte[i] ^ ibyte[index % imeiLen]);
                index++;
            }
        }
        return new String(xorName);
    }

    public String uinDecode(String uin)
    {
        byte ubyte[] = uin.getBytes();
        byte ibyte[] = imeiID.getBytes();
        byte xorMsg[] = new byte[ubyte.length];
        int index = 0;
        for(int i = 0; i < ubyte.length; i++) {
            xorMsg[i] = (byte)(ubyte[i] ^ ibyte[index % imeiLen]);
            index++;
        }
        return new String(xorMsg);
    }

    public String msgDecode(byte[] msg)
    {
        byte ibyte[] = imeiID.getBytes();
        byte xorMsg[] = new byte[msg.length];
        int index = 0;
        for(int i = 0; i < msg.length; i++) {
            xorMsg[i] = (byte)(msg[i] ^ ibyte[index % imeiLen]);
            index++;
        }
        return new String(xorMsg);
    }
}