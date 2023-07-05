import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.FileHelper;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class CourseLinksCollectorTest {
    public static void main(String[] args) {
        // Set up ChromeDriver using WebDriverManager
        WebDriverManager.chromedriver().setup();

        // Create ChromeOptions and set custom User-Agent, disable specific properties, and enable headless mode
        ChromeOptions options = new ChromeOptions();
        //options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.159 Safari/537.36");
        options.addArguments("--disable-blink-features=AutomationControlled");

        // Create a new instance of ChromeDriver with options and maximize the window
        WebDriver driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        String websiteConfigPath = "src/test/resources/website.txt";
        String cookieJSONpath = "src/test/resources/cookies.json";
        String coursePageURLPath = "src/test/resources/page.txt";

        // Specify the output directory path
        String outputDirectoryPath = "src/test/resources/output/";

        // Create a list to hold the lecture data
        List<Map<String, String>> lectureDataList = new ArrayList<>();

        String currentURL = "";

        // Open a website
        System.out.println("Opening a website");
        String websiteURL = FileHelper.readFirstLineFromFile(websiteConfigPath);
        driver.get(websiteURL);
        System.out.println("Opened a website: " + websiteURL);

        // Add cookies to browser
        Set<Cookie> cookies = CookieConverter.convertJsonToCookies(cookieJSONpath);
        for (Cookie cookie : cookies) {
            driver.manage().addCookie(cookie);
        }
        System.out.println("Cookies are added to the browser.");

        // Get course start page
        System.out.println("Navigating to the course page");
        String coursePageURL = FileHelper.readFirstLineFromFile(coursePageURLPath);
        driver.get(coursePageURL);
        System.out.println("Course page loaded: " + coursePageURL);

        // Wait for the parent element to be present
        System.out.println("Waiting for curriculum to be present");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@data-purpose='curriculum-section-container']/div")));
        System.out.println("Curriculum is present");

        // Get the course title
        String pageTitle = driver.getTitle();
        System.out.println("Webpage Title: " + pageTitle);

        // Find the list of course sections
        List<WebElement> courseSections = driver.findElements(By.xpath("//div[@data-purpose='curriculum-section-container']/div"));
        System.out.println("Sections found: " + courseSections.size());

        // Process the inner elements
        for (int sectionIndex = 0; sectionIndex < courseSections.size(); sectionIndex++) {
            // Get the current section
            courseSections = driver.findElements(By.xpath("//div[@data-purpose='curriculum-section-container']/div"));
            WebElement currentCourseSection = courseSections.get(sectionIndex);

            // Check if the lectures of the current section are visible
            WebElement currentSectionButton = currentCourseSection.findElement(By.xpath(".//button[contains(@class, 'js-panel-toggler')]"));
            boolean isCurrentCourseSectionExpanded = Boolean.parseBoolean(currentSectionButton.getAttribute("aria-expanded"));

            // If the lectures of the current section are not visible - expand section
            if (!isCurrentCourseSectionExpanded) {
                // Wait for the button to be clickable
                System.out.println("Waiting for the current section button to be clickable");
                wait.until(ExpectedConditions.elementToBeClickable(currentSectionButton));
                System.out.println("Current section button is clickable");

                // Click on the first section
                System.out.println("Clicking on the current section");
                currentSectionButton.click();
                System.out.println("Current section clicked");
            }

            // Find all lectures of the current section
            courseSections = driver.findElements(By.xpath("//div[@data-purpose='curriculum-section-container']/div"));
            currentCourseSection = courseSections.get(sectionIndex);
            List<WebElement> lectureSections = currentCourseSection.findElements(By.xpath(".//ul[@class='ud-unstyled-list']/li"));

            for (int lectureIndex = 0; lectureIndex < lectureSections.size(); lectureIndex++) {
                // Find the list of course sections
                courseSections = driver.findElements(By.xpath("//div[@data-purpose='curriculum-section-container']/div"));
                currentCourseSection = courseSections.get(sectionIndex);

                // Find all lectures of the current section
                lectureSections = currentCourseSection.findElements(By.xpath(".//ul[@class='ud-unstyled-list']/li"));

                // Get the current lecture
                WebElement currentLecture = lectureSections.get(lectureIndex);

                // Wait for the lecture to be clickable
                System.out.println("Waiting for the section #" + (sectionIndex + 1) + ", lecture #" + (lectureIndex + 1) + " to be clickable");
                wait.until(ExpectedConditions.elementToBeClickable(currentLecture));
                System.out.println("Section #" + (sectionIndex + 1) + ", lecture #" + (lectureIndex + 1) + " is clickable");

                // Click on the current lecture
                System.out.println("Clicking on the lecture #" + (lectureIndex + 1) + " in the section #" + (sectionIndex + 1));
                currentLecture.click();
                System.out.println("Lecture #" + (lectureIndex + 1) + " in the section " + (sectionIndex + 1) + " is clicked");

                // Wait before checking the element
                int min = 2500;
                int max = 5000;
                int randomNumber = ThreadLocalRandom.current().nextInt(min, max + 1);
                try {
                    Thread.sleep(randomNumber);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                // Wait until footer is loaded
                WebElement footerLogo = driver.findElement(By.xpath("//div[@class='logo-container']"));
                wait.until(ExpectedConditions.visibilityOf(footerLogo));

                // Check if there is at least one the video elements on the page
                List<WebElement> videoElements = driver.findElements(By.xpath("//video"));

                if (videoElements.size() > 0) {
                    // Get the current page URL
                    currentURL = driver.getCurrentUrl();
                    System.out.println("Video is found");

                    // Get lecture name
                    String lectureName = currentLecture.findElement(By.xpath(".//span[@data-purpose='item-title']")).getText();
                    System.out.println("Video lecture name: " + lectureName);
                    System.out.println("Video lecture page URL: " + currentURL);

                    // Inside the loop for each lecture
                    Map<String, String> lectureData = new HashMap<>();
                    lectureData.put("currentURL", currentURL);
                    lectureData.put("lectureName", lectureName);
                    lectureDataList.add(lectureData);

                } else {
                    System.out.println("No video is found in the section #" + (sectionIndex + 1) + " lecture #" + (lectureIndex + 1) + ", moving to the next lecture");
                }

                // Close coding exercise
                List<WebElement> closeCodingChallengeButtons = driver.findElements(By.xpath("//button[@data-purpose='open-course-content']"));
                if (closeCodingChallengeButtons.size() > 0){
                    closeCodingChallengeButtons.get(0).click();
                }
            }

            // Just for nice formatting in console
            System.out.println("-----------------------------------------------------");
        }

        // Convert the data to JSON string
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonData = gson.toJson(lectureDataList);

        // Replace unacceptable characters in the Webpage Title with underscores
        String fileName = pageTitle.replaceAll("[<>:\"/\\\\|?*]", "_") + ".json";

        // Write the JSON data to a file in the output directory
        try (FileWriter writer = new FileWriter(outputDirectoryPath + fileName)) {
            writer.write(jsonData);
            System.out.println("Data saved to file: " + outputDirectoryPath + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Wait before closing the browser
        try {
            Thread.sleep(10*1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // Close the browser
        driver.quit();
    }
}