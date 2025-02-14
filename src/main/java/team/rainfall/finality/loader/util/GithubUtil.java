package team.rainfall.finality.loader.util;

import com.alibaba.fastjson2.JSONObject;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import team.rainfall.finality.FinalityLogger;
import team.rainfall.finality.loader.Main;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public class GithubUtil {
    public static final String GITHUB_LATEST_RELEASE_LINK = "https://api.github.com/repos/finality-framework/loader/releases/latest";
    public static String latestVersion = Main.VERSION;
    public static boolean checkUpdate() {
        FileUtil.deleteFileIfThreeDaysPast(new File("./.finality/update"));
        if(CacheCheck()) return false;
        try {
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(createIgnoreVerifySSL(), SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            CloseableHttpClient httpclient = HttpClients.custom()
                    .setSSLSocketFactory(sslsf)
                    .build();
            Unirest.setHttpClient(httpclient);
            String response = Unirest.get(GITHUB_LATEST_RELEASE_LINK).asJson().getBody().toString();
            JSONObject jsonObject = JSONObject.parseObject(response);
            String tag = jsonObject.getString("tag_name");
            File file = new File("./.finality/update");
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
    public static boolean CacheCheck() {
        File file = new File("./.finality/update");
        return file.exists();
    }
    //Bypass SSL Cert
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
}
