package team.rainfall.finality.loader.util;

import java.io.*;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.*;

public class ZipMerger {
    public static void mergeZipFiles(File[] sourceZipFiles, File destinationZipFile) throws IOException {
        mergeZipFiles(sourceZipFiles, destinationZipFile, new String[0]);
    }

    public static void mergeZipFiles(File[] sourceZipFiles, File destinationZipFile, String[] excludedPrefixes) throws IOException {
        String[] normalizedPrefixes = normalizePrefixes(excludedPrefixes);
        Set<String> entries = new HashSet<>();
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(destinationZipFile.toPath()))) {
            byte[] buffer = new byte[4096];
            for (int i = sourceZipFiles.length - 1; i >= 0; i--) {
                File sourceZipFile = sourceZipFiles[i];
                Set<String> excludeEntries = new HashSet<>();
                try (ZipInputStream excludeZis = new ZipInputStream(Files.newInputStream(sourceZipFile.toPath()))) {
                    ZipEntry entry;
                    while ((entry = excludeZis.getNextEntry()) != null) {
                        if (entry.getName().equals("exclude.txt")) {
                            ByteArrayOutputStream content = new ByteArrayOutputStream();
                            int len;
                            while ((len = excludeZis.read(buffer)) > 0) {
                                content.write(buffer, 0, len);
                            }
                            String excludeList = content.toString("UTF-8");
                            String[] excludedFiles = excludeList.split(";");
                            for (String file : excludedFiles) {
                                excludeEntries.add(file.trim());
                            }
                            break;
                        }
                        excludeZis.closeEntry();
                    }
                }
                try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(sourceZipFile.toPath()))) {
                    ZipEntry entry;
                    while ((entry = zis.getNextEntry()) != null) {
                        String entryName = entry.getName();
                        if (entryName.equals("exclude.txt") || excludeEntries.contains(entryName)) {
                            continue;
                        }
                        if (isExcluded(entryName, normalizedPrefixes)) {
                            continue;
                        }
                        if (entries.contains(entryName)) {
                            continue;
                        }
                        zos.putNextEntry(new ZipEntry(entryName));
                        entries.add(entryName);

                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            zos.write(buffer, 0, len);
                        }
                        zos.closeEntry();
                        zis.closeEntry();
                    }
                }
            }
        }
    }

    private static String[] normalizePrefixes(String[] prefixes) {
        if (prefixes == null) {
            return new String[0];
        }
        String[] normalized = new String[prefixes.length];
        for (int i = 0; i < prefixes.length; i++) {
            String prefix = prefixes[i];
            if (prefix == null) {
                continue;
            }
            prefix = prefix.trim();
            if (prefix.isEmpty()) {
                continue;
            }
            prefix = prefix.replace('.', '/');
            if (!prefix.endsWith("/")) {
                prefix = prefix + "/";
            }
            normalized[i] = prefix;
        }
        return normalized;
    }

    private static boolean isExcluded(String entryName, String[] prefixes) {
        for (String prefix : prefixes) {
            if (prefix != null && entryName.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}
