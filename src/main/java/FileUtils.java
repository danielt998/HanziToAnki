import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;


public class FileUtils {
    private static final String DESTINATION_DIR = "/home/dtm/CHANGE_DIR_NAME";
    private static final int BUFFER_SIZE = 4096;
    private static final String ZIP = "application/x-zip-compressed";
    private static final String GZIP = "application/x-gzip";

    public static List<String> fileToStringArray(String filename) {
        return fileToStringArray(new File(filename));
    }


    private static List<String> getUnzippedLines(File file) {
        List<String> combinedList = new ArrayList<>();
        try {
            ZipFile zip = new ZipFile(file);

            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                InputStream inputStream = zip.getInputStream(zipEntry);
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                while(reader.ready()) {
                    combinedList.add(reader.readLine());
                }
                inputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return combinedList;
    }

    private static void extractFileFromZip(ZipInputStream zipIn, String filePath) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytesIn = new byte[BUFFER_SIZE];
        int read = 0;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
    }

    public static List<String> fileToStringArray(File file) {
        try {
            String contentType = Files.probeContentType(file.toPath());
            switch (contentType) {
                case ZIP -> { return getUnzippedLines(file); }
                case GZIP -> throw new IOException("gzip not yet supported");
                default -> { return Files.readAllLines(file.toPath()); }
            }
        } catch (IOException exception) {
            System.out.println("Could not read lines from input file at " + file.getPath());
            exception.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static void writeToFile(List<String> lines, String outputFileName) {
        try {
            Files.write(Paths.get(outputFileName), lines, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String removeExtensionFromFileName(String originalFileName) {
        return originalFileName.substring(0, originalFileName.lastIndexOf("."));
    }
}
