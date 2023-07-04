import com.google.gson.Gson;
import org.openqa.selenium.Cookie;

import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class CookieConverter {
    public static void main(String[] args) {
        // Provide the path to the cookies.json file
        String filePath = "path/to/cookies.json";

        // Convert JSON to Set<Cookie>
        Set<Cookie> cookies = convertJsonToCookies(filePath);

        // Print the converted cookies
        for (Cookie cookie : cookies) {
            System.out.println(cookie.toString());
        }
    }

    public static Set<Cookie> convertJsonToCookies(String filePath) {
        Set<Cookie> cookies = new HashSet<>();
        Gson gson = new Gson();

        try (FileReader reader = new FileReader(filePath)) {
            CookieJson[] cookieJsonArray = gson.fromJson(reader, CookieJson[].class);
            for (CookieJson cookieJson : cookieJsonArray) {
                Cookie.Builder builder = new Cookie.Builder(cookieJson.name, cookieJson.value)
                        .domain(cookieJson.domain)
                        .path(cookieJson.path)
                        .expiresOn(cookieJson.getExpiryDate())
                        .isSecure(cookieJson.secure)
                        .isHttpOnly(cookieJson.httpOnly);

                cookies.add(builder.build());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return cookies;
    }

    private static class CookieJson {
        private String domain;
        private double expirationDate;
        private boolean hostOnly;
        private boolean httpOnly;
        private String name;
        private String path;
        private boolean secure;
        private boolean session;
        private String storeId;
        private String value;

        public Date getExpiryDate() {
            long expiryMillis = (long) (expirationDate * 1000);
            return new Date(expiryMillis);
        }
    }
}
