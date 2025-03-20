package team.rainfall.finality.loader.plugin;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * <p>The TweakManifest class represents the manifest of a tweak.
 * <p>It contains metadata about the tweak such as its SDK version, package name, <br>
 * and lists of tweak and publicized classes.
 * <p>This class is responsible for parsing the manifest information from an input stream.
 *
 * @author RedreamR
 */
public class TweakManifest {

    public ArrayList<String> tweakClasses = new ArrayList<>();
    public ArrayList<String> publicizedClasses = new ArrayList<>();
    public int sdkVersion;
    public String packageName;

    /**
     * <p>Constructs a TweakManifest object by parsing the provided input stream.
     *
     * @param is the input stream containing the tweak manifest JSON data
     * @author RedreamR
     */
    public TweakManifest(InputStream is){
        JSONObject jsonObject = JSON.parseObject(is, Charset.defaultCharset());
        sdkVersion = jsonObject.getInteger("sdkVersion");
        jsonObject.getJSONArray("tweakClasses").forEach(item -> tweakClasses.add((String) item));
        jsonObject.getJSONArray("publicizedClasses").forEach(item -> publicizedClasses.add((String) item));
        packageName = jsonObject.getString("packageName");
    }

}
