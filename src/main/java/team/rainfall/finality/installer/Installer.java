package team.rainfall.finality.installer;

import com.sun.jna.Platform;
import net.platinumdigitalgroup.jvdf.VDFNode;
import net.platinumdigitalgroup.jvdf.VDFParser;
import net.platinumdigitalgroup.jvdf.VDFWriter;
import team.rainfall.finality.FinalityLogger;
import team.rainfall.finality.loader.FileManager;
import team.rainfall.finality.loader.Loader;
import team.rainfall.finality.loader.VdfManager;
import team.rainfall.finality.loader.gui.ErrorCode;
import team.rainfall.finality.loader.util.FileUtil;
import team.rainfall.finality.loader.util.Localization;
import team.rainfall.finality.loader.util.StringUtil;

import java.io.*;
import java.net.URLDecoder;
import java.util.Map;
import java.util.Objects;

import static team.rainfall.finality.loader.Loader.copyFile;

public class Installer {
    public static void checkEnv() {
        if(!Platform.isWindows()){
            FinalityLogger.error("Unsupported operating system!");
            ErrorCode.showInternalError("Aria - 03");
            System.exit(1);
        }
        if(FileManager.parentFile == null){
            FileManager.INSTANCE.findGameFileByVDF();
        }
    }

    public static void install() {
        checkEnv();
        boolean shouldRestartSteam = false;
        FinalityLogger.localizeInfo("into_install_mode");
        VdfManager.getINSTANCE().findSteamInstallation();
        File userdataFolder = new File(VdfManager.getINSTANCE().steamPath, "userdata");
        File loginUserVDF = new File(VdfManager.getINSTANCE().steamPath, "config/loginusers.vdf");
        if (userdataFolder.exists() && userdataFolder.isDirectory() && loginUserVDF.exists()) {
            VDFParser parser = new VDFParser();
            VDFNode node = parser.parse(Objects.requireNonNull(FileUtil.readString_UTF8(loginUserVDF)));
            node = node.getSubNode("users");
            String accountName;
            for (Map.Entry<String, Object[]> stringEntry : node.entrySet()) {
                int temp1 = node.getSubNode(stringEntry.getKey()).getInt("MostRecent");
                if (temp1 == 1) {
                    String launchOption = "java -jar \"" + FileManager.parentFile.getAbsolutePath() + "\\Finality_Loader.jar\" -forceNoVDF -ignore %command%";
                    launchOption = StringUtil.escapeString(launchOption);

                    FinalityLogger.localizeInfo("installing");
                    accountName = node.getSubNode(stringEntry.getKey()).getString("AccountName");
                    String id64 = stringEntry.getKey();
                    FinalityLogger.info(String.format(Localization.bundle.getString("install_for"), accountName));
                    File folder = findUserFolder(id64);
                    File config = new File(folder, "config/localconfig.vdf");
                    VDFNode node2 = parser.parse(Objects.requireNonNull(FileUtil.readString_UTF8(config)));
                    VDFNode gameNode = node2.getSubNode("UserLocalConfigStore").getSubNode("Software").getSubNode("Valve").getSubNode("Steam").getSubNode("apps").getSubNode("2772750");
                    if (gameNode != null) {
                        gameNode.remove("LaunchOptions");
                        gameNode.put("LaunchOptions", launchOption);
                    } else {
                        FinalityLogger.error(String.format(Localization.bundle.getString("bad_vdf"), "localconfig.vdf"));
                        ErrorCode.showInternalError("Aria - 01");
                        System.exit(1);
                    }

                    try {
                        shouldRestartSteam = detectSteam();
                        FileProcessor.processFile(config.getAbsoluteFile().getAbsolutePath(), "\"2772750\"", "\t\t\t\t\t\t\"LaunchOptions\"\t\t\"" + launchOption + "\"", "\"LaunchOptions\"");
                        dropFile();
                    } catch (IOException e) {
                        ErrorCode.showInternalError("Aria - 02");
                        FinalityLogger.error("Failed to write out", e);
                        System.exit(1);
                    }
                }
            }
            FinalityLogger.localizeInfo("installation_complete");
            //重启Steam客户端
            if(shouldRestartSteam) {
                try {
                    Runtime.getRuntime().exec("cmd /c start steam://open/main");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            System.exit(0);
        } else {
            FinalityLogger.error(String.format(Localization.bundle.getString("bad_vdf"), "loginusers.vdf"));
            System.exit(1);
        }

    }

    private static File findUserFolder(String accountID) {
        accountID = SteamIdConverter.id64ToFriendCode(accountID,false);
        File userdataFolder = new File(VdfManager.getINSTANCE().steamPath, "userdata");
        //如果userdataFolder下只有一个目录，直接返回该目录的File对象
        File singleFolder = null;
        for(File file : Objects.requireNonNull(userdataFolder.listFiles())) {
            if(file.exists() && file.isDirectory()) {
                singleFolder = file;
                continue;
            }
            if(file.exists() && file.isDirectory() && singleFolder != null){
                singleFolder = null;
                break;
            }
        }
        if(singleFolder != null) {
            return singleFolder;
        }
        File file = new File(userdataFolder,accountID+"/config/localconfig.vdf");
        if(file.exists()) {
            return file;
        }
        return null;
    }

    private static boolean detectSteam() throws IOException {
        String taskListCommand = "tasklist";
        Process process = Runtime.getRuntime().exec(taskListCommand);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        boolean isSteamRunning = false;
        while ((line = reader.readLine()) != null) {
            if (line.contains("steam")) {
                isSteamRunning = true;
                break;
            }
        }

        if (isSteamRunning) {
            //杀掉steam进程
            String killCommand = "taskkill /F /IM steam.exe";
            Runtime.getRuntime().exec(killCommand);
            FinalityLogger.localizeInfo("steam_killed");
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                return true;
            }
            return true;
        }
        return false;
    }

    private static void dropFile() {
        String currentJarPath = Loader.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        try {
            currentJarPath = URLDecoder.decode(currentJarPath, "UTF-8");
            copyFile(new File(currentJarPath), new File(FileManager.parentFile, "Finality_Loader.jar"));
        } catch (Exception e) {
            FinalityLogger.error("WTF", e);
        }
    }
}
