import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Set;

public class SeleniumTest {
    public static void main(String[] args) {
        // Set up ChromeDriver using WebDriverManager
        WebDriverManager.chromedriver().setup();

        // Create a new instance of ChromeDriver
        WebDriver driver = new ChromeDriver();

        driver.manage().window().maximize();

        // Open a website
        driver.get("https://astoundcommerce.udemy.com/");

        // Provide the path to the cookies.json file
        String filePath = "src/test/resources/cookies.json";

        // Convert JSON to Set<Cookie>
        Set<Cookie> cookies = CookieConverter.convertJsonToCookies(filePath);

        // Print the converted cookies
        for (Cookie cookie : cookies) {
            driver.manage().addCookie(cookie);
            System.out.println(cookie.toString());
        }

        driver.get("https://astoundcommerce.udemy.com/home/my-courses/");

/*
        // Enter login credentials and click "Continue"
        WebElement emailField = findElementWithRetries(driver, By.name("email"));
        emailField.sendKeys("l.chernyshov@astoundcommerce.com");

        WebElement continueButton = findElementWithRetries(driver, By.xpath("//button[contains(., 'Continue')]"));
        continueButton.click();

        // Enter password and click "Continue" again
        WebElement passwordField = findElementWithRetries(driver, By.name("password"));
        passwordField.sendKeys("l.chernyshov@astoundcommerce.com12345_");

        // Click the "Log In" button
        WebElement loginButton = findElementWithRetries(driver, By.xpath("//button[contains(., 'Log In')]"));
        loginButton.click();
*/
        // Wait before browser closed
        try {
            Thread.sleep(100000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // Close the browser
        driver.quit();
    }
/*
    private static WebElement findElementWithRetries(WebDriver driver, By by) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        int maxRetries = 3;
        int retryCount = 0;

        while (retryCount < maxRetries) {
            try {
                WebElement element = wait.until(ExpectedConditions.elementToBeClickable(by));
                return element;
            } catch (NoSuchElementException | org.openqa.selenium.TimeoutException e) {
                System.out.println("Element not found or not interactable. Retrying...");
                retryCount++;
            }
        }

        throw new NoSuchElementException("Element not found or not interactable even after retries");
    }
    */


}
