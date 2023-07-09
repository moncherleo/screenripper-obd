import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.sikuli.script.FindFailed;
import utils.*;

import java.awt.*;
import java.awt.event.InputEvent;
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

        System.out.println("Setting up Selenium WebDriver with Chrome");
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
        System.out.println("Selenium WebDriver with Chrome is launched");

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
        System.out.println("Selenium top bar is closed");

        // Add cookies to browser
        Set<Cookie> cookies = CookieConverter.convertJsonToCookies(cookieJSONFilePath);
        for (Cookie cookie : cookies) {
            driver.manage().addCookie(cookie);
        }
        System.out.println("Cookies are added to the Chrome browser");

        // Starting content processing in the BIG cycle
        System.out.println("Starting content processing...");
        for (int j = 0; j < lectures.size(); j++) {
            String contentDurationText = "";
            String currentContentTimeText = "";
            String currentPlaybackSpeedText = "";

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
            if (ScreenRecorderTest.isContentPlaying(driver, robot)) {
                ScreenRecorderTest.pauseContentPlayback(driver, robot);
            }

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
            contentDurationText = ScreenRecorderTest.pollingContentDuration(driver, robot);

            // Get current content time
            currentContentTimeText = ScreenRecorderTest.pollingCurrentContentTimeText(driver, robot);

            // Get current content speed
            currentPlaybackSpeedText = ScreenRecorderTest.getCurrentPlaybackRateSpeed(driver, robot);

            // If current content time is more than 5 seconds
            while (TimeConverter.convertToMilliseconds(currentContentTimeText) > 5 * 1000) {
                // Set the video to 2x
                System.out.println("***** WARNING *****\nWe are are quite far on content playback timeline.");
                System.out.println("Speeding content playback speed to 2x to play till the end");

                // Pause the content if it is playing now
                if (ScreenRecorderTest.isContentPlaying(driver, robot)) {
                    ScreenRecorderTest.pauseContentPlayback(driver, robot);
                }

                // Get current content time
                currentContentTimeText = ScreenRecorderTest.pollingCurrentContentTimeText(driver, robot);
                int currContTime = TimeConverter.convertToMilliseconds(currentContentTimeText);

                // Trying to rewind the content
                currentContentTimeText = ScreenRecorderTest.progressBarRewind(driver, robot);
                int newContTime = TimeConverter.convertToMilliseconds(currentContentTimeText);

                // Comparing times
                if (currContTime + (20 * 1000) < newContTime) {
                    System.out.println("Rewind is successful!");
                } else {
                    System.out.println("Rewind failed...");
                }

                // Setting playback speed to 2x via menu item
                WebElement playbackRateMenuButton = driver.findElement(By.xpath("//button[@data-purpose='playback-rate-button']"));
                System.out.println("Setting content playback rate");
                playbackRateMenuButton.click();
                WebElement playbackRateMenuItem2x = driver.findElement(By.xpath("//ul[@data-purpose='playback-rate-menu']//span[text()='2x']"));
                System.out.println("Accelerating content playback speed to 2x");
                playbackRateMenuItem2x.click();
                playbackRateMenuButton = driver.findElement(By.xpath("//button[@data-purpose='playback-rate-button']"));
                playbackRateMenuButton.click();

                // Get actual content values
                contentDurationText = ScreenRecorderTest.pollingContentDuration(driver, robot);
                currentContentTimeText = ScreenRecorderTest.pollingCurrentContentTimeText(driver, robot);
                currentPlaybackSpeedText = ScreenRecorderTest.getCurrentPlaybackRateSpeed(driver, robot);

                // Calculate residual duration by the speed of 2x, adding 5s just in case
                int timeReserveMs = 5 * 1000;
                int contentDurationMs = TimeConverter.convertToMilliseconds(contentDurationText);
                int currentContentTimeMs = TimeConverter.convertToMilliseconds(currentContentTimeText);
                int remainingVideoTime = contentDurationMs - currentContentTimeMs;
                int remainingVideoTimeAdjusted = remainingVideoTime / 2;
                int requiredDelayMs = remainingVideoTimeAdjusted + timeReserveMs;

                System.out.println("Remaining content playback time is: " + String.format("%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(remainingVideoTime),
                        TimeUnit.MILLISECONDS.toSeconds(remainingVideoTime) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(remainingVideoTime))));

                System.out.println("Adjusted remaining content playback time is: " + String.format("%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(requiredDelayMs),
                        TimeUnit.MILLISECONDS.toSeconds(requiredDelayMs) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(requiredDelayMs))));

                // Play the content
                ScreenRecorderTest.playContent(driver, robot);
                if (!ScreenRecorderTest.isContentPlaying(driver, robot)) {
                    ScreenRecorderTest.playContent(driver, robot);
                }

                // Setting up the timer
                long startTimeMs = System.currentTimeMillis();
                long endTimeMs = startTimeMs + requiredDelayMs;

                // Logging time to console in human-readable format
                String formattedStartDateTime = ScreenRecorderTest.millisecondsToDateString(startTimeMs);
                System.out.println("Playback start date and time is: " + formattedStartDateTime);
                String formattedEndDateTime = ScreenRecorderTest.millisecondsToDateString(startTimeMs);
                System.out.println("Expected end date and time is: " + formattedEndDateTime);

                // Coordinates for mouse moves
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

                    // Wait for some time before repeat
                    try {
                        Thread.sleep(5 * 1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                // Go to google.com to reset the video settings
                driver.get("https://google.com");

                // Go to the page with video
                driver.get(lectures.get(j).getCurrentURL());

                // Wait until real video duration appears
                contentDurationText = ScreenRecorderTest.pollingContentDuration(driver, robot);

                // Get current content time
                currentContentTimeText = ScreenRecorderTest.pollingCurrentContentTimeText(driver, robot);

                // Get current content speed
                currentPlaybackSpeedText = ScreenRecorderTest.getCurrentPlaybackRateSpeed(driver, robot);

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
            ScreenRecorderTest.pollingContentDuration(driver, robot);

            // Move mouse to absolute position
            robot.mouseMove(197, 197);
            System.out.println("Moved the cursor to x: " + 197 + ", y: " + 197);

            // Pause the video if it is playing now
            ScreenRecorderTest.pauseContentPlayback(driver, robot);

            // Verify that there is less than 5 seconds from the start of the video
            currentContentTimeText = ScreenRecorderTest.pollingCurrentContentTimeText(driver, robot);
            if (TimeConverter.convertToMilliseconds(currentContentTimeText) < 5 * 1000) {
                System.out.println("Current content playback time is " + currentContentTimeText);
            }

            if (!currentPlaybackSpeedText.equals("1.5x")) {
                // Speed up the video to 1.5x
                System.out.println("Speeding video to 1.5x");
                WebElement playbackRateMenuButton = driver.findElement(By.xpath("//button[@data-purpose='playback-rate-button']"));
                playbackRateMenuButton.click();
                WebElement playbackRateMenuItem15x = driver.findElement(By.xpath("//ul[@data-purpose='playback-rate-menu']//span[text()='1.5x']"));
                playbackRateMenuItem15x.click();
                playbackRateMenuButton = driver.findElement(By.xpath("//button[@data-purpose='playback-rate-button']"));
                playbackRateMenuButton.click();
                currentPlaybackSpeedText = ScreenRecorderTest.getCurrentPlaybackRateSpeed(driver, robot);
                System.out.println("Current video speed is " + currentPlaybackSpeedText);
            }

            // Enable English close captions
            WebElement closeCaptionsMenuButton = driver.findElement(By.xpath("//button[@data-purpose='captions-dropdown-button']"));
            closeCaptionsMenuButton.click();
            WebElement englishCloseCaptionsMenuItem = driver.findElement(By.xpath("//ul[@data-purpose='captions-dropdown-menu']//div[contains(text(),'English')]"));
            englishCloseCaptionsMenuItem.click();
            closeCaptionsMenuButton.click();

            // Calculate video time
            // Get current time
            currentContentTimeText = ScreenRecorderTest.pollingCurrentContentTimeText(driver, robot);

            // Get duration
            contentDurationText = driver.findElement(By.xpath("//span[@data-purpose='duration']")).getText();
            System.out.println("Video duration is " + contentDurationText);

            // Calculate real playback duration adjusted by speed multiplier
            int playbackDuration = TimeConverter.convertToMilliseconds(contentDurationText) - TimeConverter.convertToMilliseconds(currentContentTimeText);
            int playbackDurationAdjustedBySpeed = (int) (playbackDuration / 1.5);

            // Play the content
            ScreenRecorderTest.playContent(driver, robot);

            // Checking whether the content is playing or not
            if (!ScreenRecorderTest.isContentPlaying(driver, robot)) {
                ScreenRecorderTest.playContent(driver, robot);
            }

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
            robot.mouseMove(10, 199);
            System.out.println("Moved the cursor to x: " + 10 + ", y: " + 199);

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

            // Stopping the recording
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

    public static String pollingContentDuration(WebDriver driver, Robot robot) {
        // Wait until real video duration appears
        System.out.println("Polling webpage for content duration...");
        String contentDurationText = "";

        while (contentDurationText.equals("") || contentDurationText.equals("0:00")) {
            // Get content duration
            robot.mouseMove(200, 200);
            robot.mouseMove(199, 199);
            contentDurationText = driver.findElement(By.xpath("//span[@data-purpose='duration']")).getText();
            System.out.println("Polling... content duration time is " + contentDurationText);
        }
        System.out.println("Content duration received. Duration: " + contentDurationText);

        return contentDurationText;
    }

    public static String pollingCurrentContentTimeText(WebDriver driver, Robot robot) {
        // Get current content time
        String currentContentTimeText = "";
        String currentContentTimeXpath = "//span[@data-purpose='current-time']";
        List<WebElement> currentContentTime = driver.findElements(By.xpath(currentContentTimeXpath));

        while (currentContentTime.size() < 1) {
            robot.mouseMove(200, 200);
            robot.mouseMove(199, 199);
            currentContentTime = driver.findElements(By.xpath(currentContentTimeXpath));
        }
        currentContentTimeText = currentContentTime.get(0).getText();

        while (currentContentTimeText.equals("")) {
            robot.mouseMove(200, 200);
            robot.mouseMove(199, 199);
            currentContentTime = driver.findElements(By.xpath(currentContentTimeXpath));
            currentContentTimeText = currentContentTime.get(0).getText();
        }

        int currTime = TimeConverter.convertToMilliseconds(currentContentTimeText);

        if (currTime == 0){
            if (!isContentPlaying(driver, robot)){
                playContent(driver, robot);
                try {
                    Thread.sleep(2 * 1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                pauseContentPlayback(driver, robot);
            }
        }

        currentContentTime = driver.findElements(By.xpath(currentContentTimeXpath));
        int futureTime = TimeConverter.convertToMilliseconds(currentContentTimeText);

        if (currTime + 1 > futureTime){
            currentContentTimeText = "0:06";
        }

        System.out.println("Current content playback position time is: " + currentContentTimeText);
        return currentContentTimeText;
    }

    public static String getCurrentPlaybackRateSpeed(WebDriver driver, Robot robot) {
        // Get current content speed
        String currentPlaybackRateSpeedXpath = "//button[@data-purpose='playback-rate-button']/span";
        System.out.println("Getting content playback speed...");
        List<WebElement> currentPlaybackRateSpeed = driver.findElements(By.xpath(currentPlaybackRateSpeedXpath));

        while (currentPlaybackRateSpeed.size() < 1) {
            robot.mouseMove(200, 200);
            robot.mouseMove(199, 199);
            currentPlaybackRateSpeed = driver.findElements(By.xpath(currentPlaybackRateSpeedXpath));
        }

        String currentPlaybackSpeedText = currentPlaybackRateSpeed.get(0).getText();
        System.out.println("Current content speed is: " + currentPlaybackSpeedText);
        return currentPlaybackSpeedText;
    }

    public static void pauseContentPlayback(WebDriver driver, Robot robot) {
        // Pause the content if it is playing now
        robot.mouseMove(200, 200);
        robot.mouseMove(199, 199);

        List<WebElement> pauseContentPlaybackButtons = driver.findElements(By.xpath("//button[@data-purpose='pause-button']"));
        if (pauseContentPlaybackButtons.size() > 0) {
            System.out.println("Content playback is active");
            pauseContentPlaybackButtons.get(0).click();
            System.out.println("Content playback is paused");
        }
    }

    public static boolean isContentPlaying(WebDriver driver, Robot robot) {
        String pauseButtonXpath = "//button[@data-purpose='pause-button']";
        String playButtonXpath = "//button[@data-purpose='play-button']";
        robot.mouseMove(200, 200);
        robot.mouseMove(199, 199);
        List<WebElement> pauseContentPlaybackButtons = driver.findElements(By.xpath(pauseButtonXpath));

        if (pauseContentPlaybackButtons.size() > 0) {
            System.out.println("Content playback is active");
            return true;
        } else {
            System.out.println("Content playback is paused");
            return false;
        }
    }

    public static void playContent(WebDriver driver, Robot robot) {
        // Play the content if it is not playing yet
        robot.mouseMove(200, 200);
        robot.mouseMove(199, 199);

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

    public static String progressBarRewind(WebDriver driver, Robot robot) {

        String progressBarXpath = "//div[@data-purpose='video-progress-bar']";
        List<WebElement> progressBars = driver.findElements(By.xpath(progressBarXpath));

        System.out.println("Locating content playback progress bar...");
        while (progressBars.size() < 1) {
            robot.mouseMove(200, 200);
            robot.mouseMove(199, 199);
            progressBars = driver.findElements(By.xpath(progressBarXpath));
        }
        System.out.println("Content playback progress bar found! Trying to rewind the content...");

        robot.mouseMove(200, 200);
        robot.mouseMove(199, 199);
        progressBars = driver.findElements(By.xpath(progressBarXpath));
        Point location = progressBars.get(0).getLocation();
        Dimension size = progressBars.get(0).getSize();

        int clickX = location.getX() + size.getWidth() - 5;
        int clickY = location.getY() + size.getHeight() / 2;

        robot.mouseMove(200, 200);
        robot.mouseMove(199, 199);
        robot.mouseMove(clickX, clickY);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);

        return ScreenRecorderTest.pollingCurrentContentTimeText(driver, robot);

    }

}