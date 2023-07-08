package utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class FileHelper {
    public static void main(String[] args) {

        System.out.println(FileHelper.normalizeFileName("4. Lean Six Sigma White Belt: AIGPE Six Sigma White Belt Certification Requirements.mkv"));
    }

    public static String readFirstLineFromFile(String filePath) {
        String firstLine = null;
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(filePath));
            firstLine = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return firstLine;
    }

    public static String normalizeFileName(String filename) {
        return filename.replaceAll("[<>:\"/\\\\|?*\\.\\s]", "_");
    }

    public static void renameMostRecentFile(String folderPath, String newFilename) {
        File folder = new File(folderPath);

        // Get the list of files in the folder
        File[] files = folder.listFiles();

        // Sort the files by last modified timestamp in descending order
        Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());

        if (files.length > 0) {
            File mostRecentFile = files[0];

            // Get the file extension
            String extension = getFileExtension(mostRecentFile);

            // Create the new file name with the specified filename and the original extension
            String newFileName = newFilename + "." + extension;

            try {
                // Rename the most recent file
                Path sourcePath = mostRecentFile.toPath();
                Path targetPath = new File(folder, newFileName).toPath();
                Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("File renamed successfully: " + newFileName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No files found in the specified folder.");
        }
    }

    private static String getFileExtension(File file) {
        String name = file.getName();
        int lastDotIndex = name.lastIndexOf(".");
        if (lastDotIndex != -1 && lastDotIndex < name.length() - 1) {
            return name.substring(lastDotIndex + 1);
        }
        return "";
    }

    public static String extractBaseURL(String url) {
        URL baseURL = null;
        try {
            baseURL = new URL(url);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        String protocol = baseURL.getProtocol();
        String host = baseURL.getHost();
        int port = baseURL.getPort();

        // Construct the base URL
        StringBuilder baseURLBuilder = new StringBuilder();
        baseURLBuilder.append(protocol).append("://").append(host);

        // Append the port number if it's specified
        if (port != -1) {
            baseURLBuilder.append(":").append(port);
        }

        return baseURLBuilder.toString();
    }

    public static List<String> readLinesFromFile(String filePath) {
        List<String> lines = new ArrayList<>();
        Path path = Paths.get(filePath);

        try {
            lines = Files.readAllLines(path);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return lines;
    }

}

