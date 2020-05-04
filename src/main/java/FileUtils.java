import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public class FileUtils {
    private static final String DESTINATION_DIR = "/home/dtm/CHANGE_DIR_NAME";
    private static final int BUFFER_SIZE = 4096;

    public static List<String> fileToStringArray(String filename) {
        return fileToStringArray(new File(filename));
    }


    public static List<String> getUnzippedLines(File file) {
        List<String> combinedList = new ArrayList<>();
        try {
            ZipInputStream inputStream = new ZipInputStream(new FileInputStream(file.getCanonicalPath()));
            if (inputStream.getNextEntry() == null) {
                return null;//not a valid zipped file
            }
            ZipEntry entry = inputStream.getNextEntry();
            while (entry != null) {
                String outputPath = DESTINATION_DIR + File.separator + entry.getName();
                if (!entry.isDirectory()) {
                    extractFileFromZip(inputStream, outputPath);
                    combinedList.addAll(fileToStringArray(new File(outputPath)));
                } else {//not sure whether these two are absolutely necessary
                    File dir = new File(outputPath);
                    dir.mkdir();
                }
                inputStream.closeEntry();
                entry = inputStream.getNextEntry();
            }
            inputStream.close();
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
        List<String> unzippedLines = getUnzippedLines(file);
        if (unzippedLines != null) {
            return unzippedLines;
        }
        List<String> lines = new ArrayList<String>();
        try {
            Scanner scanner = new Scanner(new FileReader(file));
            while (scanner.hasNextLine()) {
                lines.add(scanner.nextLine());
            }
            scanner.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return lines;
    }

    public static void writeToFile(List<String> lines, String outputFileName) {
        try {
            Files.write(Paths.get(outputFileName), lines, Charset.defaultCharset());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String removeExtensionFromFileName(String originalFileName) {
        return originalFileName.substring(0, originalFileName.lastIndexOf("."));
    }
}
