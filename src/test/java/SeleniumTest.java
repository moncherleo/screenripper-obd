import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;

public class SeleniumTest {
    public static void main(String[] args) {
        // Set up ChromeDriver using WebDriverManager
        WebDriverManager.chromedriver().setup();

        // Create ChromeOptions and set custom User-Agent, disable specific properties, and enable headless mode
        ChromeOptions options = new ChromeOptions();
        //options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.159 Safari/537.36");
        options.addArguments("--disable-blink-features=AutomationControlled");

        // Create a new instance of ChromeDriver with options
        WebDriver driver = new ChromeDriver(options);

        // Maximize the window
        driver.manage().window().maximize();

        // Open a website
        System.out.println("Opening a website");
        driver.get("https://astoundcommerce.udemy.com/");
        System.out.println("Opened a website");

        // Provide the path to the cookies.json file
        String filePath = "src/test/resources/cookies.json";

        // Convert JSON to Set<Cookie>
        Set<Cookie> cookies = CookieConverter.convertJsonToCookies(filePath);

        // Add cookies
        for (Cookie cookie : cookies) {
            System.out.println("Adding cookie: " + cookie.getName());
            driver.manage().addCookie(cookie);
        }

        // Get course page
        System.out.println("Navigating to the course page");
        driver.get("https://astoundcommerce.udemy.com/course/lean-six-sigma-white-belt-certification");
        System.out.println("Course page loaded");

        // Wait for the login to complete and page to load
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.urlContains("/lean-six-sigma-white-belt-certification"));

        // Wait for the parent element to be present
        System.out.println("Waiting for parent element to be present");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@data-purpose='curriculum-section-container']/div")));
        System.out.println("Parent element is present");

        // Find the parent element
        List<WebElement> courseSections = driver.findElements(By.xpath("//div[@data-purpose='curriculum-section-container']/div"));

        // Process the inner elements
        System.out.println("Sections found: " + courseSections.size());

        // Get the first section
        WebElement currentCourseSection = courseSections.get(0);

        // Check if the lectures of the current section are visible
        WebElement currentCourseSectionButton = currentCourseSection.findElement(By.xpath(".//button[1]"));
        boolean isCurrentCourseSectionExpanded = Boolean.parseBoolean(currentCourseSectionButton.getAttribute("aria-expanded"));

        if (!isCurrentCourseSectionExpanded) {
            // Wait for the button to be clickable
            System.out.println("Waiting for the button to be clickable");
            wait.until(ExpectedConditions.elementToBeClickable(currentCourseSectionButton));
            System.out.println("Button is clickable");

            // Click on the first section
            System.out.println("Clicking on the first section");
            currentCourseSectionButton.click();
            System.out.println("First section clicked");
        } else {
            // Find all lectures of the current section
            List<WebElement> lectureSections = currentCourseSection.findElements(By.xpath(".//ul[@class='ud-unstyled-list']/li"));

            // Get the fourth lecture
            WebElement currentLecture = lectureSections.get(3);

            // Wait for the lecture to be clickable
            System.out.println("Waiting for the lecture to be clickable");
            wait.until(ExpectedConditions.elementToBeClickable(currentLecture));
            System.out.println("Lecture is clickable");

            // Click on the fourth lecture
            System.out.println("Clicking on the fourth lecture");
            currentLecture.click();
            System.out.println("Fourth lecture clicked");
        }

        // Find the video element
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//video")));
        WebElement videoElement = driver.findElement(By.xpath("//video"));

        // Move the mouse over the video element
        Actions actions = new Actions(driver);
        actions.moveToElement(videoElement).perform();

        // Move the mouse 10 pixels left and right repeatedly for 5 times
        int moveCount = 5;
        int xOffsetPix = 10;
        int delayMs = 500;

        for (int i = 0; i < moveCount; i++) {
            // Move left
            actions.moveByOffset(-xOffsetPix, 0).perform();
            System.out.println("Moved left");

            // Wait for a short duration
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Move right
            actions.moveByOffset(xOffsetPix, 0).perform();
            System.out.println("Moved right");

            // Wait for a short duration
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[@data-purpose='video-control-bar']")));
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[@data-purpose='play-button']")));
        WebElement playButton = driver.findElement(By.xpath("//button[@data-purpose='play-button']"));
        playButton.click();
        System.out.println("Clicked play button");

        // Wait before closing the browser
        try {
            Thread.sleep(100000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // Close the browser
        driver.quit();
    }
}