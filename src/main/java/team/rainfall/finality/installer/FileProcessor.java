package team.rainfall.finality.installer;

import team.rainfall.finality.FinalityLogger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileProcessor {
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

    private static List<String> readFile(String filePath) {
        try {
            return Files.readAllLines(Paths.get(filePath));
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private static void writeFile(String filePath, List<String> lines) {
        try {
            Files.write(Paths.get(filePath), lines);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
