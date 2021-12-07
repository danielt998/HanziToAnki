package hanziToAnki;

import org.apache.tika.Tika;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FileUtils {
    private static final String ZIP_COMPRESSED = "application/x-zip-compressed";
    private static final String ZIP = "application/zip";
    private static final String X_GZIP = "application/x-gzip";
    private static final String GZIP = "application/gzip";

    public static List<String> fileToStringArray(String filename) {
        return fileToStringArray(new File(filename));
    }

    public static List<String> fileToStringArray(File file) {
        try {
            String contentType = new Tika().detect(file);

            switch (contentType) {
                case ZIP, ZIP_COMPRESSED -> { return getZipLines(file); }
                case GZIP, X_GZIP -> { return getGZipLines(file); }
                default -> {
                    System.out.println("Mediatype detected: " + contentType + ", attempting to read lines");
                    return Files.readAllLines(file.toPath());
                }
            }
        } catch (IOException exception) {
            System.out.println("Could not read lines from file at " + file.getPath());
            exception.printStackTrace();
            return new ArrayList<>();
        }
    }

    private static List<String> getZipLines(File file) {
        List<String> allLines = new ArrayList<>();
        try (ZipFile zip = new ZipFile(file)) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                InputStream inputStream = zip.getInputStream(zipEntry);
                List<String> lines = readLinesFromStream(inputStream);
                allLines.addAll(lines);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return allLines;
    }

    // Probably single file with .gz compression
    private static List<String> getGZipLines(File file) {
        try {
            GZIPInputStream gzipInputStream = new GZIPInputStream(new FileInputStream(file));
            return readLinesFromStream(gzipInputStream);
        } catch (IOException e) {
            System.out.println("Unable to read Gzip file");
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private static List<String> readLinesFromStream(InputStream stream) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            return reader.lines().collect(Collectors.toList());
        } catch (IOException e) {
            System.out.println("Unable to read stream from zipped file");
            return Collections.emptyList();
        }
    }

    public static void writeToFile(List<String> lines, String outputFilename) {
        try {
            Files.write(Paths.get(outputFilename), lines);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
