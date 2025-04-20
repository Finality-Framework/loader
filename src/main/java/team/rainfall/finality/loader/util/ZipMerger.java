package team.rainfall.finality.loader.util;

import java.io.*;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.*;

public class ZipMerger {
    public static void mergeZipFiles(File[] sourceZipFiles, File destinationZipFile) throws IOException {
        Set<String> entries = new HashSet<>();
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(destinationZipFile.toPath()))) {
            byte[] buffer = new byte[4096];
            for (int i = sourceZipFiles.length - 1; i >= 0; i--) {
                File sourceZipFile = sourceZipFiles[i];
                try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(sourceZipFile.toPath()))) {
                    ZipEntry entry;
                    while ((entry = zis.getNextEntry()) != null) {
                        if (entries.contains(entry.getName())) {
                            continue;
                        }
                        zos.putNextEntry(new ZipEntry(entry.getName()));
                        entries.add(entry.getName());
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

}
