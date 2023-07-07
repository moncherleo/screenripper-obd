package utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
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

}

