package team.rainfall.finality.loader.plugin;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class TweakManifest {
    public ArrayList<String> tweakClasses = new ArrayList<>();
    public ArrayList<String> publicizedClasses = new ArrayList<>();
    public int sdkVersion;
    public String packageName;
    public TweakManifest(InputStream is){
        JSONObject jsonObject = JSON.parseObject(is, Charset.defaultCharset());
        sdkVersion = jsonObject.getInteger("sdkVersion");
        jsonObject.getJSONArray("tweakClasses").forEach(item -> tweakClasses.add((String) item));
        jsonObject.getJSONArray("publicizedClasses").forEach(item -> publicizedClasses.add((String) item));
        packageName = jsonObject.getString("packageName");
    }

}
