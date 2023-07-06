package utils;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FilePathExtractor {
    public static String extractFilePath(String response) {
        String regex = "outputPath=(.*?)\\)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(response);

        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return null;
        }
    }

    public static String renameFile(String filePath, String newFileName) {
        File file = new File(filePath);

        if (!file.exists() || !file.isFile()) {
            return null; // File does not exist or is not a regular file
        }

        String parentDir = file.getParent();
        String extension = getFileExtension(filePath);
        String newFilePath;

        if (newFileName.contains(".")) {
            // The new file name contains an extension
            newFileName = newFileName.substring(0, newFileName.lastIndexOf('.'));
        }

        if (extension != null) {
            newFilePath = parentDir + File.separator + newFileName + extension;
        } else {
            newFilePath = parentDir + File.separator + newFileName;
        }

        File renamedFile = new File(newFilePath);
        boolean renamed = file.renameTo(renamedFile);

        if (renamed) {
            return renamedFile.getPath();
        } else {
            return null; // Renaming unsuccessful
        }
    }

    private static String getFileExtension(String filePath) {
        int extensionIndex = filePath.lastIndexOf('.');
        if (extensionIndex >= 0 && extensionIndex < filePath.length() - 1) {
            return filePath.substring(extensionIndex);
        } else {
            return null;
        }
    }
}
