package utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class JsonReader {
    public static List<LecturePOJO> readLecturesFromFile(String filePath) {
        List<LecturePOJO> lectures = new ArrayList<>();

        try (FileReader reader = new FileReader(filePath)) {
            Gson gson = new Gson();
            Type lectureListType = new TypeToken<List<LecturePOJO>>() {}.getType();
            lectures = gson.fromJson(reader, lectureListType);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return lectures;
    }
}
