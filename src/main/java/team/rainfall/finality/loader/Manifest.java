package team.rainfall.finality.loader;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class Manifest {
    public ArrayList<String> localMods = new ArrayList<>();
    public String gameFile;
    public boolean disableSteamAPI;
    public boolean debugMode;
    public Manifest(InputStream is){
        JSONObject jsonObject = JSON.parseObject(is, Charset.defaultCharset());
        disableSteamAPI = jsonObject.getBoolean("is_disable_steamapi");
        debugMode = jsonObject.getBoolean("debug_mode");
        gameFile = jsonObject.getString("game_file");
        //jsonObject.getJSONArray("local_mods").forEach(item -> localMods.add((String) item));
    }
    public Manifest(){
        gameFile = FileManager.INSTANCE.findGameFile();
        disableSteamAPI = false;
        debugMode = false;
    }
}
