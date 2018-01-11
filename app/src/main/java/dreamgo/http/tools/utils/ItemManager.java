package dreamgo.http.tools.utils;

import android.os.Environment;
import android.util.Log;

import org.apache.http.util.EncodingUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ykh on 2016/11/14.
 */

public class ItemManager {
    private static ItemManager itemManager = new ItemManager();
    public ItemManager(){super();}
    public static ItemManager getManager(){
        return itemManager;
    }

    private JSONObject items = new JSONObject();

    public void addItem(String itemName,String itemValue){
        try {
            items.put(itemName,itemValue);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getItem(String itemName){
        String item="";
        try {
            item = items.getString(itemName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return item;
    }

    public void saveItems() {
        try{
            File path = Environment.getExternalStorageDirectory();
            File dir = new File(path,"HttpTools/");
            if(!dir.exists())
                dir.mkdirs();
            File file= new File(dir.getAbsolutePath(), "items.txt");
            FileOutputStream fout = new FileOutputStream(file);
            byte [] bytes = items.toString().getBytes();

            fout.write(bytes);
            fout.close();
            Log.e("storage","文件保存成功 ");
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public JSONObject readItems() {
        Log.e("storage","------------------start read--------------------------");
        JSONObject jsonObject = new JSONObject();
        try{
            File path = Environment.getExternalStorageDirectory();
            File dir = new File(path, "HttpTools/");
            if(!dir.exists())
                dir.mkdirs();
            File devices= new File(dir.getAbsolutePath(), "items.txt");
            if(!devices.exists()){
                boolean flag = devices.createNewFile();
                Log.e("storage","文件不存在 创建该文件结果"+flag);
            }
            FileInputStream fin = new FileInputStream(devices);

            int length = fin.available();

            byte [] buffer = new byte[length];
            fin.read(buffer);

            String res = EncodingUtils.getString(buffer, "UTF-8");
            jsonObject = new JSONObject(res);

            fin.close();
            Log.e("storage","文件读取结果 "+ jsonObject.toString(4));
        }
        catch(Exception e){
            e.printStackTrace();
        }
        Log.e("storage","------------------end read--------------------------");
        this.items = jsonObject;
        return jsonObject;
    }

}
