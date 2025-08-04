package team.rainfall.finality.loader.game;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import team.rainfall.finality.FinalityLogger;

import java.io.InputStream;

public class VersionDetector {
    Version[] versions;
    public static VersionDetector detector = new VersionDetector();
    public void loadVersions(){
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("version.json")) {
            if (is == null) {
                throw new RuntimeException("version.json not found in JAR");
            }
            versions = JSON.parseObject(is, Version[].class, JSONReader.Feature.SupportAutoType);
        } catch (Exception e) {
            FinalityLogger.error("Failed to load versions",e);
        }
    }
    public boolean isKnownVersion(String hashString){
        for (Version version : versions) {
            if(version.hashStr.equalsIgnoreCase(hashString)){
                return true;
            }
        }
        return false;
    }
    public int getVersionID(String hashString){
        for (Version version : versions) {
            if(version.hashStr.equalsIgnoreCase(hashString)){
                return version.id;
            }
        }
        return 0;
    }
    public String getVersionDesc(String hashString){
        for (Version version : versions) {
            if(hashString.equalsIgnoreCase(version.hashStr)){
                return version.desc;
            }
        }
        return "";
    }
}
@SuppressWarnings("unused")
class Version{
    int id;
    String desc;
    String hashStr;
    public Version() {
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getHashStr() {
        return hashStr;
    }

    public void setHashStr(String hashStr) {
        this.hashStr = hashStr;
    }

    @Override
    public String toString() {
        return "Version{id=" + id + ", hashStr='" + hashStr + "'}";
    }
}
