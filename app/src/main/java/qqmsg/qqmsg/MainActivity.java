package qqmsg.qqmsg;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;


import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteDatabaseHook;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;

import static net.sqlcipher.database.SQLiteDatabase.NO_LOCALIZED_COLLATORS;


public class MainActivity extends AppCompatActivity {


    final String QQ_old_path = "/data/data/com.tencent.mobileqq/databases/807064763.db";
    final String QQ_new_path = "/mnt/sdcard/work/mywork/807064763.db";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        File file = new File("/mnt/sdcard/work/mywork");
        //判断文件夹是否存在，如果不存在就创建，否则不创建
        if (!file.exists()) {
            //通过file的mkdirs()方法创建目录中包含却不存在的文件夹
            file.mkdirs();
        }
        copyFile(QQ_old_path, QQ_new_path);
        readData();
    }

    private void readData() {
        SQLiteDatabase.loadLibs(this);
        String password = "";
        SQLiteDatabaseHook hook = new SQLiteDatabaseHook() {
            public void preKey(SQLiteDatabase database) {
            }

            public void postKey(SQLiteDatabase database) {
                database.rawExecSQL("PRAGMA cipher_migrate;");
            }
        };
        MessageDecode mDecode = new MessageDecode(Utils.getImei(this));
        HashMap<String, String> troopInfo = new HashMap<String, String>();
        try {
            SQLiteDatabase db = SQLiteDatabase.openDatabase(QQ_new_path, password, null, NO_LOCALIZED_COLLATORS, hook);
            long now = System.currentTimeMillis();
            Log.e("readQQDatabases", "读取QQ数据库:" + now);
            //读取所有的群信息
            String sql = "select troopuin,troopname from TroopInfoV2 where _id";
            Log.e("sql", sql);
            Cursor c = db.rawQuery(sql, null);
            while (c.moveToNext()) {
                String troopuin = c.getString(c.getColumnIndex("troopuin"));
                String troopname = c.getString(c.getColumnIndex("troopname"));
                String name = mDecode.nameDecode(troopname);
                String uin = mDecode.uinDecode(troopuin);
                Log.e("readQQDatanases", "读取结束:" + name);
                troopInfo.put(uin, name);
            }
            c.close();

            int troopCount = troopInfo.size();
            Iterator<String> it = troopInfo.keySet().iterator();
            JSONObject json = new JSONObject();
            //遍历所有的表
            while (troopCount > 0) {
                try {
                    while (it.hasNext()) {
                        String troopuin = (String) it.next();
                        String troopname = troopInfo.get(troopuin);
                        if (troopuin.length() < 8)
                            continue;
                        String troopuinMD5 = MD5Until.md5(troopuin);
//                        String troopMsgSql = "select _id,msgData, senderuin, time from mr_troop_" + troopuinMD5 + "_New";
                        String troopMsgSql = "select _id,msgData, senderuin, time from mr_troop_" + troopuinMD5 + "_New";
                        Log.e("sql", troopMsgSql);
                        Cursor cc = db.rawQuery(troopMsgSql, null);
                        JSONObject tmp = new JSONObject();
                        while (cc.moveToNext()) {
                            long _id = cc.getLong(cc.getColumnIndex("_id"));
                            byte[] msgByte = cc.getBlob(cc.getColumnIndex("msgData"));
                            String ss = mDecode.msgDecode(msgByte);
                            //图片不保留
                            if (ss.indexOf("jpg") != -1 || ss.indexOf("gif") != -1
                                    || ss.indexOf("png") != -1)
                                continue;
                            String time = cc.getString(cc.getColumnIndex("time"));
                            String senderuin = cc.getString(cc.getColumnIndex("senderuin"));
                            senderuin = mDecode.uinDecode(senderuin);
                            Log.e("msg", _id + "====" + ss + "=============" + senderuin + "=====" + time);
//                            JSONObject tmpJson = handleQQJson(_id, ss, senderuin, time);
//                            tmp.put(String.valueOf(_id), tmpJson);
                        }
                        troopCount--;
                        cc.close();
                    }
                } catch (Exception e) {
                    Log.e("e", "readWxDatabases" + e.toString());
                }
            }
            db.close();
        } catch (Exception e) {
            Log.e("e", "readWxDatabases" + e.toString());
        }
    }

    private void copyData() {
        final String QQ_old_path = "/data/data/com.tencent.mobileqq/databases/QQ号.db";
        final String QQ_new_path = "/data/data/com.android.saurfang/QQ号.db";
        copyFile(QQ_old_path, QQ_new_path);
    }

    /**
     * 复制单个文件
     *
     * @param oldPath String 原文件路径 如：c:/fqf.txt
     * @param newPath String 复制后路径 如：f:/fqf.txt
     * @return boolean
     */
    public static boolean copyFile(String oldPath, String newPath) {
        deleteFolderFile(newPath, true);
        Log.e("copyFile", "time_1:" + System.currentTimeMillis());
        InputStream inStream = null;
        FileOutputStream fs = null;
        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            Boolean flag = oldfile.exists();
            Log.e("copyFile", "flag:" + flag);
            if (oldfile.exists()) { //文件存在时
                inStream = new FileInputStream(oldPath); //读入原文件
                fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[2048];
                while ((byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; //字节数 文件大小
                    fs.write(buffer, 0, byteread);
                }
                Log.e("copyFile", "time_2:" + System.currentTimeMillis());
            }
        } catch (Exception e) {
            System.out.println("复制单个文件操作出错");
            e.printStackTrace();
        } finally {
            try {
                if (inStream != null) {
                    inStream.close();
                }
                if (fs != null) {
                    fs.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    /**
     * 删除单个文件
     *
     * @param filepath
     * @param deleteThisPath
     */
    public static void deleteFolderFile(String filepath, boolean deleteThisPath) {
        if (!TextUtils.isEmpty(filepath)) {
            try {
                File file = new File(filepath);
                if (file.isDirectory()) {
                    //处理目录
                    File files[] = file.listFiles();
                    for (int i = 0; i < file.length(); i++) {
                        deleteFolderFile(files[i].getAbsolutePath(), true);
                    }
                }
                if (deleteThisPath) {
                    if (!file.isDirectory()) {
                        //删除文件
                        file.delete();
                    } else {
                        //删除目录
                        if (file.listFiles().length == 0) {
                            file.delete();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
