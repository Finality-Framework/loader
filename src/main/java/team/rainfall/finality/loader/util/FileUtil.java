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
                BasicFileAttributes attrs = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
                Instant creationTime = attrs.creationTime().toInstant();
                Instant now = Instant.now();
                long daysBetween = ChronoUnit.DAYS.between(creationTime, now);
                if (daysBetween >= 2) {
                    boolean ignored = file.delete();
                }
            }
        } catch (Exception ignored) {
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

    public static byte[] calculateSHA256(File file) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (FileInputStream fis = new FileInputStream(file);
             DigestInputStream dis = new DigestInputStream(fis, digest)) {
            byte[] buffer = new byte[8192]; // 8 KB buffer
            while (dis.read(buffer) != -1) {
                // just wait for read
            }
            return digest.digest();
        }
    }
}
