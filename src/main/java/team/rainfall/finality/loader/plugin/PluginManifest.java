package team.rainfall.finality.loader.plugin;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class PluginManifest {
    public int sdkVersion;
    public int version;
    public String id;
    public Boolean hasTweaker;
    public String tweaker;
    public Boolean useLuminosity = false;
    public ArrayList<String> tweakClasses = new ArrayList<>();
    public PluginManifest(InputStream is){
        JSONObject jsonObject = JSON.parseObject(is, Charset.defaultCharset());
        useLuminosity = jsonObject.getBoolean("useLuminosity");
        if(useLuminosity) {
            jsonObject.getJSONArray("tweakClasses").forEach(item -> tweakClasses.add((String) item));
        }
        sdkVersion = jsonObject.getInteger("sdkVersion");
        version = jsonObject.getInteger("version");
        id = jsonObject.getString("id");
        hasTweaker = jsonObject.getBoolean("hasTweaker");
        tweaker = jsonObject.getString("tweaker");
        if(useLuminosity == null){
            useLuminosity = false;
        }
    }
    public PluginManifest(){}
}
