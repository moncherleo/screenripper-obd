package utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class FileHelper {
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

    public static String normalizeFileName (String filename){
        return filename.replaceAll("[<>:\"/\\\\|?*]", "_");
    }
}

