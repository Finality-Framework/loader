package team.rainfall.finality.installer;

import team.rainfall.finality.FinalityLogger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * The FileProcessor class provides methods to process files by reading their content,
 * modifying it based on certain conditions, and writing the modified content back to the file.
 * @author RedreamR
 */
public class FileProcessor {

    /**
     * Processes the file at the given file path by reading its content, modifying it based on the provided parameters,
     * and writing the modified content back to the file.
     *
     * @param filePath the path of the file to process
     * @param a the string to search for in the file content
     * @param b the string to insert into the file content
     * @param c the string to check for skipping lines in the file content
     * @author RedreamR
     */
    public static void processFile(String filePath, String a, String b,String c) {
        // 读取文件内容到列表中
        List<String> lines = readFile(filePath);
        // 处理列表中的内容
        List<String> processedLines = new ArrayList<>();
        int insertCounter = -1;
        boolean bl2 = false;
        for (String line : lines) {
            if(insertCounter >= 0) insertCounter++;
            if (insertCounter == 2) {
                FinalityLogger.debug("insert b");
                processedLines.add(b);
            }
            if (line.contains(a) && !bl2) {
                FinalityLogger.debug("bl2 + contain a");
                insertCounter = 0;
                bl2 = true;
            }
            if (!line.contains(c) || insertCounter == -1) {
                FinalityLogger.debug("skip c：" + line);
                processedLines.add(line);
            }

        }
        // 将处理后的内容写回文件
        writeFile(filePath, processedLines);
    }

    /**
     * Reads the content of the file at the given file path and returns it as a list of strings.
     *
     * @param filePath the path of the file to read
     * @return a list of strings representing the content of the file
     * @author RedreamR
     */
    private static List<String> readFile(String filePath) {
        try {
            return Files.readAllLines(Paths.get(filePath));
        } catch (IOException e) {
            FinalityLogger.error("Failed to read file: " + filePath, e);
            return new ArrayList<>();
        }
    }

    /**
     * Writes the given list of strings to the file at the given file path.
     *
     * @param filePath the path of the file to write to
     * @param lines a list of strings representing the content to write to the file
     * @author RedreamR
     */
    private static void writeFile(String filePath, List<String> lines) {
        try {
            Files.write(Paths.get(filePath), lines);
        } catch (IOException e) {
            FinalityLogger.error("Failed to write file: " + filePath, e);
        }
    }

}
