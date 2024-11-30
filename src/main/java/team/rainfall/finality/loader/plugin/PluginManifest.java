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
    public PluginManifest(InputStream is){
        JSONObject jsonObject = JSON.parseObject(is, Charset.defaultCharset());
        sdkVersion = jsonObject.getInteger("sdkVersion");
        version = jsonObject.getInteger("version");
        id = jsonObject.getString("id");
        useLuminosity = jsonObject.getBoolean("useLuminosity");
        hasTweaker = jsonObject.getBoolean("hasTweaker");
        tweaker = jsonObject.getString("tweaker");
        if(useLuminosity == null){
            useLuminosity = false;
        }
    }
    public PluginManifest(){}
}
