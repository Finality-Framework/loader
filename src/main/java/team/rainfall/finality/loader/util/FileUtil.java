package team.rainfall.finality.loader.util;

import team.rainfall.finality.FinalityLogger;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

public class FileUtil {
    public static void createPrivateDir() {
        File file = new File("./.finality");
        if (!file.exists()) {
            boolean ignored = file.mkdir();
        }
    }

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
        } catch (Exception ignored) {
        }
    }

    public static String readString(File file) {
        try {
            return new String(Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            FinalityLogger.error("Failed while readString(File)", e);
            return null;
        }
    }

    public static String readString_UTF8(File file) {
        try {
            return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            FinalityLogger.error("Failed while readString(File)", e);
            return null;
        }
    }

    public static void clearAndWriteFile(File file, String content) throws IOException {
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file, false), StandardCharsets.UTF_8)) {
            writer.write(content);
        }
    }

    public static byte[] calculateSHA256(File file) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (
                FileInputStream fis = new FileInputStream(file);
                FileChannel channel = fis.getChannel();
                DigestInputStream ignored = new DigestInputStream(fis, digest)) {
            ByteBuffer buffer = ByteBuffer.allocate(8192); // 8 KB buffer
            while (channel.read(buffer) != -1) {
                buffer.flip();
                digest.update(buffer);
                buffer.clear();
            }
            return digest.digest();
        }
    }
}
