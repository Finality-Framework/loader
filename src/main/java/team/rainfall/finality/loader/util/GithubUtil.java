package team.rainfall.finality.loader.util;

import com.alibaba.fastjson2.JSONObject;
import kong.unirest.Unirest;
import team.rainfall.finality.FinalityLogger;
import team.rainfall.finality.loader.FileManager;
import team.rainfall.finality.loader.Main;
import team.rainfall.finality.loader.VersionType;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

/**
 * <p>Utility class for interacting with GitHub and Gitee repositories.</p>
 * <p>This class provides methods to check for updates, bypass SSL certificate verification, and get repository links.</p>
 * <p>Note: This class uses Unirest for HTTP requests and Fastjson for JSON parsing.</p>
 *
 * @author RedreamR
 */
public class GithubUtil {

    public static final String GITHUB_REPO_LINK = "https://github.com/finality-framework/loader";
    public static final String GITEE_REPO_LINK = "https://gitee.com/finality-framework/loader";
    public static final String GITHUB_LATEST_RELEASE_LINK = "https://api.github.com/repos/finality-framework/loader/releases/latest";
    public static final String GITEE_LATEST_RELEASE_LINK = "https://gitee.com/api/v5/repos/finality-framework/loader/releases/latest";
    public static String latestVersion = Main.VERSION;

    /**
     * <p>Checks for updates by comparing the current version with the latest release version from the repository.</p>
     *
     * @return true if an update is available, false otherwise
     * @author RedreamR
     */
    public static boolean checkUpdate() {
       if(Main.VERSION_TYPE == VersionType.DEV){
           //Bypass update
           return false;
       }

        FileUtil.deleteFileIfThreeDaysPast(FileManager.INSTANCE.getFile("./.finality/update"));
        if(CacheCheck()) return false;

        try {
            Unirest.config().verifySsl(false);
            String response = Unirest.get(getLocaleAPILink()).asJson().getBody().toString();
            JSONObject jsonObject = JSONObject.parseObject(response);
            String tag = jsonObject.getString("tag_name");
            File file = FileManager.INSTANCE.getFile("./.finality/update");
            boolean ignored = file.createNewFile();
            if(tag.equals(Main.VERSION)){
                return false;
            }else {
                latestVersion = tag;
                return true;
            }
        } catch (Exception e) {
            FinalityLogger.error("Failed to check for updates", e);
            return false;
        }
    }

    /**
     * <p>Checks if the update cache file exists.</p>
     *
     * @return true if the cache file exists, false otherwise
     * @author RedreamR
     */
    public static boolean CacheCheck() {
        File file = FileManager.INSTANCE.getFile("./.finality/update");
        return file.exists();
    }

    /**
     * <p>Creates an SSLContext that bypasses SSL certificate verification.</p>
     *
     * @return an SSLContext that does not verify SSL certificates
     * @throws NoSuchAlgorithmException if the specified algorithm is not available
     * @throws KeyManagementException if there is an error initializing the SSLContext
     * @author RedreamR
     */
    public static SSLContext createIgnoreVerifySSL() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sc = SSLContext.getInstance("TLS");
        // 实现一个X509TrustManager接口，用于绕过验证，不用修改里面的方法
        X509TrustManager trustManager = new X509TrustManager() {
            @Override
            public void checkClientTrusted(
                    java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
                    String paramString) throws CertificateException {
            }

            @Override
            public void checkServerTrusted(
                    java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
                    String paramString) throws CertificateException {
            }

            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };

        sc.init(null, new TrustManager[]{trustManager}, null);
        return sc;
    }

    /**
     * <p>Gets the API link for the latest release based on the locale.</p>
     *
     * @return the API link for the latest release
     * @author RedreamR
     */
    public static String getLocaleAPILink(){
        if(Localization.isChinese()){
            return GITEE_LATEST_RELEASE_LINK;
        }else {
            return GITHUB_LATEST_RELEASE_LINK;
        }
    }

    /**
     * <p>Gets the repository link based on the locale.</p>
     *
     * @return the repository link
     * @author RedreamR
     */
    public static String getLocaleRepoLink(){
        if(Localization.isChinese()){
            return GITEE_REPO_LINK;
        }else {
            return GITHUB_REPO_LINK;
        }
    }

    /**
     * <p>Opens the release page in the default web browser.</p>
     */
    public static void openReleasePage(){

    }

}
