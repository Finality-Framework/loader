package team.rainfall.finality.installer;

import net.platinumdigitalgroup.jvdf.VDFNode;
import net.platinumdigitalgroup.jvdf.VDFParser;
import team.rainfall.finality.FinalityLogger;
import team.rainfall.finality.loader.gui.ErrorCode;
import team.rainfall.finality.loader.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A class for processing files.
 * @see Installer
 * @since 1.3.0
 * @author RedreamR
 */
public class FileProcessor {
    /**
     * This method process files by a special way.
     * @see Installer
     * @param filePath a file path should be processed.
     * @param a starting key for process,if the line contains this key,then start process.
     * @param b content to insert
     * @param c if the line contains this key,then remove this line.
     */
    public static void processFile(String filePath, String a, String b,String c) {
        // 读取文件内容到列表中
        List<String> lines = readFile(filePath);
        // 处理列表中的内容
        List<String> processedLines = new ArrayList<>();
        InsertStatus status = InsertStatus.NONE;
        for (String line : lines) {
            if (line.contains(a) && status == InsertStatus.NONE) {
                status = InsertStatus.KEY;
            }
            if (line.contains("{") && status == InsertStatus.KEY) {
                status = InsertStatus.START_BRACE;
                processedLines.add(line);
                continue;
            }
            if (status == InsertStatus.START_BRACE) {
                processedLines.add(b);
                status = InsertStatus.END_BRACE;
            }
            if (line.contains("}") && status == InsertStatus.END_BRACE) {
                status = InsertStatus.FOUND;
            }
            if(line.contains(c) && status == InsertStatus.END_BRACE){
                continue;
            }
            processedLines.add(line);
        }
        // 将处理后的内容写回文件
        writeFile("./generated.vdf", processedLines);
        validate();
        writeFile(filePath, processedLines);
    }

    private static List<String> readFile(String filePath) {
        try {
            return Files.readAllLines(Paths.get(filePath));
        } catch (IOException e) {
            FinalityLogger.error("Failed to read file: " + filePath, e);
            return new ArrayList<>();
        }
    }

    private static void writeFile(String filePath, List<String> lines) {
        try {
            Files.write(Paths.get(filePath), lines);
        } catch (IOException e) {
            FinalityLogger.error("Failed to write file: " + filePath, e);
        }
    }
    private static void validate(){
        VDFParser parser = new VDFParser();
        File config = new File("./generated.vdf");
        VDFNode node2 = parser.parse(Objects.requireNonNull(FileUtil.readString_UTF8(config)));
        try {
            String string = node2.getSubNode("UserLocalConfigStore").getSubNode("Software").getSubNode("Valve").getSubNode("Steam").getSubNode("apps").getSubNode("2772750").getString("LaunchOptions");
        }catch (Exception e){
            ErrorCode.showInternalError("Aria - 04");
            FinalityLogger.error("Failed to validate generated vdf file,to keep your steam client safe,installer will exit!", e);
            System.exit(1);
        }
    }
}

enum InsertStatus{
    NONE,
    KEY,
    START_BRACE,
    FOUND,
    END_BRACE
}
