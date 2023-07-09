import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.python.antlr.ast.Str;
import org.sikuli.script.FindFailed;
import utils.*;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ScreenRecorderTest {
    public static void main(String[] args) {

        ProjectProperties projectProperties = new ProjectProperties();
        VisualTestHelper visualTestHelper;
        Robot robot;

        // Create an instance of the Robot class
        try {
            robot = new Robot();
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }

        // Reading properties
        System.out.println("Loading project properties");
        String cookieJSONFilePath = projectProperties.getCookieJSONFilePath();
        String videoFolderPath = projectProperties.getVideoFolderPath();
        String outputDirectoryPath = "src/test/resources/output/";
        List<LecturePOJO> lectures = JsonReader.readLecturesFromFile(projectProperties.getContentJSONFilePath());
        System.out.println("Properties are loaded");

        System.out.println("Setting up Selenium Webdriver with Chrome");
        // Set up ChromeDriver using WebDriverManager
        WebDriverManager.chromedriver().setup();

        // Create ChromeOptions and set custom User-Agent
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-blink-features=AutomationControlled");
        System.setProperty("webdriver.chrome.verboseLogging", "true");

        // Create a new instance of ChromeDriver with options and maximize the window
        WebDriver driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        System.out.println("Selenium Webdriver with Chrome is launched");

        // Open a website
        String websiteURL = FileHelper.extractBaseURL(lectures.get(0).getCurrentURL());
        System.out.println("Opening a website: " + websiteURL);
        driver.get(websiteURL);
        System.out.println("Website is opened");

        // Close Selenium top bar
        try {
            new VisualTestHelper().closeSeleniumBrowserNotification();
        } catch (FindFailed e) {
            throw new RuntimeException(e);
        }

        // Add cookies to browser
        Set<Cookie> cookies = CookieConverter.convertJsonToCookies(cookieJSONFilePath);
        for (Cookie cookie : cookies) {
            driver.manage().addCookie(cookie);
        }
        System.out.println("Cookies are added to the Chrome browser");

        // Starting content processing in the big cycle
        System.out.println("Starting content processing...");
        for (int j = 0; j < lectures.size(); j++) {
            System.out.println("Current content name: " + lectures.get(j).getLectureName());
            System.out.println("Current content URL: " + lectures.get(j).getCurrentURL());

            driver.get(lectures.get(j).getCurrentURL());
            System.out.println("Opening the content page...");

            // Polling the page to get at least one content element on the page
            System.out.println("Polling the page to get at least one content element on the page");
            List<WebElement> videoElements = null;
            while (videoElements == null || videoElements.size() < 1) {
                videoElements = driver.findElements(By.xpath("//video"));
            }
            System.out.println(videoElements.size() + " content elements found");

            // Pause the content if it is playing now
            ScreenRecorderTest.pauseContentPlayback(driver);

            // Disable content autoplay
            System.out.println("Disabling content autoplay");
            System.out.println("Opening Settings menu");
            WebElement settingsButton = driver.findElement(By.xpath("//button[@data-purpose='settings-button']"));
            settingsButton.click();
            System.out.println("Settings menu opened");
            WebElement autoplayToggle = driver.findElement(By.xpath("//ul[@data-purpose='settings-menu']/li[3]//button"));
            System.out.println("Checking autoplay status...");
            boolean isAutoplay = Boolean.parseBoolean(autoplayToggle.getAttribute("aria-checked"));
            if (isAutoplay) {
                System.out.println("Content autoplay is enabled. Disabling...");
                autoplayToggle.click();
            }
            settingsButton.click();
            System.out.println("Content autoplay is disabled");

            // Wait until real video duration appears
            String contentDurationText = ScreenRecorderTest.pollingContentDuration(driver);

            // Get current content time
            String currentContentTimeText = ScreenRecorderTest.getCurrentContentTimeText(driver);

            // Get current content speed
            String currentPlaybackSpeedText = ScreenRecorderTest.getCurrentPlaybackRateSpeed(driver);

            // If current content time is more than 5 seconds
            if (TimeConverter.convertToMilliseconds(currentContentTimeText) > 5 * 1000) {
                // Set the video to 2x
                System.out.println("***** WARNING *****\nWe are are quite far on content playback timeline.");
                System.out.println("Speeding content playback speed to 2x to play till the end");
                WebElement playbackRateMenuButton = driver.findElement(By.xpath("//button[@data-purpose='playback-rate-button']"));
                System.out.println("Starting content playback");
                playbackRateMenuButton.click();
                WebElement playbackRateMenuItem2x = driver.findElement(By.xpath("//ul[@data-purpose='playback-rate-menu']//span[text()='2x']"));
                System.out.println("Accelerating content playback speed to 2x");
                playbackRateMenuItem2x.click();

                currentPlaybackSpeedText = ScreenRecorderTest.getCurrentPlaybackRateSpeed(driver);

                // Calculate residual duration by the speed of 2x, adding 5s just in case
                int timeReserveMs = 5 * 1000;
                int contentDurationMs = TimeConverter.convertToMilliseconds(contentDurationText);
                int currentContentTimeMs = TimeConverter.convertToMilliseconds(currentContentTimeText);
                int remainingVideoTime = contentDurationMs - currentContentTimeMs;
                int remainingVideoTimeAdjusted = remainingVideoTime / 2;
                int requiredDelayMs = remainingVideoTimeAdjusted + timeReserveMs;

                System.out.println("Adjusted remaining content playback time is: " + String.format("%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(remainingVideoTime),
                        TimeUnit.MILLISECONDS.toSeconds(remainingVideoTime) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(remainingVideoTime))));

                // Play the content if it is not playing yet
                ScreenRecorderTest.playContent(driver);

                // Checking whether the content is playing or not
                ScreenRecorderTest.isContentPlayback(driver);

                // Setting the timer values
                long startTimeMs = System.currentTimeMillis();
                long endTimeMs = startTimeMs + requiredDelayMs;

                String formattedStartDateTime = ScreenRecorderTest.millisecondsToDateString(startTimeMs);
                System.out.println("Playback start date and time is: " + formattedStartDateTime);

                String formattedEndDateTime = ScreenRecorderTest.millisecondsToDateString(startTimeMs);
                System.out.println("Expected end date and time is: " + formattedEndDateTime);

                int pixelIncrement = 0;
                int xPos = 200;
                int yPos = 200;

                while (System.currentTimeMillis() < endTimeMs) {
                    String formattedDateTime = ScreenRecorderTest.millisecondsToDateString(startTimeMs);
                    System.out.println("Current date and time in cycle is: " + formattedDateTime);

                    // Perform your desired actions here
                    // Move mouse to absolute position
                    robot.mouseMove(xPos + pixelIncrement, yPos);
                    pixelIncrement++;
                    if (pixelIncrement > 20) {
                        pixelIncrement = 0;
                    }
                    System.out.println("Move the cursor to x: " + (xPos + pixelIncrement) + ", y: " + yPos);

                    // Wait for some time before repeating
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                // Go to google.com to reset the video settings
                driver.get("https://google.com");

                // Go to the page with video
                driver.get(lectures.get(j).getCurrentURL());

            }

            System.out.println("***** Proceeding with capturing of the content *****");

            // Wait 10 sec until content loaded
            try {
                Thread.sleep(10 * 1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            // Move mouse to absolute position
            robot.mouseMove(198, 198);
            System.out.println("Moved the cursor to x: " + 198 + ", y: " + 198);

            // Wait until real video duration appears
            ScreenRecorderTest.pollingContentDuration(driver);

            // Move mouse to absolute position
            robot.mouseMove(197, 197);
            System.out.println("Moved the cursor to x: " + 197 + ", y: " + 197);

            // Pause the video if it is playing now
            ScreenRecorderTest.pauseContentPlayback(driver);

            // Verify that there is less than 5 seconds from the start of the video
            currentContentTimeText = ScreenRecorderTest.getCurrentContentTimeText(driver);
            if (TimeConverter.convertToMilliseconds(currentContentTimeText) < 5 * 1000) {
                System.out.println("Current content playback time is less than 5 seconds");
            }

            if (!currentPlaybackSpeedText.equals("1.5x")) {
                // Speed up the video to 1.5x
                System.out.println("Speeding video to 1.5x");
                WebElement playbackRateMenuButton = driver.findElement(By.xpath("//button[@data-purpose='playback-rate-button']"));
                playbackRateMenuButton.click();
                WebElement playbackRateMenuItem15x = driver.findElement(By.xpath("//ul[@data-purpose='playback-rate-menu']//span[text()='1.5x']"));
                playbackRateMenuItem15x.click();
                currentPlaybackSpeedText = ScreenRecorderTest.getCurrentPlaybackRateSpeed(driver);
                System.out.println("Current video speed is " + currentPlaybackSpeedText);
            }

            // Enable English close captions
            WebElement closeCaptionsMenuButton = driver.findElement(By.xpath("//button[@data-purpose='captions-dropdown-button']"));
            closeCaptionsMenuButton.click();
            WebElement englishCloseCaptionsMenuItem = driver.findElement(By.xpath("//ul[@data-purpose='captions-dropdown-menu']//div[contains(text(),'English')]"));
            englishCloseCaptionsMenuItem.click();
            closeCaptionsMenuButton.click();

            // Play the video if it did not play before
            ScreenRecorderTest.playContent(driver);

            // Calculate video time
            // Get current time
            currentContentTimeText = ScreenRecorderTest.getCurrentContentTimeText(driver);

            // Get duration
            contentDurationText = driver.findElement(By.xpath("//span[@data-purpose='duration']")).getText();
            System.out.println("Video duration is " + contentDurationText);

            // Calculate real playback duration adjusted by speed multiplier
            int playbackDuration = TimeConverter.convertToMilliseconds(contentDurationText) - TimeConverter.convertToMilliseconds(currentContentTimeText);
            int playbackDurationAdjustedBySpeed = (int) (playbackDuration / 1.5);

            // Minimize browser window to let SikuliX manipulate OBS
            driver.manage().window().minimize();

            // Make sure that recording is started
            visualTestHelper = new VisualTestHelper();
            try {
                visualTestHelper.startRecording();
                visualTestHelper.isRecordingStarted();
            } catch (FindFailed e) {
                throw new RuntimeException(e);
            }

            // Maximize browser window
            driver.manage().window().maximize();

            // Move mouse to absolute position
            robot.mouseMove(199, 199);
            System.out.println("Moved the cursor to x: " + 199 + ", y: " + 199);

            // Set video full-screen
            WebElement fullScreenButton = driver.findElement(By.xpath("//div[@data-purpose='video-controls']/div[12]/button"));
            fullScreenButton.click();
            System.out.println("Entered full-screen mode");

            // Wait till the video will play till the end
            try {
                Thread.sleep(playbackDurationAdjustedBySpeed);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            // Perform action to exit full-screen mode using the JS executor
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("document.exitFullscreen();");
            System.out.println("Exiting full-screen mode with JavaScript");

            // Minimize browser window to let SikuliX manipulate OBS
            driver.manage().window().minimize();

            // Make sure that recording is started
            visualTestHelper = new VisualTestHelper();
            try {
                visualTestHelper.stopRecording();
                visualTestHelper.isRecordingStopped();
            } catch (FindFailed e) {
                throw new RuntimeException(e);
            }

            // Wait until file save is completed
            try {
                Thread.sleep(10 * 1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            FileHelper.renameMostRecentFile(videoFolderPath, FileHelper.normalizeFileName(lectures.get(j).getLectureName()));

            driver.manage().window().maximize();
        }

        // End of the content list
        // Close the browser
        driver.quit();
    }

    public static String pollingContentDuration(WebDriver driver) {
        // Wait until real video duration appears
        System.out.println("Polling webpage for content duration...");
        Robot robot;
        try {
            robot = new Robot();
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }

        String contentDurationText = "";
        while (contentDurationText.equals("") || contentDurationText.equals("0:00")) {
            // Get content duration
            robot.mouseMove(200, 200);
            robot.mouseMove(199, 199);
            contentDurationText = driver.findElement(By.xpath("//span[@data-purpose='duration']")).getText();
            System.out.println("Polling... time is " + contentDurationText);
        }
        System.out.println("Content duration received. Duration: " + contentDurationText);

        return contentDurationText;
    }

    public static String getCurrentContentTimeText(WebDriver driver) {
        // Get current content time
        String currentContentTimeText = "";
        Robot robot;
        try {
            robot = new Robot();
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }

        for (int i = 0; i < 10; i++){
            robot.mouseMove(200, 200);
            robot.mouseMove(199, 199);
            currentContentTimeText = driver.findElement(By.xpath("//span[@data-purpose='current-time']")).getText();
        }

        System.out.println("Current content playback position time is: " + currentContentTimeText);
        return currentContentTimeText;
    }

    public static String getCurrentPlaybackRateSpeed(WebDriver driver) {
        // Get current content speed
        System.out.println("Getting content playback speed...");
        WebElement currentPlaybackRateSpeed = driver.findElement(By.xpath("//button[@data-purpose='playback-rate-button']/span"));
        String currentPlaybackSpeedText = currentPlaybackRateSpeed.getText();
        System.out.println("Current content speed is: " + currentPlaybackSpeedText);
        return currentPlaybackSpeedText;
    }

    public static void pauseContentPlayback(WebDriver driver) {
        // Pause the content if it is playing now
        List<WebElement> pauseContentPlaybackButtons = driver.findElements(By.xpath("//button[@data-purpose='pause-button']"));
        if (pauseContentPlaybackButtons.size() > 0) {
            System.out.println("Content playback is active");
            pauseContentPlaybackButtons.get(0).click();
            System.out.println("Content playback is paused");
        }
    }

    public static boolean isContentPlayback(WebDriver driver) {
        List<WebElement> pauseContentPlaybackButtons = driver.findElements(By.xpath("//button[@data-purpose='pause-button']"));
        if (pauseContentPlaybackButtons.size() > 0) {
            System.out.println("Content playback is active");
            return true;
        } else {
            System.out.println("Content playback is paused");
            return false;
        }
    }

    public static void playContent(WebDriver driver) {
        // Play the content if it is not playing yet
        List<WebElement> playContentButtons = driver.findElements(By.xpath("//button[@data-purpose='play-button']"));
        if (playContentButtons.size() > 0) {
            System.out.println("Content playback is paused");
            playContentButtons.get(0).click();
            System.out.println("Attempting to start content playback");
        }
    }

    public static String millisecondsToDateString(long milliseconds) {
        // Create a Date object from the current time in milliseconds
        Date currentDate = new Date(milliseconds);
        // Create a SimpleDateFormat object to specify the desired date and time format
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // Format the date as a string
        String formattedDateTime = dateFormat.format(currentDate);

        return formattedDateTime;
    }

}