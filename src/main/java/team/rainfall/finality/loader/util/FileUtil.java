package team.rainfall.finality.loader.util;
import team.rainfall.finality.FinalityLogger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

/**
 * <p>Utility class for file operations.</p>
 * <p>This class provides methods to handle various file-related operations such as creating directories,</p>
 * <p>deleting files based on their age, and reading/writing file contents.</p>
 * <p>Note: This class uses {@link Files} and {@link BasicFileAttributes} to perform file operations.</p>
 *
 * @see Files
 * @see BasicFileAttributes
 * @author RedreamR
 */
public class FileUtil {

    /**
     * <p>Creates a private directory named ".finality" in the current working directory.</p>
     * <p>If the directory does not exist, it will be created.</p>
     *
     * @author RedreamR
     */
    public static void createPrivateDir(){
        File file = new File("./.finality");
        if(!file.exists()){
            boolean ignored = file.mkdir();
        }
    }

    /**
     * <p>Deletes the specified file if it was created more than three days ago.</p>
     * <p>This method checks the creation time of the file and deletes it if the file is older than three days.</p>
     *
     * @param file the file to be checked and potentially deleted
     * @author RedreamR
     */
    public static void deleteFileIfThreeDaysPast(File file) {
        try {
            if (file.exists()) {
                // 获取文件的基本属性
                BasicFileAttributes attrs = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
                // 获取文件的创建时间
                Instant creationTime = attrs.creationTime().toInstant();
                // 获取当前时间
                Instant now = Instant.now();
                // 计算创建时间与当前时间的差异
                long daysBetween = ChronoUnit.DAYS.between(creationTime, now);

                // 如果差异大于或等于1天，删除文件
                if (daysBetween >= 1) {
                    boolean ignored = file.delete();
                }
            }
        }catch (Exception ignored){}
    }

    /**
     * <p>Reads the content of the specified file as a string.</p>
     * <p>This method reads all bytes from the file and converts them to a string.</p>
     *
     * @param file the file to be read
     * @return the content of the file as a string, or null if an error occurs
     * @author RedreamR
     */
    public static String readString(File file){
        try {
            return new String(Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            FinalityLogger.error("Failed while readString(File)",e);
            return null;
        }
    }

    /**
     * <p>Reads the content of the specified file as a UTF-8 encoded string.</p>
     * <p>This method reads all bytes from the file and converts them to a UTF-8 encoded string.</p>
     *
     * @param file the file to be read
     * @return the content of the file as a UTF-8 encoded string, or null if an error occurs
     * @author RedreamR
     */
    public static String readString_UTF8(File file){
        try {
            return new String(Files.readAllBytes(file.toPath()),StandardCharsets.UTF_8);
        } catch (IOException e) {
            FinalityLogger.error("Failed while readString(File)",e);
            return null;
        }
    }

    /**
     * <p>Clears the content of the specified file and writes the given content to it.</p>
     * <p>This method opens the file in write mode, clearing its existing content, and writes the new content to it.</p>
     *
     * @param file    the file to be written to
     * @param content the content to be written to the file
     * @throws IOException if an I/O error occurs
     * @author RedreamR
     */
    public static void clearAndWriteFile(File file, String content) throws IOException {
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file, false),StandardCharsets.UTF_8)) {
            writer.write(content);
        }
    }

}
