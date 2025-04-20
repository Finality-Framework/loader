package team.rainfall.finality.loader.util;

import team.rainfall.finality.FinalityLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

public class OSUtil {
    public static String getSystem(){
        return System.getProperty("os.name") + " " + System.getProperty("os.version")+","+System.getProperty("os.arch");
    }
    public static String getJavaInfo(){
        return System.getProperty("java.vendor")+" "+System.getProperty("java.vm.name")+" "+System.getProperty("java.version");
    }
    public static String getProcessorIdentifier(){
        return System.getenv("PROCESSOR_IDENTIFIER");
    }
    public static String getCpuName() {
        if(!System.getProperty("os.name").contains("Windows")){
            return "Unsupported";
        }
        try {
            Process process = Runtime.getRuntime().exec("wmic cpu get Name");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty() && !line.trim().equals("Name")) {
                    return line.trim();
                }
            }
        } catch (IOException e) {
            FinalityLogger.error("Failed to get CPU name", e);
        }
        return "Unknown CPU"; // 如果无法获取，返回未知CPU
    }

}
