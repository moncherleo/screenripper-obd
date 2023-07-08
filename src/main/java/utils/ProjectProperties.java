package utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ProjectProperties {
    String propertiesFilePath = "src/test/resources/properties.txt";
    private String cookieJSONFilePath;
    private String videoFolderPath;
    private String contentJSONFilePath;

    public ProjectProperties() {
        loadProperties(this.propertiesFilePath);
    }

    private void loadProperties(String filePath) {
        Properties properties = new Properties();
        try (FileInputStream fileInputStream = new FileInputStream(filePath)) {
            properties.load(fileInputStream);
            cookieJSONFilePath = properties.getProperty("cookieJSONFilePath");
            videoFolderPath = properties.getProperty("videoFolderPath");
            contentJSONFilePath = properties.getProperty("contentJSONFilePath");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getCookieJSONFilePath() {
        return cookieJSONFilePath;
    }

    public String getVideoFolderPath() {
        return videoFolderPath;
    }
    public String getContentJSONFilePath() {
        return contentJSONFilePath;
    }


    public static void main(String[] args) {
        ProjectProperties reader = new ProjectProperties();
        System.out.println("cookieJSONFilePath: " + reader.getCookieJSONFilePath());
        System.out.println("videoFolderPath: " + reader.getVideoFolderPath());
        System.out.println("contentJSONFilePath: " + reader.getContentJSONFilePath());
    }
}

